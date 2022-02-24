package io.dereknelson.lostcities.gamestate.config

import io.dereknelson.lostcities.gamestate.matchevents.MatchEventService
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class RabbitConfiguration(
    template: RabbitTemplate
) {
    @get:Bean
    val admin: RabbitAdmin = RabbitAdmin(template)

    val exchange: String = ""

    @PostConstruct
    fun initialize() {
        try {
            val turnChangeQueue = Queue(
                MatchEventService.TURN_CHANGE_EVENT,
                true,
                false,
                false

            )
            val turnChangeBinding = Binding(
                MatchEventService.TURN_CHANGE_EVENT,
                Binding.DestinationType.QUEUE,
                exchange,
                "",
                null
            )
            admin.declareQueue(turnChangeQueue)
            admin.declareBinding(turnChangeBinding)

            val playerEventQueue = Queue(
                MatchEventService.PLAYER_EVENT,
                true,
                false,
                false
            )
            val playerEventBinding = Binding(
                MatchEventService.PLAYER_EVENT,
                Binding.DestinationType.QUEUE,
                exchange,
                "",
                null
            )

            admin.declareQueue(playerEventQueue)
            admin.declareBinding(playerEventBinding)

            val gameOverQueue = Queue(
                MatchEventService.END_GAME_EVENT,
                true,
                false,
                false
            )
            val gameOverBinding = Binding(
                MatchEventService.END_GAME_EVENT,
                Binding.DestinationType.QUEUE,
                exchange,
                "",
                null
            )

            admin.declareQueue(gameOverQueue)
            admin.declareBinding(gameOverBinding)
        } catch (e: Exception) {
            println("Alright.")
        }
    }
}
