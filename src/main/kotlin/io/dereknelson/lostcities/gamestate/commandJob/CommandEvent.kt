package io.dereknelson.lostcities.gamestate.commandJob

import io.dereknelson.lostcities.common.auth.LostCitiesUserDetails
import io.dereknelson.lostcities.gamestate.matches.entity.MatchEntity
import io.dereknelson.lostcities.models.commands.CommandDto
import org.springframework.context.ApplicationEvent

data class CommandEvent (
    val userDetails: LostCitiesUserDetails,
    val match: MatchEntity,
    val playOrDiscard: CommandDto,
    val draw: CommandDto
): ApplicationEvent(userDetails)
