package org.example.callulaserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "http-client")
class HttpClientProperties(
    var connectTimeout: Long = 10,
    var readTimeout: Long = 30,
    var writeTimeout: Long = 30,
    var maxIdleConnections: Int = 5,
    var keepAliveDuration: Long = 5,
    var retryOnConnectionFailure: Boolean = true,
    var loggingLevel: String = "BASIC",
)
