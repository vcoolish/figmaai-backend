package com.app.drivn.backend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import org.springframework.web.cors.CorsConfiguration
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Validated
@ConfigurationProperties(prefix = "server.cors")
class CorsProperties {
    var isEnabled = true

    @Valid
    var configurations: MutableList<CorsConfigurationMapping>? = null

    /**
     * The type Cors configuration mapping.
     */
    class CorsConfigurationMapping {
        @NotBlank
        var path: String? = null

        @NotNull
        var configuration: CorsConfiguration? = null
    }
}