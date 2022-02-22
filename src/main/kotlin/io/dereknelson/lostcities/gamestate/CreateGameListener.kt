package io.dereknelson.lostcities.gamestate

import com.fasterxml.jackson.databind.ObjectMapper
import io.dereknelson.lostcities.gamestate.persistance.MatchEntity
import io.dereknelson.lostcities.gamestate.persistance.MatchRepository
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class CreateGameListener(
    val matchRepository: MatchRepository,
    val objectMapper: ObjectMapper
) {

    @Bean
    fun createGame(): Queue {
        return Queue("create-game",)
    }

    @RabbitListener(queues = ["create-game"])
    fun createGame(gameMessage: Message) {
        println("Message read from create-game: ${String(gameMessage.body)}\n\n\n\n\n\n\n\n\n\n\n\n")
        val match = objectMapper.readValue(gameMessage.body, MatchEntity::class.java)

        matchRepository.save(match)

        println("Saved match to repo")
    }
}
