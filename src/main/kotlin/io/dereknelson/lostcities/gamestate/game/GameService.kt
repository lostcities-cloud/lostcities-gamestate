package io.dereknelson.lostcities.gamestate.game

import io.dereknelson.lostcities.common.model.game.GameState
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
}