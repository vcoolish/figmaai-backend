package com.app.drivn.backend.common.blockchain

import com.app.drivn.backend.common.util.logger
import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.user.model.User
import com.app.drivn.backend.user.service.UserService
import net.osslabz.evm.abi.decoder.AbiDecoder
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.tx.FastRawTransactionManager
import java.math.BigDecimal
import java.math.BigInteger
import java.net.http.WebSocket
import java.util.stream.Stream

//region Move to application.yml
const val ADMIN_ADDRESS = "0xF36f148D6FdEaCD6c765F8f59D4074109E311f0c"
const val CONTRACT_ADDRESS = "0xe418eE8ec1Bca66FFa7E088e4656Cc628661043d"
const val CLIENT_URL =
  "https://intensive-divine-mountain.bsc.discover.quiknode.pro/79d20c28b6e2219dd4c5e8e9599f6440a6627034/"
//endregion

@Service
class BlockchainService(
  private val userService: UserService,
  private val privateKeyProvider: PrivateKeyProvider,
  appProperties: AppProperties
) : WebSocket.Listener {

  @Volatile
  var isConnected: Boolean = false

  var onDisconnect: (code: Int, reason: String) -> Unit = { _, _ -> }

  val logger = logger()
  var erc20Decoder: AbiDecoder = AbiDecoder(appProperties.ercFile.inputStream)

  private val client: Web3j = Web3j.build(HttpService(CLIENT_URL))
  private val tokenUnit = com.app.drivn.backend.common.blockchain.entity.Unit(8, "DRIVE", "DRIVE")
  private val coinUnit = com.app.drivn.backend.common.blockchain.entity.Unit(18, "BNB", "BNB")

  @EventListener
  fun onApplicationStarted(event: ApplicationStartedEvent) {
    // todo: store start block in db
    val startBlock = (100500).toBigInteger()
    val endBlock = client.ethBlockNumber().send().blockNumber

    Stream.iterate(startBlock, { current -> current <= endBlock }, BigInteger::inc)
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

    var isPassedStart = false
    client.transactionFlowable().subscribe {
      if (isPassedStart) {
        logger.debug("New transaction")
        logger.debug("from" + it.from)
        logger.debug("to" + it.to)
        logger.debug(it.value.toString())
        processTx(it)
      } else {
        isPassedStart = it.blockNumber !in startBlock..endBlock
      }
    }
  }

  private fun processTx(tx: org.web3j.protocol.core.methods.response.Transaction) {
    try {
      if (tx.to.equals(ADMIN_ADDRESS, true)) {
        depositCoin(tx.from, tx.value.toBigDecimal())
      } else if (tx.to.equals(CONTRACT_ADDRESS, true)) {
        val call = erc20Decoder.decodeFunctionCall(tx.input)
        val name = call?.name
        val amount = BigDecimal(call.getParam("amount").value.toString())
        if (name == "burn") {
          depositToken(tx.from, amount)
        } else if (name == "burnFrom") {
          val account = call.getParam("account").value
          depositToken(account.toString(), amount)
        }
      }
    } catch (ignored: Throwable) { }
  }

  private fun depositToken(to: String, amount: BigDecimal) {
    //todo: validate transaction before
    userService.addToTokenBalance(to, tokenUnit.toValue(amount))
  }

  fun withdrawToken(to: String, amount: BigDecimal): User {
    mint(to, tokenUnit.toUnit(amount))
    return userService.subtractFromTokenBalance(to, amount)
  }

  fun withdrawCoin(to: String, amount: BigDecimal): User {
    transferCoins(to, coinUnit.toUnit(amount))
    return userService.subtractFromBalance(to, amount)
  }

  private fun depositCoin(to: String, amount: BigDecimal) {
    userService.addToBalance(to, coinUnit.toValue(amount))
  }

  //todo: Should retry in case of fail
  private fun mint(address: String, amount: BigInteger): String {
    val function = Function(
      "mint",
      listOf(org.web3j.abi.datatypes.Address(address), org.web3j.abi.datatypes.Uint(amount)),
      listOf(),
    )
    val encodedFunction = FunctionEncoder.encode(function)
    val creds = Credentials.create(privateKeyProvider.fetchPrivateKey())
    val gasPrice = client.ethGasPrice().send().gasPrice

    val nonce = client.ethGetTransactionCount(ADMIN_ADDRESS, DefaultBlockParameterName.LATEST)
      .send().transactionCount
    val transactionForEstimate = Transaction.createFunctionCallTransaction(
      ADMIN_ADDRESS,
      nonce,
      gasPrice,
      BigInteger.valueOf(10000000),
      CONTRACT_ADDRESS,
      BigInteger.ZERO,
      encodedFunction
    )
    val gasLimit = client.ethEstimateGas(transactionForEstimate).send().amountUsed
    return FastRawTransactionManager(client, creds)
      .sendTransaction(
        gasPrice,
        gasLimit,
        address,
        encodedFunction,
        BigInteger.ZERO,
      ).transactionHash
  }

  //todo: Should retry in case of fail
  private fun transferCoins(address: String, amount: BigInteger): String {
    val creds = Credentials.create(privateKeyProvider.fetchPrivateKey())
    val gasPrice = client.ethGasPrice().send().gasPrice
    return FastRawTransactionManager(client, creds)
      .sendTransaction(
        gasPrice,
        BigInteger.valueOf(21000),
        address,
        "",
        amount,
      ).transactionHash
  }
}
