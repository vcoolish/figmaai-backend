package com.app.drivn.backend.common.blockchain

import com.app.drivn.backend.common.blockchain.entity.Direction
import com.app.drivn.backend.common.blockchain.entity.TransactionUnprocessed
import com.app.drivn.backend.common.blockchain.model.BlockchainState
import com.app.drivn.backend.common.blockchain.model.TransactionCache
import com.app.drivn.backend.common.blockchain.repository.BlockchainStateRepository
import com.app.drivn.backend.common.util.logger
import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.user.dto.BalanceType
import com.app.drivn.backend.user.model.User
import com.app.drivn.backend.user.service.UserService
import net.osslabz.evm.abi.decoder.AbiDecoder
import org.springframework.boot.context.event.ApplicationFailedEvent
import org.springframework.boot.context.event.ApplicationStartedEvent
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
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.tx.FastRawTransactionManager
import java.math.BigDecimal
import java.math.BigInteger
import java.net.http.WebSocket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Stream

@Service
class BlockchainService(
  private val userService: UserService,
  private val privateKeyProvider: PrivateKeyProvider,
  private val appProperties: AppProperties,
  private val blockchainStateRepository: BlockchainStateRepository,
) : WebSocket.Listener {

  val logger = logger()
  var erc20Decoder: AbiDecoder = AbiDecoder(appProperties.ercFile.inputStream)

  private val client: Web3j = Web3j.build(HttpService(appProperties.clientUrl))
  private val tokenUnit = com.app.drivn.backend.common.blockchain.entity.Unit(8, "DRIVE", "DRIVE")
  private val coinUnit = com.app.drivn.backend.common.blockchain.entity.Unit(18, "BNB", "BNB")

  //todo: add scheduler and process time to time
  private val queue = CopyOnWriteArrayList<TransactionUnprocessed>()

  @EventListener
  fun onApplicationStopped(event: ApplicationFailedEvent) {
    blockchainStateRepository.save(
      BlockchainState().apply {
        lastProcessedBlock = (client.ethBlockNumber().send().blockNumber - BigInteger.ONE).toString()
        transactions = queue.map {
          TransactionCache().apply {
            amount = it.amount
            txType = it.type.name
            address = it.address
            direction = it.direction.name
          }
        }
      }
    )
  }

  @EventListener
  fun onApplicationStarted(event: ApplicationStartedEvent) {
    //save processed tx id and check it to avoid double spends
    val states = blockchainStateRepository.findAll()
    val startBlock = (states.lastOrNull()?.lastProcessedBlock)?.toBigInteger()  ?: BigInteger.ZERO
    val endBlock = client.ethBlockNumber().send().blockNumber
    if (startBlock > BigInteger.ZERO && startBlock < endBlock) {
      processBlocks(startBlock, endBlock)
      states.forEach {
        it.transactions.forEach { tx ->
          processTxCache(tx)
        }
        blockchainStateRepository.delete(it)
      }
    }

    var isPassedStart = false
    client.transactionFlowable().subscribe {
      if (isPassedStart) {
        processTx(it)
      } else {
        isPassedStart = it.blockNumber !in startBlock..endBlock
      }
    }
  }

  private fun processTxCache(tx: TransactionCache) {
    when (tx.txType) {
      BalanceType.coin.name -> when (tx.direction) {
        Direction.withdraw.name -> withdrawCoin(tx.address)
        Direction.deposit.name -> depositCoin(tx.address, tx.amount)
      }
      BalanceType.token.name -> when (tx.direction) {
        Direction.withdraw.name -> withdrawToken(tx.address)
        Direction.deposit.name -> depositToken(tx.address, tx.amount)
      }
    }
  }

  private fun processBlocks(start: BigInteger, end: BigInteger) {
    Stream.iterate(start, { current -> current <= end }, BigInteger::inc)
      .map(DefaultBlockParameter::valueOf)
      .forEach {
        client.ethGetBlockByNumber(it, true).sendAsync().whenComplete { result, ex ->
          if (ex != null) {
            logger.warn("Got an exception on getting block ${it.value}", ex)
            return@whenComplete
          }
          result.block.transactions.stream().map { tx ->
            tx.get() as org.web3j.protocol.core.methods.response.Transaction
          }.forEach(::processTx)
        }
      }
  }

  private fun processTx(tx: org.web3j.protocol.core.methods.response.Transaction) {
    try {
      if (tx.to.equals(appProperties.adminAddress, true)) {
        depositCoin(tx.from, tx.value.toBigDecimal())
      } else if (tx.to.equals(appProperties.contractAddress, true)) {
        val call = erc20Decoder.decodeFunctionCall(tx.input)
        val name = call?.name
        val amount = BigDecimal(call.getParam("amount").value.toString())
        if (name == "burn") {
          depositToken(tx.from, amount)
        } else if (name == "burnFrom") {
          val account = call.getParam("account").value
          depositToken(account.toString(), amount)
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

  private fun depositToken(to: String, amount: BigDecimal) {
    val item = TransactionUnprocessed(to, Direction.deposit, amount, BalanceType.token)
    queue.add(item)
    userService.addToTokenBalance(to, tokenUnit.toValue(amount))
    queue.remove(item)
  }

  fun withdrawToken(to: String): User {
    val amount = userService.get(to).tokensToClaim
    val item = TransactionUnprocessed(to, Direction.withdraw, amount, BalanceType.token)
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
    val amount = userService.get(to).balance
    val item = TransactionUnprocessed(to, Direction.withdraw, amount, BalanceType.coin)
    queue.add(item)
    transferCoins(to, coinUnit.toUnit(amount))
    val user = userService.subtractFromBalance(to, amount)
    queue.remove(item)
    return user
  }

  private fun depositCoin(to: String, amount: BigDecimal) {
    try {

      val fee = BigDecimal.valueOf(0.0005)
      val item = TransactionUnprocessed(to, Direction.deposit, amount, BalanceType.coin)
      queue.add(item)
      val finalUnitAmount = coinUnit.toValue(amount).minus(fee).max(BigDecimal.ZERO)
      userService.addToBalance(to, finalUnitAmount)
      queue.remove(item)
    } catch (t: Throwable) {
      t.printStackTrace()
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
    val transactionForEstimate = Transaction.createFunctionCallTransaction(
      appProperties.adminAddress,
      nonce,
      gasPrice,
      BigInteger.valueOf(1000000),
      contractAddress,
      BigInteger.ZERO,
      encodedFunction
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

