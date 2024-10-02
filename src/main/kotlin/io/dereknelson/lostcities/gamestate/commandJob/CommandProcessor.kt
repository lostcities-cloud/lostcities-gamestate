package io.dereknelson.lostcities.gamestate.commandJob

import io.dereknelson.lostcities.gamestate.api.GameService
import io.dereknelson.lostcities.gamestate.matches.CommandEntity
import io.dereknelson.lostcities.models.commands.CommandDto
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Component
class CommandProcessor : ApplicationListener<CommandEvent> {

    @Autowired @Lazy
    private lateinit var gameService: GameService

    override fun onApplicationEvent(event: CommandEvent) {
        val game = gameService.build(event.match)
        val user = event.userDetails.login

        if (game.isGameOver()) {
            logger.info(
                "Game Already Completed, Player($user) ",
                "{${event.playOrDiscard}} {${event.draw}}"
            )
        }

        gameService.play(game, event.playOrDiscard, user)
        gameService.play(game, event.draw, user)

        logger.info("This is a test")
        logger.info("This is a test")
        logger.info("This is a test")

        gameService.saveTurn(
            game,
            event.playOrDiscard.asEntity(user),
            event.draw.asEntity(user)
        )
    }

    private fun CommandDto.asEntity(user: String): CommandEntity {
        return CommandEntity(user, type, card, color, Instant.now().toEpochMilli())
    }
}
