package io.dereknelson.lostcities.gamestate.commandJob

import io.dereknelson.lostcities.gamestate.AiEvent
import io.dereknelson.lostcities.gamestate.api.GameService
import io.dereknelson.lostcities.gamestate.matches.CommandEntity
import io.dereknelson.lostcities.models.commands.CommandDto
import io.dereknelson.lostcities.models.commands.CommandType
import io.dereknelson.lostcities.models.state.Card
import mu.KotlinLogging
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Component
class AiProcessor : ApplicationListener<AiEvent> {
    private val logger: Log = LogFactory.getLog(this::class.java)
    @Autowired @Lazy
    private lateinit var gameService: GameService

    override fun onApplicationEvent(event: AiEvent) {
        val game = gameService.build(event.match)

        if (game.isGameOver()) {
            logger.info("Game Already Completed")
            return
        }

        var playOrDiscard: CommandDto?
        val draw = CommandDto(CommandType.DRAW, card=null, color=null, player=game.currentPlayer)

        val card: Card? = game.currentHand().firstOrNull {
            game.canPlayCard(game.currentPlayer, it.id)
        }

        if (card == null) {
            val discard = game.currentHand().first()
            playOrDiscard = CommandDto(CommandType.DISCARD, card=discard.id, color=null, player=game.currentPlayer)
        } else {
            playOrDiscard = CommandDto(CommandType.PLAY, card=card.id, color=null, player=game.currentPlayer)
        }

        try {
            gameService.play(game, playOrDiscard, game.currentPlayer)
            gameService.play(game, draw, game.currentPlayer)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }

        gameService.saveTurn(
            game,
            playOrDiscard.asEntity(game.currentPlayer),
            draw.asEntity(game.currentPlayer),
        )
    }

    private fun CommandDto.asEntity(user: String): CommandEntity {
        return CommandEntity(user, type, card, color, Instant.now().toEpochMilli())
    }
}
