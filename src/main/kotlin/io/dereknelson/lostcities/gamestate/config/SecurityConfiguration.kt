package io.dereknelson.lostcities.gamestate.config

import io.dereknelson.lostcities.common.AuthoritiesConstants
import io.dereknelson.lostcities.common.auth.JwtFilter
import io.dereknelson.lostcities.common.auth.TokenProvider
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.web.filter.ForwardedHeaderFilter

@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@SecurityScheme(
    name = "jwt_auth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer"
)
class SecurityConfiguration(
    private val tokenProvider: TokenProvider,
) {

    @Bean
    fun forwardedHeaderFilter(): ForwardedHeaderFilter? {
        return ForwardedHeaderFilter()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): DefaultSecurityFilterChain {
        http
            .cors()
            .and()
            .csrf()
            .disable()
            .headers()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
            .requestMatchers("/api/account/reset-password/init").permitAll()
            .requestMatchers("/api/account/reset-password/finish").permitAll()
            .requestMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .requestMatchers("/api/**").permitAll()
            .requestMatchers("/management/health").permitAll()
            .requestMatchers("/management/health/**").permitAll()
            .requestMatchers("/management/info").permitAll()
            .requestMatchers("/management/prometheus").permitAll()
            .requestMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .and().addFilterAfter(
                JwtFilter(tokenProvider), AnonymousAuthenticationFilter::class.java
            )
        http.headers().cacheControl()
        return http.build()!!
    }

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web: WebSecurity ->
            web
                .ignoring()
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                // .requestMatchers("/api/**")
                .requestMatchers("/app/**/*.{js,html}")
                .requestMatchers("/i18n/**")
                .requestMatchers("/content/**")
                .requestMatchers("/swagger-ui/**")
                .requestMatchers("/test/**")
        }
    }

    @Bean
    fun encoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
