package io.dereknelson.lostcities.gamestate

import io.dereknelson.lostcities.common.auth.LostCitiesUserDetails
import io.dereknelson.lostcities.gamestate.gamestate.GameService
import io.dereknelson.lostcities.gamestate.gamestate.dto.TurnCommandRequest
import io.dereknelson.lostcities.models.SimpleResponseMessage
import io.dereknelson.lostcities.models.state.PlayerViewDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController()
@CrossOrigin(
    origins = [
        "http://localhost:4452",
        "http://127.0.0.1:4452",
        "http://localhost:8080",
        "http://127.0.0.1:8080",
        "http://192.168.1.241:8080",

        "*",
    ],
)
class GameController(
    private var gameService: GameService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(description = "Retrieve a player view.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Game retrieved."),
            ApiResponse(responseCode = "404", description = "Game not found."),
        ],
    )
    @GetMapping("/gamestate/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    fun getPlayerView(
        @PathVariable id: Long,
        @AuthenticationPrincipal @Parameter(hidden = true) userDetails: LostCitiesUserDetails,
    ): PlayerViewDto {
        val gamestate = try {
            gameService.getGame(id)
        } catch (e: Exception) {
            logger.error("Unable to build game-state for match: $id")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }

        val playerViewDto = try {
            gamestate.asPlayerView(userDetails.login)
        } catch (e: Exception) {
            logger.error("Unable to build player-view for game-state: $id")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }

        return playerViewDto
    }

    @Operation(description = "Play a command in a game.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Command executed."),
            ApiResponse(responseCode = "404", description = "Game not found."),
        ],
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/gamestate/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Transactional
    fun playCommand(
        @PathVariable id: Long,
        @RequestBody turn: TurnCommandRequest,
        @AuthenticationPrincipal @Parameter(hidden = true) userDetails: LostCitiesUserDetails,
    ): SimpleResponseMessage {
        val gamestate = try {
            gameService.getGame(id)
        } catch (e: Exception) {
            logger.error("Unable to build game-state for match: $id")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }

        if (gamestate.isGameOver()) {
            logger.info("Game Already Completed, Player($userDetails.login)")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }

        try {
            gameService.play(gamestate, turn.playOrDiscard, userDetails.login)
            gameService.play(gamestate, turn.draw, userDetails.login)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }

        gameService.saveTurn(
            gamestate,
            turn.playOrDiscard,
            turn.draw,
        )

        return SimpleResponseMessage("Command processed")
    }
}
