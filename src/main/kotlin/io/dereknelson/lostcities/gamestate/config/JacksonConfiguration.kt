package io.dereknelson.lostcities.gamestate.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfiguration {

    @Autowired
    fun updateMapper(objectMapper: ObjectMapper) {
        objectMapper
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerKotlinModule()
    }

    @Bean
    fun javaTimeModule(): JavaTimeModule {
        return JavaTimeModule()
    }

    @Bean
    fun jdk8TimeModule(): Jdk8Module {
        return Jdk8Module()
    }
}
