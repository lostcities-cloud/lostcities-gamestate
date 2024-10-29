package io.dereknelson.lostcities.gamestate.api

import io.dereknelson.lostcities.common.auth.LostCitiesUserDetails
import io.dereknelson.lostcities.gamestate.api.dto.TurnCommandRequest
import io.dereknelson.lostcities.gamestate.commandJob.CommandEvent
import io.dereknelson.lostcities.gamestate.matches.MatchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController("/gamestate")
class GameController(
    private var applicationEventPublisher: ApplicationEventPublisher,
    private var matchService: MatchService,
    private var gameService: GameService,
) {

    @Operation(description = "Retrieve a player view.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Game retrieved."),
            ApiResponse(responseCode = "404", description = "Game not found."),
        ],
    )
    @GetMapping("/{id}")
    fun getPlayerView(
        @PathVariable id: Long,
        @AuthenticationPrincipal @Parameter(hidden = true) userDetails: LostCitiesUserDetails,
    ): PlayerViewDto {
        return matchService
            .getMatch(id)
            .map {
                gameService
                    .build(it)
                    .asPlayerView(userDetails.login)
            }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
    }

    @Operation(description = "Play a command in a game.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Command executed."),
            ApiResponse(responseCode = "404", description = "Game not found."),
        ],
    )
    @PatchMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun playCommand(
        @PathVariable id: Long,
        @RequestBody turn: TurnCommandRequest,
        @AuthenticationPrincipal @Parameter(hidden = true) userDetails: LostCitiesUserDetails,
    ) = matchService
        .getMatch(id)
        .orElseThrow { throw ResponseStatusException(HttpStatus.NOT_FOUND) }
        .let {
            applicationEventPublisher.publishEvent(
                CommandEvent(
                    userDetails,
                    it,
                    turn.playOrDiscard,
                    turn.draw,
                ),
            )
        }
}
