package io.dereknelson.lostcities.gamestate.game

import io.dereknelson.lostcities.common.auth.LostCitiesUserDetails
import io.dereknelson.lostcities.gamestate.game.command.CommandDto
import io.dereknelson.lostcities.gamestate.game.command.CommandService
import io.dereknelson.lostcities.gamestate.game.command.TurnCommandRequest
import io.dereknelson.lostcities.gamestate.persistance.CommandEntity
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestController
@RequestMapping("/api/gamestate")
class GameController(
    private var gameService: GameService,

) {

    @Operation(description = "Retrieve a player view.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Game retrieved."),
            ApiResponse(responseCode = "404", description = "Game not found.")
        ]
    )
    @GetMapping("/{id}")
    fun getPlayerView(
        @PathVariable id: Long,
        @AuthenticationPrincipal @Parameter(hidden = true) userDetails: LostCitiesUserDetails,
    ): PlayerViewDto {
        return gameService.getGame(id)
            .map {
                it.asPlayerView(userDetails.login)
            }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
    }

    @Operation(description = "Retrieve a game state for debugging.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Game retrieved."),
            ApiResponse(responseCode = "404", description = "Game not found.")
        ]
    )
    @GetMapping("/{id}/debug")
    fun getDebugGameState(
        @PathVariable id: Long
    ): GameState {
        return gameService.getGame(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
    }

    @Operation(description = "Play a command in a game.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Command executed."),
            ApiResponse(responseCode = "404", description = "Game not found."),
            ApiResponse(responseCode = "406", description = "Invalid command.")
        ]
    )
    @PatchMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun playCommand(
        @PathVariable id: Long,
        @RequestBody turnCommandRequest: TurnCommandRequest,
        @AuthenticationPrincipal @Parameter(hidden = true) userDetails: LostCitiesUserDetails,
    ): PlayerViewDto? {
        val user = userDetails.login

        val game = gameService.getGame(id)
            .orElseThrow { throw ResponseStatusException(HttpStatus.NOT_FOUND) }

        gameService.play(game, turnCommandRequest.playOrDiscard, user)
        gameService.play(game, turnCommandRequest.draw, user)

        gameService.saveTurn(
            game,
            turnCommandRequest.playOrDiscard.asEntity(user),
            turnCommandRequest.draw.asEntity(user)
        )

        if(game.isGameOver()) {
            gameService.endGame(game.id, game.calculateScores())
        }

        return game.asPlayerView(user)
    }

    private fun CommandDto.asEntity(user: String): CommandEntity {
        return CommandEntity(user, type, card, color, Instant.now().toEpochMilli())
    }
}
