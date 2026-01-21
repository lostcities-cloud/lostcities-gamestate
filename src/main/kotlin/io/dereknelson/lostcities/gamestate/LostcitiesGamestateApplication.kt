package io.dereknelson.lostcities.gamestate

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.dereknelson.lostcities.common.WebConfigProperties
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

@SpringBootApplication(
    scanBasePackages = [
        "io.dereknelson.lostcities.gamestate",
        "io.dereknelson.lostcities.common",
    ],
)
@EnableConfigurationProperties(WebConfigProperties::class)
@EnableRabbit
@EnableRedisRepositories
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = false, jsr250Enabled = true)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
)
@OpenAPIDefinition(servers = [Server(url = "lostcities.com", )])
class LostcitiesGamestateApplication

fun main(args: Array<String>) {
    runApplication<LostcitiesGamestateApplication>(*args)
}

@Bean
fun mapper(): ObjectMapper =
    jacksonObjectMapper()
        .registerKotlinModule()
        .registerModule(Jdk8Module())
        .registerModule(JavaTimeModule())
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)!!
