package io.dereknelson.lostcities.gamestate.config

import io.dereknelson.lostcities.common.AuthoritiesConstants
import io.dereknelson.lostcities.common.WebConfigProperties
import io.dereknelson.lostcities.common.auth.JwtFilter
import io.dereknelson.lostcities.common.auth.TokenProvider
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
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher
import org.springframework.web.filter.ForwardedHeaderFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = false, jsr250Enabled = true)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
class SecurityConfiguration(
    private val tokenProvider: TokenProvider,
) {

    @Bean
    fun forwardedHeaderFilter(): ForwardedHeaderFilter? {
        return ForwardedHeaderFilter()
    }

    @Bean
    fun corsMappingConfigurer(webConfigProperties: WebConfigProperties): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                val cors: WebConfigProperties.Cors = webConfigProperties.cors
                registry.addMapping("/**")
                    .allowedOrigins(*cors.allowedOrigins)
                    .allowedMethods(*cors.allowedMethods)
                    .maxAge(cors.maxAge)
                    .allowedHeaders(*cors.allowedHeaders)
                    .exposedHeaders(*cors.exposedHeaders)
            }
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): DefaultSecurityFilterChain {
        http.csrf { it.disable() }
            .cors { it.disable() } // configure(http) }
            .addFilterBefore(JwtFilter(tokenProvider), UsernamePasswordAuthenticationFilter::class.java)
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
                    .requestMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(antMatcher("/gamestate/**")).hasAuthority(AuthoritiesConstants.USER)
                    .requestMatchers(
                        "/actuator",
                        "/actuator/**")
                    .permitAll()
                    .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
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
                .requestMatchers(antMatcher(HttpMethod.OPTIONS, "/**"))
                // .requestMatchers("/api/**")
                // .requestMatchers("/app/**/*.{js,html}")
                .requestMatchers("/actuator/**")
                .requestMatchers(antMatcher(HttpMethod.GET, "/actuator/**"))
                .requestMatchers("/i18n/**")
                .requestMatchers("/content/**")
                .requestMatchers("/swagger-ui/**")
                .requestMatchers(antMatcher("/v3/api-docs/**"))
                .requestMatchers("/v3/api-docs/swagger-config")
                .requestMatchers("/test/**")
        }
    }

    @Bean
    fun encoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
