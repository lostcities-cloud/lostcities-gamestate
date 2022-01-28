package io.dereknelson.lostcities.gamestate.matchevents

import io.dereknelson.lostcities.models.matches.TurnChangeEvent
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component


@Component
class MatchEventService(
    private var rabbitTemplate: RabbitTemplate,
) {
    companion object {
        val TURN_CHANGE_EVENT = "turn-change-event"
    }

    public fun sendTurnChangeEvent(id: Long, login: String) {
        rabbitTemplate.convertAndSend(TURN_CHANGE_EVENT, TurnChangeEvent(id, login))
    }
}