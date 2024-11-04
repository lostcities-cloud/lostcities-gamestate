package io.dereknelson.lostcities.gamestate.api

import io.dereknelson.lostcities.gamestate.matches.CommandEntity
import io.dereknelson.lostcities.gamestate.matches.MatchEntity
import io.dereknelson.lostcities.gamestate.matches.MatchRepository
import io.dereknelson.lostcities.models.commands.CommandDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GameService(
    private val matchRepository: MatchRepository,
    private val gameFactory: GameFactory,
    private val matchEventService: GameEventService,
    private val commandService: CommandService,
) {
    private val logger = LoggerFactory.getLogger(GameService::class.java)

    fun exists(id: Long): Boolean {
        return matchRepository.existsById(id)
    }

    fun build(matchEntity: MatchEntity): GameState {
        return gameFactory.build(matchEntity)
            .playCommandsForward()
    }

    fun saveNewMatch(matchEntity: MatchEntity): GameState? {
        if (matchRepository.existsById(matchEntity.id)) {
            return null
        }

        matchEntity.currentPlayer = matchEntity.player1

        val gameState = gameFactory.build(matchEntity)
        return save(gameState)
    }

    fun saveTurn(
        gameState: GameState,
        playOrDiscardCommand: CommandEntity,
        drawCommand: CommandEntity,
    ) {
        val match = gameState.matchEntity
        match.commands.add(playOrDiscardCommand)
        match.commands.add(drawCommand)

        match.currentPlayer = if (match.currentPlayer != match.player1) {
            match.player1
        } else {
            match.player2
        }

        save(gameState)

        matchEventService.sendTurnChangeEvent(match.id, match.currentPlayer!!)

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
            val commandDto = command.toDto()
            try {
                commandService.execCommand(
                    this,
                    commandDto,
                    this.currentPlayer,
                )
            } catch (e: Exception) {
                logger.error("Command failed: $command")
            }
        }

        this.playerEvents.clear()
        return this
    }

    private fun CommandEntity.toDto() = CommandDto(
        player = this.user,
        received = this.received(),
        type = this.type,
        card = this.card,
        color = this.color,
    )
}
