package io.dereknelson.lostcities.gamestate.aiJob

import com.fasterxml.jackson.databind.ObjectMapper
import io.dereknelson.lostcities.gamestate.AiEvent
import io.dereknelson.lostcities.gamestate.gamestate.GameEventService.Companion.AI_PLAYER_REQUEST_EVENT
import io.dereknelson.lostcities.gamestate.gamestate.GameService
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

@Component
class AiProcessor(
    val objectMapper: ObjectMapper,
) {
    private val logger: Log = LogFactory.getLog(this::class.java)

    @Autowired @Lazy
    private lateinit var gameService: GameService

    @RabbitListener(queues = [AI_PLAYER_REQUEST_EVENT], concurrency = "1-2")
    fun playAiTurn(gameMessage: Message) {
        val aiEvent = objectMapper.readValue(gameMessage.body, AiEvent::class.java)

        val game = gameService.getGame(aiEvent.id)

        val userDetails = AiLostCitiesUserDetails(game.currentPlayer)
        logger.info("GAME=${aiEvent.id} PLAYER=${game.currentPlayer} Starting AI turn")
        if (game.isGameOver()) {
            logger.info("GAME=${aiEvent.id} PLAYER=${game.currentPlayer} Game Already Completed")
            return
        }

        if (!game.isCurrentPlayerAi()) {
            logger.info("GAME=${aiEvent.id} PLAYER=${game.currentPlayer} Current player is not an AI Player")
            return
        }

        var playOrDiscard: CommandDto
        val draw = CommandDto(CommandType.DRAW, card = null, color = null, player = game.currentPlayer)

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
            logger.info("GAME=${aiEvent.id} PLAYER=${game.currentPlayer} Command $playOrDiscard")
            logger.info("GAME=${aiEvent.id} PLAYER=${game.currentPlayer} Command $draw")
            gameService.play(game, playOrDiscard, game.currentPlayer)
            gameService.play(game, draw, game.currentPlayer)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }

        gameService.saveTurn(
            userDetails,
            game,
            playOrDiscard,
            draw,
        )
    }
}
