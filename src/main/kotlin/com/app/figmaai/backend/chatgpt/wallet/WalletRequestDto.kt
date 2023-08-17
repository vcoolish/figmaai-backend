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
      2. If ask you to do some action with cryptocurrency, coin, token or some asset, you should create a JSON tree with given information as in example.
      set market type if I ask you about asset price or market info, or graph
      set send type if I ask you to send or transfer some asset to someone
      set buy type if I ask you to buy some asset
      set swap type if I ask you to swap or exchange some asset to another
      set receive type if I ask you to receive some asset or show address or QR code
      set stake type if I ask you to stake, delegate, unstake, claim rewards, redelegate or earn some asset
      set open type if I ask you to open some asset
      
      {
        "type": "send", // can be send, buy, swap, receive, stake, market or open if not specified
        "asset": "Bitcoin", // asset name, set empty string if not specified
        "recipient": "vitalik", // recipient, only applicable for send
        "to_asset": "Ethereum", // asset name to swap, set empty string if not specified, should be present only for swap
        "amount": "0.1", // amount of asset, set 0 if not specified, should be present only for transfer
        "is_fiat": "false" // is amount in fiat or not, false by default, should be present only for transfer
      }
      3. If I ask  you some question, you should create a JSON tree with given information as in example and only answer information related to Trust Wallet application and what you can do with this app
      {
        "type": "question",
        "text": "Short answer to the question",
        "link": "Link to read more if present or empty"
      }
      4. If I ask you something else, you should create a JSON tree with given information as in example
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