package com.app.figmaai.backend.chatgpt.wallet

import org.springframework.web.multipart.MultipartFile

class WalletRequestDto(
  val mode: PlayMode,
)

enum class PlayMode(val value: String, val title: String, val inputs: Map<String, String>) {
  play(
    """
      Your goal is to create translate request into JSON tree:
      "Rules:
      1. Create a JSON tree based on the provided information
      2. If ask you to do some action with cryptocurrency, coin, token or some asset, you should create a JSON tree with given information as in example
      {
        "type": "send", // can be send, buy, swap, receive, stake, open_coin, market
        "asset": "Bitcoin", // asset name, set empty string if not specified
        "to_asset": "Ethereum", // asset name to swap, set empty string if not specified, should be present only for swap
        "amount": "0.1", // amount of asset, set 0 if not specified, should be present only for transfer
        "is_fiat": "false" // is amount in fiat or not, false by default, should be present only for transfer
      }
      3. if I ask you to open some dapp or url you should create a JSON tree with given information as in example. Not applicable for asset actions.
      {
        "type": "open_url",
        "network": "ethereum", // only if network specified or set empty string
        "query": "website name", // if only website name or description specified
        "url": "https://www.google.com" // if url is specified
      }
      4. If I ask  you some question, you should create a JSON tree with given information as in example
      {
        "type": "question",
        "text": "Short answer to the question",
        "link": "Link to read more if present or empty"
      }
      5. If I ask you something else, you should create a JSON tree with given information as in example
      {
        "type": "error",
        "text": "I couldn't process the request, please try again"
      }
    """.trimIndent(),
    "Play",
    mapOf(
      "Goals/Objectives" to "Create AI support chat-bot as a SaaS application for businesses",
      "Metrics for Success" to "5000 active users by the end of the year",
      "Stakeholders" to "Business Owners, Clients of businesses",
    ),
  )
}