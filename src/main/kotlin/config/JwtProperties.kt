package org.example.callulaserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "jwt")
class JwtProperties {
    var secret: String = ""
    var accessTokenExpiration: Long = 0
    var refreshTokenExpiration: Long = 0
}
