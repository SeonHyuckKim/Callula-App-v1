package org.example.callulaserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "swagger")
class SwaggerProperties {
    var username: String = ""
    var password: String = ""
}
