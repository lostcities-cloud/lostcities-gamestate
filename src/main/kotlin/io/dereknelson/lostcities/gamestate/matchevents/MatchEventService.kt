package io.dereknelson.lostcities.gamestate.matchevents

import com.fasterxml.jackson.databind.ObjectMapper
import io.dereknelson.lostcities.gamestate.game.PlayerViewDto
import io.dereknelson.lostcities.models.Color
import io.dereknelson.lostcities.models.matches.PlayerEvent
import io.dereknelson.lostcities.models.matches.PlayerEventType
import io.dereknelson.lostcities.models.matches.TurnChangeEvent
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component


@Component
class MatchEventService(
    private val objectMapper: ObjectMapper,
    private var rabbitTemplate: RabbitTemplate,
) {

    companion object {
        val TURN_CHANGE_EVENT = "turn-change-event"
    }

    fun sendTurnChangeEvent(id: Long, login: String) {
        rabbitTemplate.convertAndSend(TURN_CHANGE_EVENT,
            objectMapper.writeValueAsBytes(TurnChangeEvent(id, login))
        )
    }

    fun sendPlayerEvents(playerEvents: Map<String, PlayerViewDto>) {
        rabbitTemplate.convertAndSend("player-event",
            objectMapper.writeValueAsBytes(playerEvents)
        )
    }
}