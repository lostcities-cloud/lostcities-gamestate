package io.dereknelson.lostcities.gamestate.game

import io.dereknelson.lostcities.common.auth.LostCitiesUserDetails
import io.dereknelson.lostcities.gamestate.game.command.CommandDto
import io.dereknelson.lostcities.gamestate.game.command.CommandType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.lang.IllegalStateException

@RestController
@RequestMapping("/api/gamestate")
class GameController(
    private var gameService: GameService
) {

    @Operation(description = "Retrieve a player view.")
    @ApiResponses(value = [
        ApiResponse(responseCode="200", description= "Game retrieved."),
        ApiResponse(responseCode="404", description= "Game not found.")
    ])
    @GetMapping("/{id}")
    fun getPlayerView(
        @PathVariable id: Long,
        @AuthenticationPrincipal @Parameter(hidden=true) userDetails: LostCitiesUserDetails,
    ): PlayerViewDto {
        return gameService.getGame(id)
            .map { it.asPlayerView(userDetails.login) }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
    }

    @Operation(description = "Retrieve a game state for debugging.")
    @ApiResponses(value = [
        ApiResponse(responseCode="200", description= "Game retrieved."),
        ApiResponse(responseCode="404", description= "Game not found.")
    ])
    @GetMapping("/:id/debug")
    fun getDebugGameState(
        @PathVariable id: Long
    ): GameState {
        return gameService.getGame(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
    }

    @Operation(description = "Play a command in a game.")
    @ApiResponses(value = [
        ApiResponse(responseCode="201", description= "Command executed."),
        ApiResponse(responseCode="404", description= "Game not found."),
        ApiResponse(responseCode="406", description= "Invalid command.")
    ])
    @PatchMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun playCommand(
        @PathVariable id: Long,
        @RequestBody commandDto: CommandDto,
        @AuthenticationPrincipal @Parameter(hidden=true) userDetails: LostCitiesUserDetails,
    ): PlayerViewDto? {
        val game = gameService.getGame(id)
            .orElseThrow { throw ResponseStatusException(HttpStatus.NOT_FOUND)}

        val (user, type, card, color) = commandDto

        if(commandDto.user !== userDetails.login || game.currentPlayer !== user) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }

        if (type === CommandType.PLAY) {
            if(game.isCardInHand(user, card!!)) {
                game.playCard(user, card)
            } else {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }
        } else if (type === CommandType.DRAW && color !== null) {
                game.drawFromDiscard(user, color)
        } else if (type === CommandType.DRAW) {
                game.drawCard(user)
        } else if (type === CommandType.DISCARD) {
            if(game.isCardInHand(user, card!!)) {
                game.discard(user, card)
            } else {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }
        }

        return game.asPlayerView(user)
    }


    private fun GameState.asPlayerView(player: String): PlayerViewDto {
        return PlayerViewDto(
            id=this.id,
            gamePhase=this.phase,
            deckRemaining=this.deck.size,
            player=player,
            isPlayerTurn=this.currentPlayer==player,
            hand=this.playerHands[player]!!.values.toMutableList(),
            playAreas=this.playerAreas,
            discard=this.discard
        )
    }
}