package io.dereknelson.lostcities.gamestate.commandJob

import io.dereknelson.lostcities.gamestate.CommandEvent
import io.dereknelson.lostcities.gamestate.gamestate.GameService
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationListener
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class CommandProcessor(
    private val gameService: GameService,
) : ApplicationListener<CommandEvent> {
    private val logger: Log = LogFactory.getLog(this::class.java)

    override fun onApplicationEvent(event: CommandEvent) {
        val game = gameService.build(event.match)
        val user = event.userDetails.login

        if (game.isGameOver()) {
            logger.info("Game Already Completed, Player($user) {${event.playOrDiscard}} {${event.draw}}")
            return
        }

        try {
            gameService.play(game, event.playOrDiscard, user)
            gameService.play(game, event.draw, user)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }

        gameService.saveTurn(
            game,
            event.playOrDiscard,
            event.draw,
        )
    }
}
