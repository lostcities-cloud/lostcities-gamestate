package io.dereknelson.lostcities.gamestate.gamestate

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
internal interface MatchRepository : CrudRepository<MatchEntity, Long>
