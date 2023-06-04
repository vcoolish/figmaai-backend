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
  val lemonKey: String = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI5NGQ1OWNlZi1kYmI4LTRlYTUtYjE3OC1kMjU0MGZjZDY5MTkiLCJqdGkiOiJiNTY2ZjQ4N2RiMjExNjE1MGZjNDQzNTI3MDg5ZmRjZDg5ODI4YjEzMzVhYWFiZjU5Nzg5NzMzNGMxMjQ3ZTYwNThlZjdjNDk0Y2ZiZTllNCIsImlhdCI6MTY4NTg1MjIyMS44MjExNjQsIm5iZiI6MTY4NTg1MjIyMS44MjExNjcsImV4cCI6MTcxNzQ3NDYyMS44MDg2ODIsInN1YiI6IjcxNDQ5MCIsInNjb3BlcyI6W119.j-bPda0MW5ZhmgQIEhipSwbtRhV77N5t7U9gJDSTT8pJAOAWseZHqUxQcDvtphPkl_3R8-ed-QKxQiIXy6q6RXnM3fGKgDuLTv04n5jIHUs9cfuN7VhFTiuJwrRvcfmflU-iK1roVo76KpZmHtff7sM5OjJRNnP5HIUxQ9B0_F8MZWS_LQYgVfshlSnkc2gK2DNuSonHQHp31WvncI4IRpAHVgTqussB3KAQlusfEFsHCn2b8DIG53ZM97yT43S200BXmPp3wtruy8o2sLlFnLObOgYsIi6F4PZ0NwLKNedYRQ2VgS1nXTvHnPGGGMDil1pDC13TPIfmyx0SvjWpChwkCN4jCKAoYqIOFQFkGrtzLUfq8AiOhwY9HrnS7HIZU2F7t5omc2nLgGpAXvfh1-IfbpAHpo1ZX-f5qVKrtGqR5BqivpiAhf_11R13L6s9IQ6IFfnLY1gB5wXVScE-KEQoxhH9kU2dJlvuVdhem2DAM_UZMCJoBNaBTX7RXyCZ",
)
