package io.dereknelson.lostcities.gamestate.gamestate.matches

import org.springframework.stereotype.Service
import java.util.Optional

@Service
class MatchService(
    private val matchRepository: MatchRepository,
) {
    fun getMatch(id: Long): Optional<MatchEntity> {
        return matchRepository.findById(id)
    }

    fun save(matchEntity: MatchEntity): MatchEntity {
        return matchRepository.save(matchEntity)
    }
}
