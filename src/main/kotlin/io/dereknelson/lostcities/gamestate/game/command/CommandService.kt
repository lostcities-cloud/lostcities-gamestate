package io.dereknelson.lostcities.gamestate.game.command

import io.dereknelson.lostcities.gamestate.game.GameState
import io.dereknelson.lostcities.models.state.Color
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class CommandService {

    fun execCommand(game: GameState, type: CommandType, card: String?, color: Color?, user: String) {
        if (!game.currentPlayer.equals(user)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }

        if (type === CommandType.PLAY) {
            if (!game.isGameOver() &&
                game.isCardInHand(user, card!!) &&
                game.canPlayCard(user, card)
            ) {
                game.playCard(user, card)
            } else {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
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
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }
        }
    }
}
