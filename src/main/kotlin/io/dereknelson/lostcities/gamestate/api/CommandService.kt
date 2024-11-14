package io.dereknelson.lostcities.gamestate.api

import io.dereknelson.lostcities.models.commands.CommandDto
import io.dereknelson.lostcities.models.commands.CommandError
import io.dereknelson.lostcities.models.commands.CommandType
import io.dereknelson.lostcities.models.state.Color
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Service

@Service
class CommandService(
    private val gameEventService: GameEventService,
) {
    private val logger: Log = LogFactory.getLog(this::class.java)

    fun execCommand(game: GameState, commandDto: CommandDto, user: String) {
        try {
            val (type: CommandType, card: String?, color: Color?) = commandDto
            if (game.currentPlayer != user) {
                gameEventService.sendCommandError(CommandError(
                    game.id,
                    user,
                    commandDto,
                    "Not your turn.",
                ))
                logger.info("Not your turn")
                return
            }

            if (game.isGameOver()) {
                gameEventService.sendCommandError(CommandError(
                    game.id,
                    user,
                    commandDto,
                    "Game over.",
                ))
                logger.info("Game over.")
                return
            }

            if (type === CommandType.PLAY) {
                if (game.isCardInHand(user, card!!) &&
                    game.canPlayCard(user, card)
                ) {
                    game.log.addLast(commandDto)
                    game.playCard(user, card)
                } else {
                    logger.info("Unable to play card $card")
                    gameEventService.sendCommandError(CommandError(
                        game.id,
                        user,
                        commandDto,
                        "Unable to play card.",
                    ))
                    throw Exception()
                }
            } else if (type === CommandType.DRAW && color !== null) {
                game.log.addLast(commandDto)
                game.drawFromDiscard(user, color)
                game.endTurn()
            } else if (type === CommandType.DRAW) {
                game.log.addLast(commandDto)
                game.drawCard(user)
                game.endTurn()
            } else if (type === CommandType.DISCARD) {
                if (game.isCardInHand(user, card!!)) {
                    game.log.addLast(commandDto)
                    game.discard(user, card)
                } else {
                    gameEventService.sendCommandError(CommandError(
                        game.id,
                        user,
                        commandDto,
                        "Unable to discard card.",
                    ))
                    logger.info("Unable to discard card $card")

                    throw Exception()
                }
            }
        } catch (e: Exception) {
            gameEventService.sendCommandError(CommandError(
                game.id,
                user,
                commandDto,
                "Unable to execute command.",
            ))

            throw e
        }
    }

}
