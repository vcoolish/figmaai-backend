package com.app.surnft.backend.blockchain.service

import com.app.surnft.backend.blockchain.entity.TransactionUnprocessed
import com.app.surnft.backend.blockchain.entity.Unit
import com.app.surnft.backend.blockchain.model.BalanceType
import com.app.surnft.backend.blockchain.model.BlockchainState
import com.app.surnft.backend.blockchain.model.Direction
import com.app.surnft.backend.blockchain.model.TransactionCache
import com.app.surnft.backend.blockchain.repository.BlockchainStateRepository
import com.app.surnft.backend.common.util.logger
import com.app.surnft.backend.config.properties.AppProperties
import com.app.surnft.backend.user.model.User
import com.app.surnft.backend.user.service.UserEnergyService
import com.app.surnft.backend.user.service.UserService
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import net.osslabz.evm.abi.decoder.AbiDecoder
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Bip32ECKeyPair.HARDENED_BIT
import org.web3j.crypto.Credentials
import org.web3j.crypto.MnemonicUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.filters.BlockFilter
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult
import org.web3j.protocol.http.HttpService
import org.web3j.tx.FastRawTransactionManager
import org.web3j.utils.Async
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import java.net.http.WebSocket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Stream
import javax.annotation.PreDestroy

@Service
class BlockchainService(
  private val userService: UserService,
  private val privateKeyProvider: PrivateKeyProvider,
  private val appProperties: AppProperties,
  private val blockchainStateRepository: BlockchainStateRepository,
  private val userEnergyService: UserEnergyService,
) : WebSocket.Listener {

  val logger = logger()
  var erc20Decoder: AbiDecoder = AbiDecoder(appProperties.getErcFile().inputStream)

  private var started: Boolean = false
  private val client: Web3j = Web3j.build(HttpService(appProperties.clientUrl))
  private val fallbackClient: Web3j = Web3j.build(HttpService(appProperties.secondClientUrl))
  private val tokenUnit = com.app.surnft.backend.blockchain.entity.Unit(8, "DRIVE", "DRIVE")
  private val coinUnit = com.app.surnft.backend.blockchain.entity.Unit(18, "BNB", "BNB")

  //todo: add scheduler and process time to time
  private val queue = CopyOnWriteArrayList<com.app.surnft.backend.blockchain.entity.TransactionUnprocessed>()

  @PreDestroy
  fun onApplicationStopped() {
    if (!started) {
      // nothing to save
      return
    }
    logger.info("Saving blockchain state...")

    val state = BlockchainState()
    state.lastProcessedBlock = client.ethBlockNumber().send().blockNumber.dec().toString()
    state.transactions = queue.map {
      TransactionCache().apply {
        amount = it.amount
        txType = it.type
        address = it.address
        direction = it.direction
        blockchainState = state
      }
    }
    blockchainStateRepository.save(state)
    logger.info("Blockchain state saved.")
  }

  @EventListener(ApplicationReadyEvent::class)
  fun onApplicationStarted() {
    init()
  }

  private fun init() {
    logger.info("Syncing with blockchain...")

    //save processed tx id and check it to avoid double spends
    val blocks = try {
      val states = blockchainStateRepository.findAll()
      val startBlock = (states.lastOrNull()?.lastProcessedBlock)?.toBigInteger() ?: BigInteger.ZERO
      val endBlock = client.ethBlockNumber().send().blockNumber

      started = true

      if (startBlock > BigInteger.ZERO && startBlock < endBlock) {
        processBlocks(startBlock, endBlock)
        states.forEach {
          it.transactions.forEach(this::processTxCache)
          blockchainStateRepository.delete(it)
        }
      }
      Pair(startBlock, endBlock)
    } catch (t: Throwable) {
      logger.error("Failed to process skipped blocks!", t)
      Pair(BigInteger.ZERO, BigInteger.ZERO)
    }

    var isPassedStart = false
    //implement block queue and reprocess failed blocks
    Flowable.create({ subscriber: FlowableEmitter<String?> ->
      val blockFilter = BlockFilter(client) { value: String? ->
        subscriber.onNext(value ?: "")
      }
      try {
        blockFilter.run(Async.defaultExecutorService(), 3000L)
      } catch (t: Throwable) {
        init()
        subscriber.onError(t)
      }
      subscriber.setCancellable { blockFilter.cancel() }
    }, BackpressureStrategy.BUFFER)
      .subscribe(
        {
          try {
            val block = client.ethGetBlockByHash(it, true).send().result
            val txs = block.transactions
              .map { transactionResult: TransactionResult<*> -> transactionResult.get() as org.web3j.protocol.core.methods.response.Transaction }
            txs.forEach { tx ->
              if (isPassedStart) {
                //todo: save processed tx hash and check if we have it
                processTx(tx)
              } else {
                isPassedStart = tx.blockNumber !in blocks.first..blocks.second
              }
            }
          } catch (ignored: Throwable) { }
        },
        { logger.error("An error occurred while listening to the blockchain", it) }
      )

    logger.info("Synced with blockchain.")
  }

  private fun processTxCache(tx: TransactionCache) {
    when (tx.txType) {
      BalanceType.COIN -> when (tx.direction) {
        Direction.WITHDRAW -> withdrawCoin(tx.address)
        Direction.DEPOSIT -> depositCoin(tx.address, tx.amount)
      }

      BalanceType.TOKEN -> when (tx.direction) {
        Direction.WITHDRAW -> withdrawToken(tx.address)
        Direction.DEPOSIT -> depositToken(tx.address, tx.amount)
      }
    }
  }

  private fun processBlocks(start: BigInteger, end: BigInteger) {
    Stream.iterate(start, { current -> current <= end }, BigInteger::inc)
      .map(DefaultBlockParameter::valueOf)
      .forEach { block ->
//        client.ethGetBlockByNumber(it, true).sendAsync().whenComplete { result, ex ->
//          if (ex != null) {
//            logger.warn("Got an exception on getting block ${it.value}", ex)
//            return@whenComplete
//          }
//          result.block.transactions.stream().map { tx ->
//            tx.get() as org.web3j.protocol.core.methods.response.Transaction
//          }.forEach(::processTx)
//        }
        val result = kotlin.runCatching {
          client.ethGetBlockByNumber(block, true).send()
        }.getOrElse {
          kotlin.runCatching {
            fallbackClient.ethGetBlockByNumber(block, true).send()
          }.getOrElse {
            logger.warn("Got an exception on getting block ${block.value}", it)
            return@forEach
          }
        }
        result.block.transactions.stream().map { tx ->
          tx.get() as org.web3j.protocol.core.methods.response.Transaction
        }.forEach(::processTx)
      }
  }

  private fun processTx(tx: org.web3j.protocol.core.methods.response.Transaction) {
    try {
      if (tx.to.equals(appProperties.adminAddress, true)) {
        depositCoin(tx.from, tx.value.toBigDecimal())
      } else if (tx.to.equals(appProperties.contractAddress, true)) {
        val call = erc20Decoder.decodeFunctionCall(tx.input)
        val name = call?.name?.toString()
        val amount = BigDecimal(call.getParam("amount").value.toString())
        if (name == "burn") {
          depositToken(tx.from, amount)
        } else if (name == "burnFrom") {
          val account = call.getParam("account").value
          depositToken(account.toString(), amount)
        }
      } else if (tx.to.equals(appProperties.collectionAddress, true)) {
        val call = erc20Decoder.decodeFunctionCall(tx.input)
        val name = call?.name?.toString()
        if (name == "transferFrom" || name == "safeTransferFrom") {
          transferCarOwnership(
            from = call.getParam("from").value.toString(),
            to = call.getParam("to").value.toString(),
            tokenId = call.getParam("tokenId").value.toString(),
          )
        } else if (name == "burn") {
          transferCarOwnership(
            from = tx.from,
            to = "0x0000000000000000000000000000000000000000",
            tokenId = call.getParam("tokenId").value.toString(),
          )
        }
      } else {
        return
      }
      logger.debug("New transaction")
      logger.debug("from" + tx.from)
      logger.debug("to" + tx.to)
      logger.debug(tx.value.toString())
    } catch (ignored: Throwable) { }
  }

  private fun transferCarOwnership(from: String, to: String, tokenId: String) {
    val sender = userService.getOrCreate(from)
    val recipient = userService.getOrCreate(to)
    val nft = sender.nfts.first { it.id == tokenId.toLong(16) }
    val senderNfts = sender.nfts.toMutableList()
    val recipientNfts = recipient.nfts.toMutableList()
    senderNfts.remove(nft)
    recipientNfts.add(nft)

    if (sender.nfts.size == 3 && senderNfts.size < 3) {
      userEnergyService.decreaseMaxEnergy(sender)
    }
    if (recipient.nfts.size < 3 && senderNfts.size == 3) {
      userEnergyService.increaseMaxEnergy(sender)
    }

    sender.nfts = senderNfts
    recipient.nfts = recipientNfts
    userService.save(sender)
    userService.save(recipient)
  }

  private fun depositToken(to: String, amount: BigDecimal) {
    val item = com.app.surnft.backend.blockchain.entity.TransactionUnprocessed(
      to,
      Direction.DEPOSIT,
      amount,
      BalanceType.TOKEN
    )
    queue.add(item)
    userService.addToTokenBalance(to, tokenUnit.toValue(amount))
    queue.remove(item)
  }

  fun withdrawToken(to: String): User {
    val amount = userService.getOrCreate(to).tokensToClaim
    val item = com.app.surnft.backend.blockchain.entity.TransactionUnprocessed(
      to,
      Direction.WITHDRAW,
      amount,
      BalanceType.TOKEN
    )
    queue.add(item)
    mint(
      contractAddress = appProperties.contractAddress,
      address = to,
      tokenId = tokenUnit.toUnit(amount),
    )
    val user = userService.subtractFromTokenBalance(to, amount)
    queue.remove(item)
    return user
  }

  fun withdrawCoin(to: String): User {
    val amount = userService.getOrCreate(to).balance
    val item = com.app.surnft.backend.blockchain.entity.TransactionUnprocessed(
      to,
      Direction.WITHDRAW,
      amount,
      BalanceType.COIN
    )
    queue.add(item)
    transferCoins(to, coinUnit.toUnit(amount))
    val user = userService.subtractFromBalance(to, amount)
    queue.remove(item)
    return user
  }

  private fun depositCoin(to: String, amount: BigDecimal) {
    try {
      val fee = BigDecimal.valueOf(0.0005)
      val item = com.app.surnft.backend.blockchain.entity.TransactionUnprocessed(
        to,
        Direction.DEPOSIT,
        amount,
        BalanceType.COIN
      )
      queue.add(item)
      val finalUnitAmount = coinUnit.toValue(amount).minus(fee).max(BigDecimal.ZERO)
      userService.addToBalance(to, finalUnitAmount)
      queue.remove(item)
    } catch (t: Throwable) {
      logger.error("Failed to deposit coin to $to", t)
    }
  }

  private fun transferCoins(address: String, amount: BigInteger): String {
    val gasPrice = client.ethGasPrice().send().gasPrice
    return FastRawTransactionManager(client, loadCreds(), appProperties.chainId)
      .sendTransaction(
        gasPrice,
        BigInteger.valueOf(21000),
        address,
        "",
        amount,
      ).transactionHash
  }

  private fun loadCreds(): Credentials {
    val path = intArrayOf(44 or HARDENED_BIT, 60 or HARDENED_BIT, 0 or HARDENED_BIT, 0, 0)
    val seed = MnemonicUtils.generateSeed(privateKeyProvider.fetchPrivateKey(),"")
    val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
    val bip44Keypair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, path)

    return Credentials.create(bip44Keypair)
  }

  fun mint(
    contractAddress: String,
    address: String,
    tokenId: BigInteger,
  ): String {
    val function = Function(
      "mint",
      listOf(org.web3j.abi.datatypes.Address(address), org.web3j.abi.datatypes.Uint(tokenId)),
      listOf(),
    )
    val encodedFunction = FunctionEncoder.encode(function)
    val gasPrice = client.ethGasPrice().send().gasPrice

    val nonce = client.ethGetTransactionCount(appProperties.adminAddress, DefaultBlockParameterName.LATEST)
      .send().transactionCount
    val transactionForEstimate = Transaction(
      appProperties.adminAddress,
      nonce,
      gasPrice,
      BigInteger.valueOf(1_000_000),
      contractAddress,
      BigInteger.ZERO,
      encodedFunction,
      56L,
      null,
      null,
    )
    val gasLimit = client.ethEstimateGas(transactionForEstimate).send().amountUsed
    return FastRawTransactionManager(client, loadCreds(), appProperties.chainId)
      .sendTransaction(
        gasPrice,
        gasLimit,
        contractAddress,
        encodedFunction,
        BigInteger.ZERO,
      ).transactionHash
  }
}

