package io.dereknelson.lostcities.gamestate.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer

class RedisConfiguration {

    @Value("\${spring.redis.host}")
    private val host: String? = null

    @Value("\${spring.redis.port}")
    private val port = 0

    @Value("\${spring.redis.password:#{null}}")
    private val password: String? = null

    @Value("\${spring.redis.database}")
    private val database = 0

    @Bean
    fun jedisConnectionFactory(): JedisConnectionFactory {
        val configuration = RedisStandaloneConfiguration()
        configuration.hostName = host!!
        configuration.port = port
        configuration.database = database

        if (password != null) {
            configuration.password = RedisPassword.of(password)
        }

        return JedisConnectionFactory(configuration)
    }

    @Bean
    fun redisTemplate(objectMapper: ObjectMapper): RedisTemplate<String, Any> {
        val serializer = Jackson2JsonRedisSerializer(objectMapper, Any::class.java)

        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(jedisConnectionFactory())
        template.setDefaultSerializer(serializer)

        return template
    }
}
