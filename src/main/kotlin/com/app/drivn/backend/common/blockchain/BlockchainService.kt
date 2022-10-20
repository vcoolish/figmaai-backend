package com.app.drivn.backend.common.blockchain

import com.app.drivn.backend.common.blockchain.entity.Direction
import com.app.drivn.backend.common.blockchain.entity.TransactionCache
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
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.tx.FastRawTransactionManager
import java.math.BigDecimal
import java.math.BigInteger
import java.net.http.WebSocket
import java.util.concurrent.CopyOnWriteArrayList

@Service
class BlockchainService(
  private val userService: UserService,
  private val privateKeyProvider: PrivateKeyProvider,
  private val appProperties: AppProperties,
) : WebSocket.Listener {

  val logger = logger()
  var erc20Decoder: AbiDecoder = AbiDecoder(appProperties.ercFile.inputStream)

  private val client: Web3j = Web3j.build(HttpService(appProperties.clientUrl))
  private val tokenUnit = com.app.drivn.backend.common.blockchain.entity.Unit(8, "DRIVE", "DRIVE")
  private val coinUnit = com.app.drivn.backend.common.blockchain.entity.Unit(18, "BNB", "BNB")

  //todo: Cache it
  private val queue = CopyOnWriteArrayList<TransactionCache>()

  @EventListener
  fun onApplicationStopped(event: ApplicationFailedEvent) {
    //todo: Cache queue
  }

  @EventListener
  fun onApplicationStarted(event: ApplicationStartedEvent) {
    // todo: store start block in db
    //todo: process cached queue
//    val startBlock = (100500).toBigInteger()
//    val endBlock = client.ethBlockNumber().send().blockNumber
//
//    Stream.iterate(startBlock, { current -> current <= endBlock }, BigInteger::inc)
//      .map(DefaultBlockParameter::valueOf)
//      .forEach {
//        client.ethGetBlockByNumber(it, true).sendAsync().whenComplete { result, ex ->
//          if (ex != null) {
//            logger.warn("Got an exception on getting block ${it.value}", ex)
//            return@whenComplete
//          }
//          result.block.transactions.stream().map { tx ->
//            tx.get() as org.web3j.protocol.core.methods.response.Transaction
//          }.forEach(::processTx)
//        }
//      }

    var isPassedStart = false
    client.transactionFlowable().subscribe {
      if (isPassedStart) {
        processTx(it)
      } else {
        isPassedStart = true//it.blockNumber !in startBlock..endBlock
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
    val item = TransactionCache(to, Direction.deposit, amount, BalanceType.token)
    queue.add(item)
    userService.addToTokenBalance(to, tokenUnit.toValue(amount))
    queue.remove(item)
  }

  fun withdrawToken(to: String, amount: BigDecimal): User {
    val item = TransactionCache(to, Direction.withdraw, amount, BalanceType.token)
    queue.add(item)
    mint(to, tokenUnit.toUnit(amount))
    val user = userService.subtractFromTokenBalance(to, amount)
    queue.remove(item)
    return user
  }

  fun withdrawCoin(to: String, amount: BigDecimal): User {
    val item = TransactionCache(to, Direction.withdraw, amount, BalanceType.coin)
    queue.add(item)
    transferCoins(to, coinUnit.toUnit(amount))
    val user = userService.subtractFromBalance(to, amount)
    queue.remove(item)
    return user
  }

  private fun depositCoin(to: String, amount: BigDecimal) {
    val item = TransactionCache(to, Direction.deposit, amount, BalanceType.coin)
    queue.add(item)
    userService.addToBalance(to, coinUnit.toValue(amount))
    queue.remove(item)
  }

  //todo: Should retry in case of fail
  private fun mint(address: String, amount: BigInteger): String {
    val function = Function(
      "mint",
      listOf(org.web3j.abi.datatypes.Address(address), org.web3j.abi.datatypes.Uint(amount)),
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
      BigInteger.valueOf(10000000),
      appProperties.contractAddress,
      BigInteger.ZERO,
      encodedFunction
    )
    val gasLimit = client.ethEstimateGas(transactionForEstimate).send().amountUsed
    return FastRawTransactionManager(client, loadCreds())
      .sendTransaction(
        gasPrice,
        gasLimit,
        address,
        encodedFunction,
        BigInteger.ZERO,
      ).transactionHash
  }

  private fun transferCoins(address: String, amount: BigInteger): String {
    val gasPrice = client.ethGasPrice().send().gasPrice
    return FastRawTransactionManager(client, loadCreds())
      .sendTransaction(
        gasPrice,
        BigInteger.valueOf(21000),
        address,
        "",
        amount,
      ).transactionHash
  }

  private fun loadCreds() =
    WalletUtils.loadBip39Credentials("", privateKeyProvider.fetchPrivateKey())
}

