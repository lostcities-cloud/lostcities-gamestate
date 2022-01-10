package io.dereknelson.lostcities.gamestate.game

import com.fasterxml.jackson.databind.ObjectMapper
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
    private var commandService: CommandService,
    private var objectMapper: ObjectMapper
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
            .map {
                playCommandsForward(it)
                it.asPlayerView(userDetails.login)
            }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
    }

    @Operation(description = "Retrieve a game state for debugging.")
    @ApiResponses(value = [
        ApiResponse(responseCode="200", description= "Game retrieved."),
        ApiResponse(responseCode="404", description= "Game not found.")
    ])
    @GetMapping("/{id}/debug")
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
        @RequestBody turnCommandRequest: TurnCommandRequest,
        @AuthenticationPrincipal @Parameter(hidden=true) userDetails: LostCitiesUserDetails,
    ): PlayerViewDto? {
        val user = userDetails.login


        val game = gameService.getGame(id)
            .orElseThrow { throw ResponseStatusException(HttpStatus.NOT_FOUND)}

        playCommandsForward(game)

        play(game, turnCommandRequest.playOrDiscard, user)
        play(game, turnCommandRequest.draw, user)

        gameService.saveTurn(
            game,
            turnCommandRequest.playOrDiscard.asEntity(),
            turnCommandRequest.draw.asEntity()
        )

        return game.asPlayerView(user)
    }

    private fun play(game: GameState, commandDto: CommandDto, user: String) {
        val (type, card, color) = commandDto
        commandService.execCommand(game, type, card, color, user)
    }


    private fun playCommandsForward(gameState: GameState) {
        gameState.matchEntity.commands.forEach {
            commandService.execCommand(
                gameState,
                it.type,
                it.card,
                it.color,
                gameState.currentPlayer
            )
        }
    }

    private fun CommandDto.asEntity(): CommandEntity {
        return CommandEntity(type, card, color, Instant.now().toEpochMilli())
    }

    private fun GameState.asPlayerView(player: String): PlayerViewDto {
        return PlayerViewDto(
            id=this.id,
            deckRemaining=this.deck.size,
            player=player,
            isPlayerTurn=this.currentPlayer==player,
            hand=this.playerHands[player]!!.values.toMutableList(),
            playAreas=this.playerAreas,
            discard=this.discard
        )
    }
}