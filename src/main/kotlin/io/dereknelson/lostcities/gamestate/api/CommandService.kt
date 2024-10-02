package io.dereknelson.lostcities.gamestate.api

import io.dereknelson.lostcities.models.commands.CommandDto
import io.dereknelson.lostcities.models.commands.CommandError
import io.dereknelson.lostcities.models.commands.CommandType
import io.dereknelson.lostcities.models.state.Color
import org.springframework.stereotype.Service

@Service
class CommandService(
    private val gameEventService: GameEventService
) {

    fun execCommand(game: GameState, commandDto: CommandDto, user: String) {
        try {

            val (type: CommandType, card: String?, color: Color?) = commandDto
            if (game.currentPlayer != user) {
                CommandError(
                    game.id,
                    user,
                    commandDto,
                    "Not your turn."
                ).send()
                return
            }

            if (game.isGameOver()) {
                CommandError(
                    game.id,
                    user,
                    commandDto,
                    "Game over."
                ).send()
                return
            }

            if (type === CommandType.PLAY) {
                if (game.isCardInHand(user, card!!) &&
                    game.canPlayCard(user, card)
                ) {
                    game.playCard(user, card)
                } else {
                    CommandError(
                        game.id,
                        user,
                        commandDto,
                        "Unable to play card."
                    ).send()
                    throw Exception()
                }
            } else if (type === CommandType.DRAW && color !== null) {
                game.drawFromDiscard(user, color)
                game.endTurn()
            } else if (type === CommandType.DRAW) {
                game.drawCard(user)
                game.endTurn()
            } else if (type === CommandType.DISCARD) {
                if (game.isCardInHand(user, card!!)) {
                    game.discard(user, card)
                } else {
                    CommandError(
                        game.id,
                        user,
                        commandDto,
                        "Unable to discard card."
                    ).send()

                    throw Exception()
                }
            }
        } catch (e: Exception) {
            CommandError(
                game.id,
                user,
                commandDto,
                "Unable to execute command."
            ).send()

            throw e
        }
    }

    private fun CommandError.send() = gameEventService.sendCommandError(this)
}
