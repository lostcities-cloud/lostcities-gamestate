package io.dereknelson.lostcities.gamestate

import io.dereknelson.lostcities.common.auth.LostCitiesUserDetails
import io.dereknelson.lostcities.gamestate.matches.MatchEntity
import io.dereknelson.lostcities.models.commands.CommandDto
import org.springframework.context.ApplicationEvent

data class AiEvent(
    val match: MatchEntity,
) : ApplicationEvent(match)
