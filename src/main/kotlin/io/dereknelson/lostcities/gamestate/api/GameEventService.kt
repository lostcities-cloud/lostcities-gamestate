package io.dereknelson.lostcities.gamestate.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.dereknelson.lostcities.gamestate.matches.MatchEntity
import io.dereknelson.lostcities.models.commands.CommandError
import io.dereknelson.lostcities.models.matches.FinishMatchEvent
import io.dereknelson.lostcities.models.matches.TurnChangeEvent
import mu.KotlinLogging
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

private val logger = KotlinLogging.logger {}

@Component
class GameEventService(
    private val objectMapper: ObjectMapper,
    private var rabbitTemplate: RabbitTemplate,
) {
    @Autowired @Lazy
    private lateinit var gameService: GameService

    companion object {
        const val TURN_CHANGE_EVENT = "turn-change-event"
        const val TURN_CHANGE_EVENT_DLQ = "turn-change-event-dlq"
        const val PLAYER_EVENT = "player-event"
        const val PLAYER_EVENT_DLQ = "player-event-dlq"
        const val END_GAME_EVENT = "end-game-event"
        const val END_GAME_EVENT_DLQ = "end-game-event-dlq"
        const val CREATE_GAME_QUEUE = "create-game"
        const val CREATE_GAME_QUEUE_DLQ = "create-game-dlq"
        const val COMMAND_ERROR_QUEUE = "command-error-event"
        const val COMMAND_ERROR_QUEUE_DLQ = "command-error-event-dlq"
    }

    @Bean
    @Qualifier(COMMAND_ERROR_QUEUE)
    fun commandError() = QueueBuilder
        .durable(COMMAND_ERROR_QUEUE)
        .ttl(5000)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", COMMAND_ERROR_QUEUE_DLQ)
        .build()!!

    @Bean
    @Qualifier(COMMAND_ERROR_QUEUE_DLQ)
    fun commandErrorDlQueue() = QueueBuilder
        .durable(COMMAND_ERROR_QUEUE_DLQ)
        .build()!!

    @Bean
    @Qualifier(CREATE_GAME_QUEUE)
    fun createGame() = QueueBuilder
        .durable(CREATE_GAME_QUEUE)
        .ttl(5000)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", CREATE_GAME_QUEUE_DLQ)
        .build()!!

    @Bean
    @Qualifier(CREATE_GAME_QUEUE_DLQ)
    fun createGameDlQueue() = QueueBuilder
        .durable(CREATE_GAME_QUEUE_DLQ)
        .build()!!

    @Bean
    @Qualifier(TURN_CHANGE_EVENT)
    fun turnChangeEventQueue() = QueueBuilder
        .durable(TURN_CHANGE_EVENT)
        .ttl(5000)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", TURN_CHANGE_EVENT_DLQ)
        .build()!!

    @Bean
    @Qualifier(TURN_CHANGE_EVENT_DLQ)
    fun turnChangeEventDlQueue() = QueueBuilder
        .durable(TURN_CHANGE_EVENT_DLQ)
        .build()!!

    @Bean
    @Qualifier(PLAYER_EVENT)
    fun playerEventQueue() = QueueBuilder
        .durable(PLAYER_EVENT)
        .ttl(5000)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", PLAYER_EVENT_DLQ)
        .build()!!

    @Bean
    @Qualifier(PLAYER_EVENT_DLQ)
    fun playerEventDlQueue() = QueueBuilder
        .durable(PLAYER_EVENT_DLQ)
        .build()!!

    @Bean
    @Qualifier(END_GAME_EVENT)
    fun endGameEventQueue() = QueueBuilder
        .durable(END_GAME_EVENT)
        .ttl(5000)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", END_GAME_EVENT_DLQ)
        .build()!!

    @Bean
    @Qualifier(END_GAME_EVENT_DLQ)
    fun endGameEventDlQueue() = QueueBuilder
        .durable(END_GAME_EVENT_DLQ)
        .build()!!

    fun sendCommandError(error: CommandError) {
        rabbitTemplate.convertAndSend(
            COMMAND_ERROR_QUEUE,
            objectMapper.writeValueAsBytes(error),
        )
    }

    fun sendTurnChangeEvent(id: Long, login: String) {
        rabbitTemplate.convertAndSend(
            TURN_CHANGE_EVENT,
            objectMapper.writeValueAsBytes(TurnChangeEvent(id, login)),
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

    @RabbitListener(queues = [CREATE_GAME_QUEUE])
    fun createGame(gameMessage: Message) {
        val match = objectMapper.readValue(gameMessage.body, MatchEntity::class.java)

        logger.info("Create Match[${match.id}]: ${String(gameMessage.body)}")

        if (gameService.saveNewMatch(match) != null) {
            logger.info("Match[${match.id}] saved match to repo")
        } else {
            logger.info("Match[${match.id}] already created")
        }
    }
}
