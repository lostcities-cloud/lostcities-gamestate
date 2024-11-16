package io.dereknelson.lostcities.gamestate.gamestate

import com.fasterxml.jackson.databind.ObjectMapper
import io.dereknelson.lostcities.models.commands.CommandError
import io.dereknelson.lostcities.models.matches.FinishMatchEvent
import io.dereknelson.lostcities.models.state.PlayerViewDto
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

@Component
class GameEventService(
    private val objectMapper: ObjectMapper,
    private var rabbitTemplate: RabbitTemplate,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val TURN_CHANGE_EVENT = "turn-change"
        const val TURN_CHANGE_EVENT_DLQ = "turn-change-dlq"
        const val PLAYER_EVENT = "player-event"
        const val PLAYER_EVENT_DLQ = "player-event-dlq"
        const val END_GAME_EVENT = "end-game-event"
        const val END_GAME_EVENT_DLQ = "end-game-event-dlq"
        const val CREATE_GAME_QUEUE = "create-game"
        const val CREATE_GAME_QUEUE_DLQ = "create-game-dlq"
        const val COMMAND_ERROR_QUEUE = "command-error"
        const val COMMAND_ERROR_QUEUE_DLQ = "command-error-dlq"
        const val AI_PLAYER_REQUEST_EVENT = "ai-player-request-event"
        const val AI_PLAYER_REQUEST_EVENT_DLQ = "ai-player-event-request-dlq"
    }

    @Bean
    @Qualifier(COMMAND_ERROR_QUEUE)
    fun commandError() = QueueBuilder
        .durable(COMMAND_ERROR_QUEUE)
        .quorum()
        .ttl(5000)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", COMMAND_ERROR_QUEUE_DLQ)
        .build()!!

    @Bean
    @Qualifier(COMMAND_ERROR_QUEUE_DLQ)
    fun commandErrorDlQueue() = QueueBuilder
        .durable(COMMAND_ERROR_QUEUE_DLQ)
        .quorum()
        .build()!!

    @Bean
    @Qualifier(CREATE_GAME_QUEUE)
    fun createGame() = QueueBuilder
        .durable(CREATE_GAME_QUEUE)
        .quorum()
        .ttl(5000)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", CREATE_GAME_QUEUE_DLQ)
        .build()!!

    @Bean
    @Qualifier(CREATE_GAME_QUEUE_DLQ)
    fun createGameDlQueue() = QueueBuilder
        .durable(CREATE_GAME_QUEUE_DLQ)
        .quorum()
        .build()!!

    @Bean
    @Qualifier(TURN_CHANGE_EVENT)
    fun turnChangeEventQueue() = QueueBuilder
        .durable(TURN_CHANGE_EVENT)
        .quorum()
        .ttl(5000)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", TURN_CHANGE_EVENT_DLQ)
        .build()!!

    @Bean
    @Qualifier(TURN_CHANGE_EVENT_DLQ)
    fun turnChangeEventDlQueue() = QueueBuilder
        .durable(TURN_CHANGE_EVENT_DLQ)
        .quorum()
        .build()!!

    @Bean
    @Qualifier(PLAYER_EVENT)
    fun playerEventQueue() = QueueBuilder
        .durable(PLAYER_EVENT)
        .quorum()
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", PLAYER_EVENT_DLQ)
        .ttl(5000)
        .build()!!

    @Bean
    @Qualifier(PLAYER_EVENT_DLQ)
    fun playerEventDlQueue() = QueueBuilder
        .durable(PLAYER_EVENT_DLQ)
        .quorum()
        .build()!!

    @Bean
    @Qualifier(END_GAME_EVENT)
    fun endGameEventQueue() = QueueBuilder
        .durable(END_GAME_EVENT)
        .quorum()
        .ttl(5000)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", END_GAME_EVENT_DLQ)
        .build()!!

    @Bean
    @Qualifier(END_GAME_EVENT_DLQ)
    fun endGameEventDlQueue() = QueueBuilder
        .durable(END_GAME_EVENT_DLQ)
        .quorum()
        .build()!!

    @Bean
    @Qualifier(AI_PLAYER_REQUEST_EVENT)
    fun aiPlayerRequestEventQueue() = QueueBuilder
        .durable(AI_PLAYER_REQUEST_EVENT)
        .quorum()
        .ttl(5000)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", AI_PLAYER_REQUEST_EVENT_DLQ)
        .build()!!

    @Bean
    @Qualifier(AI_PLAYER_REQUEST_EVENT_DLQ)
    fun aiPlayerRequestEventQueueDlq() = QueueBuilder
        .durable(AI_PLAYER_REQUEST_EVENT_DLQ)
        .quorum()
        .build()!!

    fun sendCommandError(error: CommandError) {
        rabbitTemplate.convertAndSend(
            COMMAND_ERROR_QUEUE,
            objectMapper.writeValueAsBytes(error),
        )
    }

    fun sendPlayerEvents(playerEvents: Map<String, PlayerViewDto>) {
        rabbitTemplate.convertAndSend(
            PLAYER_EVENT,
            objectMapper.writeValueAsBytes(playerEvents),
        )
    }

    fun endGame(id: Long, scores: Map<String, Int>) {
        val event = FinishMatchEvent(
            id,
            scores,
            LocalDateTime.now(UTC),
        )

        logger.info("Finished Match: $event")

        rabbitTemplate.convertAndSend(
            END_GAME_EVENT,
            objectMapper.writeValueAsBytes(event),
        )
    }
}
