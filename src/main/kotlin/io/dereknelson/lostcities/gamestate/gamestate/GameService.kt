package io.dereknelson.lostcities.gamestate.gamestate

import com.fasterxml.jackson.databind.ObjectMapper
import io.dereknelson.lostcities.gamestate.gamestate.GameEventService.Companion.AI_PLAYER_REQUEST_EVENT
import io.dereknelson.lostcities.gamestate.gamestate.matches.CommandEntity
import io.dereknelson.lostcities.gamestate.gamestate.matches.MatchEntity
import io.dereknelson.lostcities.gamestate.gamestate.matches.MatchRepository
import io.dereknelson.lostcities.models.commands.CommandDto
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class GameService(
    private val matchRepository: MatchRepository,
    private val gameFactory: GameFactory,
    private val matchEventService: GameEventService,
    private val commandService: CommandService,
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper,
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
        playOrDiscardCommand: CommandDto,
        drawCommand: CommandDto,
    ): GameState {
        val match = gameState.matchEntity
        match.commands.add(CommandEntity.fromDto(playOrDiscardCommand))
        match.commands.add(CommandEntity.fromDto(drawCommand))

        match.turns++

        match.currentPlayer = gameState.currentPlayer

        val newGamestate = save(gameState)

        matchEventService.sendTurnChangeEvent(match)

        if (gameState.isGameOver()) {
            endGame(gameState.id, gameState.calculateScores())
        }

        return newGamestate
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
        logger.info("GAME=${gameState.id} Saving gamestate")
        // if (!verifyMatchHash(gameState.matchEntity)) {
        //    val message = "Game has been modified since matchEntity was read."
        //    logger.warn(message)
        //    throw RuntimeException(message)
        // }

        gameState.matchEntity.hash = gameState.hashCode()
        matchRepository.save(gameState.matchEntity)
        logger.info("GAME=${gameState.id} Sending player events")
        sendPlayerEvents(gameState)
        logger.info("GAME=${gameState.id} Sending ai requests")
        triggerAiPlayerForMatch(gameState)

        return gameState
    }

    private fun verifyMatchHash(matchEntity: MatchEntity): Boolean {
        if (matchRepository.existsById(matchEntity.id)) {
            val checkMatch = matchRepository.findById(matchEntity.id).get()

            return checkMatch.hash == matchEntity.hash
        }

        return true
    }

    private fun endGame(id: Long, scores: Map<String, Int>) {
        matchEventService.endGame(id, scores)
    }

    private fun GameState.playCommandsForward(): GameState {
        this.matchEntity.commands.forEach { command ->
            val commandDto = command.toDto()
            try {
                logger.info("GAME=$id PLAYER=${this.matchEntity.currentPlayer}, Playing Forward command=$commandDto")
                commandService.execCommand(
                    this,
                    commandDto,
                    this.currentPlayer,
                )
            } catch (e: Exception) {
                logger.error("GAME=$id Command failed: $command")
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

    private fun triggerAiPlayerForMatch(gameState: GameState) {
        logger.info("Checking for ai player")
        if (gameState.isCurrentPlayerAi()) {
            logger.info("Triggering ai turn")
            rabbitTemplate.convertAndSend(
                AI_PLAYER_REQUEST_EVENT,
                objectMapper.writeValueAsBytes(gameState.matchEntity),
            )
        }
    }
}
