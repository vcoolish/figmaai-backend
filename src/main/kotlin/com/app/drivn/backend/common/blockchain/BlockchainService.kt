package com.app.drivn.backend.common.blockchain

import com.app.drivn.backend.common.util.logger
import com.app.drivn.backend.user.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.tx.FastRawTransactionManager
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.time.Duration
import java.util.concurrent.CompletionStage

const val ADMIN_ADDRESS = "0x0000000000000000000000000000000000001004"
const val CONTRACT_ADDRESS = "0xe418eE8ec1Bca66FFa7E088e4656Cc628661043d"
const val CLIENT_URL = "https://intensive-divine-mountain.bsc.discover.quiknode.pro/79d20c28b6e2219dd4c5e8e9599f6440a6627034/"
const val SOCKET_URL = "wss://intensive-divine-mountain.bsc.discover.quiknode.pro/79d20c28b6e2219dd4c5e8e9599f6440a6627034/"

@Service
class BlockchainService(
  private val userService: UserService,
  private val privateKeyProvider: PrivateKeyProvider,
) : WebSocket.Listener {

  private var socket: WebSocket = HttpClient.newHttpClient().newWebSocketBuilder()
    .connectTimeout(Duration.ofMinutes(1))
    .buildAsync(URI(SOCKET_URL), this)
    .get()
  private val client = Web3j.build(HttpService(CLIENT_URL))

  @Volatile
  var isConnected: Boolean = false

  var onDisconnect: (code: Int, reason: String) -> Unit = { _, _ ->  }
  val logger = logger()

  private fun subscribe() {
    val message = RpcRequest(
      id = 0,
      method = "eth_subscribe",
      params = listOf(
        "logs",
        mapOf(
          "fromBlock" to "earliest",
          "toBlock" to "latest",
          "address" to ADMIN_ADDRESS,
        )
      )
    )

    socket.sendText(ObjectMapper().writeValueAsString(message), true)
  }

  override fun onText(
    webSocket: WebSocket?,
    data: CharSequence?,
    last: Boolean
  ): CompletionStage<*> {
    logger.debug("<< websocket message >>")
    logger.debug(data.toString())
    //todo: parse transaction of address and do balance action
    return super.onText(webSocket, data, last)
  }

  override fun onClose(
    webSocket: WebSocket?,
    statusCode: Int,
    reason: String?
  ): CompletionStage<*> {
    logger.debug("<< websocket close >>")
    return super.onClose(webSocket, statusCode, reason)
  }

  override fun onOpen(webSocket: WebSocket?) {
    logger.debug("<< websocket opened >>")
    isConnected = true
    subscribe()
    super.onOpen(webSocket)
  }

  private fun depositToken(to: String, amount: BigDecimal) {
    //todo: validate transaction before
    userService.addToTokenBalance(to, amount)
  }

  private fun withdrawToken(to: String, amount: BigDecimal) {
    mint(to, amount.toBigInteger())
    userService.subtractFromTokenBalance(to, amount)
  }

  private fun depositCoin(to: String, amount: BigDecimal) {
    //todo: validate transaction before
    userService.addToBalance(to, amount)
  }

  private fun withdrawCoin(to: String, amount: BigDecimal) {
    transferCoins(to, amount.toBigInteger())
    userService.subtractFromBalance(to, amount)
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

    val nonce = client.ethGetTransactionCount(ADMIN_ADDRESS, DefaultBlockParameterName.LATEST).send().transactionCount
    val transactionForEstimate = Transaction.createFunctionCallTransaction(
      ADMIN_ADDRESS, nonce, gasPrice, BigInteger.valueOf(10000000), CONTRACT_ADDRESS, BigInteger.ZERO, encodedFunction)
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

data class RpcRequest<out T>(
  val id: Int,
  val jsonrpc: String = "2.0",
  val method: String,
  val params: List<T>
)