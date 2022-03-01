package io.dereknelson.lostcities.gamestate

import com.fasterxml.jackson.databind.ObjectMapper
import io.dereknelson.lostcities.gamestate.game.GameService
import io.dereknelson.lostcities.gamestate.persistance.MatchEntity
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class CreateGameListener(
    val gameService: GameService,
    val objectMapper: ObjectMapper
) {
    companion object {
        const val CREATE_GAME_QUEUE = "create-game"
    }

    @Bean @Qualifier(CREATE_GAME_QUEUE)
    fun createGame() = Queue(CREATE_GAME_QUEUE)

    @RabbitListener(queues = [CREATE_GAME_QUEUE])
    fun createGame(gameMessage: Message) {
        println("Message read from create-game: ${String(gameMessage.body)}\n\n\n\n\n\n\n\n\n\n\n\n")
        val match = objectMapper.readValue(gameMessage.body, MatchEntity::class.java)
        val gameState = gameService.build(match)

        gameService.save(gameState)
        println("Saved match to repo")
    }
}
