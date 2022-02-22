package io.dereknelson.lostcities.gamestate.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer

@Configuration
class RedisConfiguration {

    @Value("\${spring.redis.host}")
    private val host: String? = null

    @Value("\${spring.redis.port}")
    private val port = 0

    @Value("\${spring.redis.database}")
    private val database = 0

    @Bean
    fun jedisConnectionFactory(): JedisConnectionFactory {
        val configuration = RedisStandaloneConfiguration()
        configuration.hostName = host!!
        configuration.port = port
        configuration.database = database

        return JedisConnectionFactory(configuration)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(jedisConnectionFactory())
        template.setDefaultSerializer(Jackson2JsonRedisSerializer(Any::class.java))
        return template
    }
}
