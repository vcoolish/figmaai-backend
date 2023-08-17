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
      5. Partial list of supported assets, but expect other assets to be supported as well:
      1. name = Acala, symbol = ACA
      2. name = Acala EVM, symbol = ACA
      3. name = Aeternity, symbol = AE
      4. name = Agoric, symbol = BLD
      5. name = Aion, symbol = AION
      6. name = Akash, symbol = AKT
      7. name = Algorand, symbol = ALGO
      8. name = Aptos, symbol = APT
      9. name = Arbitrum, symbol = ETH
      10. name = Cosmos Hub, symbol = ATOM
      11. name = Aurora, symbol = ETH
      12. name = Avalanche C-Chain, symbol = AVAX
      13. name = Axelar, symbol = AXL
      14. name = Base, symbol = ETH
      15. name = BNB Beacon Chain, symbol = BNB
      16. name = Bitcoin, symbol = BTC
      17. name = Bitcoin Cash, symbol = BCH
      18. name = Callisto, symbol = CLO
      19. name = Cardano, symbol = ADA
      20. name = Celo, symbol = CELO
      21. name = Conflux eSpace, symbol = CFX
      22. name = Cronos Chain, symbol = CRO
      23. name = Crypto.org, symbol = CRO
      24. name = Dash, symbol = DASH
      25. name = Decred, symbol = DCR
      26. name = DigiByte, symbol = DGB
      27. name = Dogecoin, symbol = DOGE
      28. name = Ethereum, symbol = ETH
      29. name = Ethereum Classic, symbol = ETC
      30. name = Evmos, symbol = EVMOS
      31. name = Fantom, symbol = FTM
      32. name = Filecoin, symbol = FIL
      33. name = FIO, symbol = FIO
      34. name = Firo, symbol = FIRO
      35. name = Flux, symbol = FLUX
      36. name = GoChain, symbol = GO
      37. name = Groestlcoin, symbol = GRS
      38. name = Harmony, symbol = ONE
      39. name = Huobi ECO Chain, symbol = HT
      40. name = ICON, symbol = ICX
      41. name = Native Injective, symbol = INJ
      42. name = IoTeX, symbol = IOTX
      43. name = IoTeX EVM, symbol = IOTX
      44. name = Juno, symbol = JUNO
      45. name = Kava, symbol = KAVA
      46. name = Klaytn, symbol = KLAY
      47. name = KuCoin Community Chain, symbol = KCS
      48. name = Kusama, symbol = KSM
      49. name = Litecoin, symbol = LTC
      50. name = Polygon, symbol = MATIC
      51. name = Moonbeam, symbol = GLMR
      52. name = Moonriver, symbol = MOVR
      53. name = MultiversX, symbol = eGLD
      54. name = Nano, symbol = XNO
      55. name = Native Evmos, symbol = EVMOS
      56. name = NEAR, symbol = NEAR
      57. name = Nebulas, symbol = NAS
      58. name = Neutron, symbol = NTRN
      59. name = Nimiq, symbol = NIM
      60. name = Ontology, symbol = ONT
      61. name = OpBNB, symbol = BNB
      62. name = Optimism Ethereum, symbol = ETH
      63. name = Osmosis, symbol = OSMO
      64. name = Polkadot, symbol = DOT
      65. name = Qtum, symbol = QTUM
      66. name = Ravencoin, symbol = RVN
      67. name = XRP, symbol = XRP
      68. name = Ronin, symbol = RON
      69. name = Sei, symbol = SEI
      70. name = BNB Smart Chain, symbol = BNB
      71. name = Smart Chain Legacy, symbol = BNB
      72. name = Solana, symbol = SOL
      73. name = Stargaze, symbol = STARS
      74. name = Stellar, symbol = XLM
      75. name = Stride, symbol = STRD
      76. name = Sui, symbol = SUI
      77. name = Terra Classic, symbol = LUNC
      78. name = Tezos, symbol = XTZ
      79. name = Theta, symbol = THETA
      80. name = THORChain, symbol = RUNE
      81. name = ThunderCore, symbol = TT
      82. name = TomoChain, symbol = TOMO
      83. name = TON, symbol = TON
      84. name = Tron, symbol = TRX
      85. name = VeChain, symbol = VET
      86. name = Viacoin, symbol = VIA
      87. name = Wanchain, symbol = WAN
      88. name = Waves, symbol = WAVES
      89. name = Gnosis Chain, symbol = xDAI
      90. name = Zcash, symbol = ZEC
      91. name = Zilliqa, symbol = ZIL
      92. name = Polygon zkEVM, symbol = ETH
      93. name = zkSync Era, symbol = ETH
    """.trimIndent(),
    "Play",
    mapOf(
      "Goals/Objectives" to "Create AI support chat-bot as a SaaS application for businesses",
      "Metrics for Success" to "5000 active users by the end of the year",
      "Stakeholders" to "Business Owners, Clients of businesses",
    ),
  )
}