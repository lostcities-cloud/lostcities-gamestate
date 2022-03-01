package io.dereknelson.lostcities.gamestate.matches

import io.dereknelson.lostcities.gamestate.matches.entity.MatchEntity
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class MatchService (
    private val matchRepository: MatchRepository
) {
    fun getMatch(id: Long): Optional<MatchEntity> {
        return matchRepository.findById(id)
    }

    fun save(matchEntity: MatchEntity): MatchEntity {
        return matchRepository.save(matchEntity)
    }
}
