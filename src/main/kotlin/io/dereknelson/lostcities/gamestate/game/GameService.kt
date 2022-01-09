package io.dereknelson.lostcities.gamestate.game

import io.dereknelson.lostcities.gamestate.persistance.CommandEntity
import io.dereknelson.lostcities.gamestate.persistance.MatchRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class GameService(
    private var matchRepository: MatchRepository,
    private var gameFactory: GameFactory
) {

    fun exists(id: Long): Boolean {
        return matchRepository.existsById(id)
    }

    fun getGame(id: Long): Optional<GameState> {
        return matchRepository
            .findById(id)
            .map { gameFactory.build(it!!) }
    }

    fun saveTurn(gameState: GameState, playOrDiscardCommand: CommandEntity, drawCommand: CommandEntity) {
        val match = gameState.matchEntity
        match.commands.add(playOrDiscardCommand)
        match.commands.add(drawCommand)
        matchRepository.save(match)
    }
}