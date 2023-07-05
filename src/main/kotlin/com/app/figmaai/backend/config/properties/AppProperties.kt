package com.app.figmaai.backend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import java.math.BigDecimal
import java.time.Duration
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Validated
@ConstructorBinding
@ConfigurationProperties("application")
data class AppProperties(

  @NotBlank
  val sigKey: String,

  @NotBlank
  val key: String,

  @NotBlank
  val dalleKey: String,

  @NotBlank
  val stableKey: String,

  @Positive
  val defaultUserEnergyLimit: BigDecimal = BigDecimal.valueOf(30),

  @Positive
  val defaultUserTokensLimitPerDay: BigDecimal,

  @NotNull
  val energyRenewRate: Duration,

  @NotNull
  val subscriptionValidationRate: Duration,

  @Positive
  val energyRenewPercent: BigDecimal = BigDecimal.valueOf(0.2),

  @NotBlank
  val arweaveUrl: String,

  @NotBlank
  val paypalId: String,

  @NotBlank
  val paypalSecret: String,

  @NotBlank
  val paypalUrl: String,

  @NotBlank
  val lemonUrl: String,

  @NotBlank
  val lemonKey: String,
)
//eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI5NGQ1OWNlZi1kYmI4LTRlYTUtYjE3OC1kMjU0MGZjZDY5MTkiLCJqdGkiOiI0YzU0OTA2NTYzNDMzMTU1MTEzOTcyZWZhOWJkM2NjMGMyODBjNWJlYjk2ZDg1ZjUxODU4ZjYzYTE2ZTczYmE4NGQ4YjEyNTUzMWU3NTQ5ZSIsImlhdCI6MTY4NTkzMjMzMC41OTU3NjksIm5iZiI6MTY4NTkzMjMzMC41OTU3NzIsImV4cCI6MTcxNzU1NDczMC41ODgxMzUsInN1YiI6IjcxNDQ5MCIsInNjb3BlcyI6W119.Ry_G_aSZlajTa-nHFcB8GjJR0AcNFtTrtGd1AUaTNFt0fBziTGYzm5zmmLKq1sZx5zV6H3-teCAchc1kJ39kf4rIxpiP6EyoemI3ED1DhGezmQ0FsRV0muvW2GW41kbtv1lbSXjSXEF-ovSTqr2H75Uv4uE5T_ds5VTgSvJ2AtIHUkjZn81Lb2xTSsFDHXXQuDl_uAIZJaMUr9QzGZG6prgJngrBUlolNXK0hlf8wuTalV8EO8HtBkqa4Z_VBuVqAPsgGUsRdSuckjipwH6Pd1gepXvEQLB6dgDav4Nht30VJEs8iFeaG4pJBMe7PlF42HN1FZIrZlcmm_OMCQoH4QAg55yRlUDWdj5MbaaFYAnPPmvxBRrzv8U8xjb9JB73q4wyJurITsShQPFNehcYkIB-RG17QMIYVZ-kwK0hLz6c8q8DgyX77LV7lupY2pD5sqptYmbB3SBhUKsQ4W2e5lfSsIXpE-QTplr3K5nqzmzdfWylo6q3QhdjfFYtzmWO