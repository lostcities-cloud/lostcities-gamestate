package io.dereknelson.lostcities.gamestate.game

import io.dereknelson.lostcities.gamestate.matchevents.MatchEventService
import io.dereknelson.lostcities.gamestate.persistance.CommandEntity
import io.dereknelson.lostcities.gamestate.persistance.MatchRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class GameService(
    private var matchRepository: MatchRepository,
    private var gameFactory: GameFactory,
    private var matchEventService: MatchEventService
) {

    fun exists(id: Long): Boolean {
        return matchRepository.existsById(id)
    }

    fun getGame(id: Long): Optional<GameState> {
        return matchRepository
            .findById(id)
            .map { gameFactory.build(it!!) }
    }

    fun saveTurn(
        gameState: GameState,
        playOrDiscardCommand: CommandEntity,
        drawCommand: CommandEntity,
        playerEvents: Map<String, PlayerViewDto>
    ) {
        val match = gameState.matchEntity
        match.commands.add(playOrDiscardCommand)
        match.commands.add(drawCommand)
        matchRepository.save(match)
        matchEventService.sendTurnChangeEvent(match.id, playOrDiscardCommand.user)

        matchEventService.sendPlayerEvents(playerEvents)
    }

    fun endGame(scores: Map<String, Int>) {
        matchEventService.endGame(scores)
    }
}
