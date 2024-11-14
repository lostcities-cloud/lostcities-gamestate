package io.dereknelson.lostcities.gamestate.commandJob

import com.fasterxml.jackson.databind.ObjectMapper
import io.dereknelson.lostcities.gamestate.api.GameEventService.Companion.AI_PLAYER_REQUEST_EVENT
import io.dereknelson.lostcities.gamestate.api.GameService
import io.dereknelson.lostcities.gamestate.matches.CommandEntity
import io.dereknelson.lostcities.gamestate.matches.MatchEntity
import io.dereknelson.lostcities.models.commands.CommandDto
import io.dereknelson.lostcities.models.commands.CommandType
import io.dereknelson.lostcities.models.state.Card
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Component
class AiProcessor(
    val objectMapper: ObjectMapper,
) {
    private val logger: Log = LogFactory.getLog(this::class.java)

    @Autowired @Lazy
    private lateinit var gameService: GameService

    @RabbitListener(queues = [AI_PLAYER_REQUEST_EVENT])
    fun playAiTurn(gameMessage: Message) {
        val match = objectMapper.readValue(gameMessage.body, MatchEntity::class.java)

        logger.info("GAME=${match.id} PLAYER=${match.currentPlayer} Starting AI turn")
        val game = gameService.build(match)

        if (game.isGameOver()) {
            logger.info("GAME=${match.id} PLAYER=${match.currentPlayer} Game Already Completed")
            return
        }

        if (!game.isCurrentPlayerAi()) {
            logger.info("GAME=${match.id} PLAYER=${match.currentPlayer} Current player is not an AI Player")
        }

        var playOrDiscard: CommandDto
        val draw = CommandDto(CommandType.DRAW, card = null, color = null, player = game.currentPlayer, )

        val card: Card? = game.currentHand().firstOrNull {
            game.canPlayCard(game.currentPlayer, it.id)
        }

        if (card == null) {
            val discard = game.currentHand().first()
            playOrDiscard = CommandDto(CommandType.DISCARD, card = discard.id, color = null, player = game.currentPlayer)
        } else {
            playOrDiscard = CommandDto(CommandType.PLAY, card = card.id, color = null, player = game.currentPlayer)
        }

        try {
            logger.info("GAME=${match.id} PLAYER=${match.currentPlayer} Command $playOrDiscard")
            logger.info("GAME=${match.id} PLAYER=${match.currentPlayer} Command $draw")
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
