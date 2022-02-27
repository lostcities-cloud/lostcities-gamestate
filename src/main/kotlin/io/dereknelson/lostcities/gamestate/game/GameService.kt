package io.dereknelson.lostcities.gamestate.game

import com.fasterxml.jackson.databind.ObjectMapper
import io.dereknelson.lostcities.gamestate.game.command.CommandDto
import io.dereknelson.lostcities.gamestate.game.command.CommandService
import io.dereknelson.lostcities.gamestate.matchevents.MatchEventService
import io.dereknelson.lostcities.gamestate.persistance.CommandEntity
import io.dereknelson.lostcities.gamestate.persistance.MatchEntity
import io.dereknelson.lostcities.gamestate.persistance.MatchRepository
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import java.util.*

@Service
class GameService(
    private var matchRepository: MatchRepository,
    private var gameFactory: GameFactory,
    private var matchEventService: MatchEventService,
    private var commandService: CommandService
) {



    fun exists(id: Long): Boolean {
        return matchRepository.existsById(id)
    }

    fun getGame(id: Long): Optional<GameState> {
        return matchRepository
            .findById(id)
            .map {
                gameFactory.build(it!!)
                    .playCommandsForward()
            }
    }

    fun build(matchEntity: MatchEntity): GameState {
        return gameFactory.build(matchEntity)
    }

    fun save(gameState: GameState) {
        matchRepository.save(gameState.matchEntity)
        sendPlayerEvents(gameState)
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
    }

    fun endGame(id: Long, scores: Map<String, Int>) {
        matchEventService.endGame(id, scores)
    }

    fun play(game: GameState, commandDto: CommandDto, user: String) {
        val (type, card, color) = commandDto
        commandService.execCommand(game, type, card, color, user)
    }

    fun sendPlayerEvents(gameState: GameState) {
        val playerEvents = gameState.playerAreas.keys
            .associateWith { gameState.asPlayerView(it) }
        matchEventService.sendPlayerEvents(playerEvents)
    }

    private fun GameState.playCommandsForward(): GameState {
        this.matchEntity.commands.forEach { command ->
            commandService.execCommand(
                this,
                command.type,
                command.card,
                command.color,
                this.currentPlayer
            )
        }
        this.playerEvents.clear()
        return this
    }

}
