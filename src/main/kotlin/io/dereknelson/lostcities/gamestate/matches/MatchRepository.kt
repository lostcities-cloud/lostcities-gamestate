package io.dereknelson.lostcities.gamestate.matches

import io.dereknelson.lostcities.gamestate.matches.entity.MatchEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MatchRepository : CrudRepository<MatchEntity, Long>
