package io.dereknelson.lostcities.gamestate.persistance

import io.dereknelson.lostcities.gamestate.game.command.CommandType
import io.dereknelson.lostcities.models.state.Color

class CommandEntity(
    val user: String,
    val type: CommandType,
    val card: String?,
    val color: Color?,
    val createdDate: Long
)
