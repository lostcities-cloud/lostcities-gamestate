package io.dereknelson.lostcities.gamestate.matchevents

import com.fasterxml.jackson.databind.ObjectMapper
import io.dereknelson.lostcities.gamestate.CreateGameListener
import io.dereknelson.lostcities.gamestate.game.PlayerViewDto
import io.dereknelson.lostcities.models.matches.FinishMatchEvent
import io.dereknelson.lostcities.models.matches.TurnChangeEvent
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import java.util.TimeZone

@Component
class MatchEventService(
    private val objectMapper: ObjectMapper,
    private var rabbitTemplate: RabbitTemplate,
) {

    companion object {
        const val TURN_CHANGE_EVENT = "turn-change-event"
        const val PLAYER_EVENT = "player-event"
        const val END_GAME_EVENT = "end-game-event"
    }

    @Bean @Qualifier(TURN_CHANGE_EVENT)
    fun turnChangeEventQueue() = Queue(TURN_CHANGE_EVENT)

    @Bean @Qualifier(PLAYER_EVENT)
    fun playerEventQueue() = Queue(PLAYER_EVENT)

    @Bean @Qualifier(END_GAME_EVENT)
    fun endGameEventQueue() = Queue(END_GAME_EVENT)

    fun sendTurnChangeEvent(id: Long, login: String) {
        rabbitTemplate.convertAndSend(
            TURN_CHANGE_EVENT,
            objectMapper.writeValueAsBytes(TurnChangeEvent(id, login))
        )
    }

    fun sendPlayerEvents(playerEvents: Map<String, PlayerViewDto>) {
        rabbitTemplate.convertAndSend(
            PLAYER_EVENT,
            objectMapper.writeValueAsBytes(playerEvents)
        )
    }

    fun endGame(id:Long, scores: Map<String, Int>) {
        val event = FinishMatchEvent(
            id,
            scores,
            LocalDateTime.now(UTC)
        )
        rabbitTemplate.convertAndSend(
            END_GAME_EVENT,
            objectMapper.writeValueAsBytes(event)
        )
    }
}
