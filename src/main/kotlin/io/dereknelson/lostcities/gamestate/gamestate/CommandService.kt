package io.dereknelson.lostcities.gamestate.gamestate

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
                gameEventService.sendCommandError(
                    CommandError(
                        game.id,
                        user,
                        commandDto,
                        "Game: ${game.id} | Not your turn.",
                    ),
                )
                logger.info("GAME=${game.id} PLAYER=${game.currentPlayer} Not your turn")
                return
            }

            if (game.isGameOver()) {
                gameEventService.sendCommandError(
                    CommandError(
                        game.id,
                        user,
                        commandDto,
                        "Game over.",
                    ),
                )
                logger.info("GAME=${game.id} PLAYER=${game.currentPlayer} Game over.")
                return
            }

            if (type === CommandType.PLAY) {
                if (game.isCardInHand(user, card!!) && game.canPlayCard(user, card)) {
                    logger.info("GAME=${game.id} PLAYER=${game.currentPlayer} Playing Card $card")
                    game.log.addLast(commandDto)
                    game.playCard(user, card)
                } else {
                    logger.info("GAME=${game.id} PLAYER=${game.currentPlayer} Unable to play card $card")
                    gameEventService.sendCommandError(
                        CommandError(
                            game.id,
                            user,
                            commandDto,
                            "Unable to play card.",
                        ),
                    )
                    throw Exception()
                }
            } else if (type === CommandType.DRAW && color !== null) {
                logger.info("GAME=${game.id} PLAYER=${game.currentPlayer} Draw from $color")
                game.log.addLast(commandDto)
                game.drawFromDiscard(user, color)
                game.endTurn()
            } else if (type === CommandType.DRAW) {
                logger.info("GAME=${game.id} PLAYER=${game.currentPlayer} Draw from deck")
                game.log.addLast(commandDto)
                game.drawCard(user)
                game.endTurn()
            } else if (type === CommandType.DISCARD) {
                if (game.isCardInHand(user, card!!)) {
                    logger.info("GAME=${game.id} PLAYER=${game.currentPlayer} Discard $card")
                    game.log.addLast(commandDto)
                    game.discard(user, card)
                } else {
                    logger.info("GAME=${game.id} PLAYER=${game.currentPlayer} Unable to discard $card")
                    gameEventService.sendCommandError(
                        CommandError(
                            game.id,
                            user,
                            commandDto,
                            "Unable to discard card.",
                        ),
                    )
                    logger.info("Unable to discard card $card")

                    throw Exception()
                }
            }
        } catch (e: Exception) {
            gameEventService.sendCommandError(
                CommandError(
                    game.id,
                    user,
                    commandDto,
                    "Unable to execute command.",
                ),
            )

            throw e
        }
    }
}
