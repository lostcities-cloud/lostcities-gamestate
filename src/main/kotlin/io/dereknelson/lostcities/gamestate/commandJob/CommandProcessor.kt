package io.dereknelson.lostcities.gamestate.commandJob

import io.dereknelson.lostcities.gamestate.AiEvent
import io.dereknelson.lostcities.gamestate.CommandEvent
import io.dereknelson.lostcities.gamestate.api.GameService
import io.dereknelson.lostcities.gamestate.matches.CommandEntity
import io.dereknelson.lostcities.models.commands.CommandDto
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Component
class CommandProcessor(
    private val gameService: GameService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : ApplicationListener<CommandEvent> {
    private val logger: Log = LogFactory.getLog(this::class.java)

    override fun onApplicationEvent(event: CommandEvent) {
        val game = gameService.build(event.match)
        val user = event.userDetails.login

        if (game.isGameOver()) {
            logger.info("Game Already Completed, Player($user) {${event.playOrDiscard}} {${event.draw}}")
        }

        try {
            gameService.play(game, event.playOrDiscard, user)
            gameService.play(game, event.draw, user)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }

        logger.info("This is a test")
        logger.info("This is a test")
        logger.info("This is a test")

        gameService.saveTurn(
            game,
            event.playOrDiscard.asEntity(user),
            event.draw.asEntity(user),
        )

        if (game.isCurrentPlayerAi()) {
            applicationEventPublisher.publishEvent(AiEvent(game.matchEntity))
        }
    }

    private fun CommandDto.asEntity(user: String): CommandEntity {
        return CommandEntity(user, type, card, color, Instant.now().toEpochMilli())
    }
}
