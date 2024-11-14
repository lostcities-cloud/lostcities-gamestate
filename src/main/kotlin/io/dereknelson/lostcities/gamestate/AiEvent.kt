package io.dereknelson.lostcities.gamestate

import io.dereknelson.lostcities.gamestate.gamestate.matches.MatchEntity
import org.springframework.context.ApplicationEvent

data class AiEvent(
    val match: MatchEntity,
) : ApplicationEvent(match)
