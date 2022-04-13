package io.dereknelson.lostcities.gamestate.games

import io.dereknelson.lostcities.gamestate.matches.CommandEntity
import io.dereknelson.lostcities.gamestate.matches.MatchEntity
import io.dereknelson.lostcities.gamestate.matches.MatchRepository
import io.dereknelson.lostcities.models.commands.CommandDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class GameService(
    private var matchRepository: MatchRepository,
    private var gameFactory: GameFactory,
    private var matchEventService: GameEventService
) {

    @Autowired @Lazy
    private lateinit var commandService: CommandService

    fun exists(id: Long): Boolean {
        return matchRepository.existsById(id)
    }

    fun build(matchEntity: MatchEntity): GameState {
        return gameFactory.build(matchEntity)
            .playCommandsForward()
    }

    fun saveNewMatch(matchEntity: MatchEntity): GameState? {
        return if (matchRepository.findById(matchEntity.id).isEmpty) {
            val gameState = gameFactory.build(matchEntity)
            save(gameState)
        } else null
    }

    fun saveTurn(
        gameState: GameState,
        playOrDiscardCommand: CommandEntity,
        drawCommand: CommandEntity
    ) {
        val match = gameState.matchEntity
        match.commands.add(playOrDiscardCommand)
        match.commands.add(drawCommand)

        save(gameState)

        matchEventService.sendTurnChangeEvent(match.id, playOrDiscardCommand.user)

        if (gameState.isGameOver()) {
            endGame(gameState.id, gameState.calculateScores())
        }
    }

    fun play(game: GameState, commandDto: CommandDto, user: String) {
        commandService.execCommand(game, commandDto, user)
    }

    private fun sendPlayerEvents(gameState: GameState) {
        val playerEvents = gameState.playerAreas.keys
            .associateWith { gameState.asPlayerView(it) }
        matchEventService.sendPlayerEvents(playerEvents)
    }

    private fun save(gameState: GameState): GameState {
        matchRepository.save(gameState.matchEntity)
        sendPlayerEvents(gameState)
        return gameState
    }

    private fun endGame(id: Long, scores: Map<String, Int>) {
        matchEventService.endGame(id, scores)
    }

    private fun GameState.playCommandsForward(): GameState {
        this.matchEntity.commands.forEach { command ->
            commandService.execCommand(
                this,
                command.toDto(),
                this.currentPlayer
            )
        }
        this.playerEvents.clear()
        return this
    }

    private fun CommandEntity.toDto() = CommandDto(this.type, this.card, this.color)
}
