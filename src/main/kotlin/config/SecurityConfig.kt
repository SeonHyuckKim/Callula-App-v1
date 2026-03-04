package org.example.callulaserver.config

import org.example.callulaserver.common.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val swaggerProperties: SwaggerProperties,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun swaggerUserDetailsService(
        passwordEncoder: PasswordEncoder,
    ): InMemoryUserDetailsManager {
        val user = User.builder()
            .username(swaggerProperties.username)
            .password(passwordEncoder.encode(swaggerProperties.password))
            .roles("SWAGGER")
            .build()

        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        swaggerUserDetailsService: InMemoryUserDetailsManager,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    // Swagger
                    .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                    ).permitAll()

                    // 공개 API만
                    .requestMatchers(
                        "/api/users/v1/signup",
                        "/api/auth/v1/login",
                        "/favicon.ico"
                    ).permitAll()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().authenticated()
            }
            .anonymous { it.disable() }
            .sessionManagement { session ->
                session.sessionFixation().migrateSession()
            }
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
            .httpBasic(Customizer.withDefaults())
            .userDetailsService(swaggerUserDetailsService)

        println("Security FilterChain loaded")

        return http.build()
    }
}
