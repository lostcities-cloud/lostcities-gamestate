package io.dereknelson.lostcities.gamestate.api

import io.dereknelson.lostcities.common.auth.LostCitiesUserDetails
import io.dereknelson.lostcities.gamestate.api.dto.TurnCommandRequest
import io.dereknelson.lostcities.gamestate.commandJob.CommandEvent
import io.dereknelson.lostcities.gamestate.matches.MatchService
import io.dereknelson.lostcities.models.SimpleResponseMessage
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
    private var applicationEventPublisher: ApplicationEventPublisher,
    private var matchService: MatchService,
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

        val match = matchService.getMatch(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        val gamestate = try {
            gameService.build(match)
        } catch (e: Exception) {
            logger.error("Unable to build game-state for match: $match")
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
        }

        val playerViewDto = try {
            gamestate.asPlayerView(userDetails.login)
        } catch (e: Exception) {
            logger.error("Unable to build player-view for game-state: $match")
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
    fun playCommand(
        @PathVariable id: Long,
        @RequestBody turn: TurnCommandRequest,
        @AuthenticationPrincipal @Parameter(hidden = true) userDetails: LostCitiesUserDetails,
    ): SimpleResponseMessage {
        val match = matchService
            .getMatch(id)
            .orElseThrow { throw ResponseStatusException(HttpStatus.NOT_FOUND) }

        applicationEventPublisher.publishEvent(
            CommandEvent(
                userDetails,
                match,
                turn.playOrDiscard,
                turn.draw,
            ),
        )

        return SimpleResponseMessage("Command processed")
    }
}
