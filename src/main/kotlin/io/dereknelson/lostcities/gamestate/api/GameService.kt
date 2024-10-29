package io.dereknelson.lostcities.gamestate.api

import io.dereknelson.lostcities.common.auth.TokenProvider
import io.dereknelson.lostcities.gamestate.matches.CommandEntity
import io.dereknelson.lostcities.gamestate.matches.MatchEntity
import io.dereknelson.lostcities.gamestate.matches.MatchRepository
import io.dereknelson.lostcities.models.commands.CommandDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class GameService(
    private var matchRepository: MatchRepository,
    private var gameFactory: GameFactory,
    private var matchEventService: GameEventService,
) {
    private val logger = LoggerFactory.getLogger(GameService::class.java)

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
        } else {
            null
        }
    }

    fun saveTurn(
        gameState: GameState,
        playOrDiscardCommand: CommandEntity,
        drawCommand: CommandEntity,
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
            val commandDto = command.toDto()
            try {
                commandService.execCommand(
                    this,
                    commandDto,
                    this.currentPlayer,
                )
            } catch (e: Exception) {
                logger.error("Command failed: ${command.toString()}")
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
