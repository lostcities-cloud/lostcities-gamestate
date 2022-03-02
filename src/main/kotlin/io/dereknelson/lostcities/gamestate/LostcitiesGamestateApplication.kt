package io.dereknelson.lostcities.gamestate

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@SpringBootApplication(
    scanBasePackages = [
        "io.dereknelson.lostcities.gamestate",
        "io.dereknelson.lostcities.common.auth",
        "io.dereknelson.lostcities.common.library",
    ]
)
@EnableRabbit
@EnableRedisRepositories
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
