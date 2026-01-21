package io.dereknelson.lostcities.gamestate.config

import io.dereknelson.lostcities.common.WebConfigProperties
import io.dereknelson.lostcities.common.auth.JwtFilter
import io.dereknelson.lostcities.common.auth.PublicTokenValidator
import io.dereknelson.lostcities.common.model.Role
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.web.filter.ForwardedHeaderFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebSecurity(debug = true)

class SecurityConfiguration(
    private val publicTokenValidator: PublicTokenValidator
) {

    @Bean
    fun forwardedHeaderFilter(): ForwardedHeaderFilter? {
        return ForwardedHeaderFilter()
    }

    @Bean
    fun corsMappingConfigurer(webConfigProperties: WebConfigProperties): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                val cors: WebConfigProperties = webConfigProperties
                registry.addMapping("/**")
                    .allowedOrigins(*cors.allowedOrigins.toTypedArray())
                    .allowedMethods(*cors.allowedMethods.toTypedArray())
                    .maxAge(cors.maxAge)
                    .allowedHeaders(*cors.allowedHeaders.toTypedArray())
                    .exposedHeaders(*cors.exposedHeaders.toTypedArray())
            }
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): DefaultSecurityFilterChain {
        http.csrf { it.disable() }
            .cors { it.disable() } // configure(http) }
            .addFilterBefore(JwtFilter(publicTokenValidator), UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling {}
            .headers { headersConfigurer ->
                headersConfigurer.contentSecurityPolicy {
                    it.policyDirectives(
                        "default-src 'self' localhost:* ws://localhost:*;",
                    )
                }
                headersConfigurer.referrerPolicy {
                    it.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                }.cacheControl { }
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/api/admin/**").hasAuthority(Role.ADMIN.authority)
                    .requestMatchers("/gamestate/**").hasAuthority(Role.USER.authority)
                    .requestMatchers(
                        "/actuator",
                        "/actuator/**",
                    )
                    .permitAll()
                    .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                    ).permitAll()
                    .requestMatchers("/v3/api-docs/swagger-config").permitAll()
                // .requestMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
            }

        return http.build()!!
    }

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web: WebSecurity ->
            web
                .ignoring()
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                // .requestMatchers("/api/**")
                // .requestMatchers("/app/**/*.{js,html}")
                .requestMatchers("/actuator/**")
                .requestMatchers(HttpMethod.GET, "/actuator/**")
                .requestMatchers("/i18n/**")
                .requestMatchers("/content/**")
                .requestMatchers("/swagger-ui/**")
                .requestMatchers("/v3/api-docs/**")
                .requestMatchers("/v3/api-docs/swagger-config")
                .requestMatchers("/test/**")
        }
    }

    @Bean
    fun encoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
