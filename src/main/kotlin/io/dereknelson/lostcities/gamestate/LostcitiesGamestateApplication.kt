package io.dereknelson.lostcities.gamestate

import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories


@SpringBootApplication(scanBasePackages = [
	"io.dereknelson.lostcities.gamestate",
	"io.dereknelson.lostcities.common.auth",
	"io.dereknelson.lostcities.common.library"
])
@EnableRabbit
@EnableRedisRepositories
class LostcitiesGamestateApplication

fun main(args: Array<String>) {
	runApplication<LostcitiesGamestateApplication>(*args)
}