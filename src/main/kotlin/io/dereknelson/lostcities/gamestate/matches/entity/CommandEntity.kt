package io.dereknelson.lostcities.gamestate.matches.entity


import io.dereknelson.lostcities.models.commands.CommandType
import io.dereknelson.lostcities.models.state.Color

class CommandEntity(
    val user: String,
    val type: CommandType,
    val card: String?,
    val color: Color?,
    val createdDate: Long
)
