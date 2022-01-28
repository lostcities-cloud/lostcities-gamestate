package io.dereknelson.lostcities.gamestate.persistance

import io.dereknelson.lostcities.gamestate.game.command.CommandType
import io.dereknelson.lostcities.gamestate.game.state.Color
import java.sql.Timestamp

class CommandEntity(
    val user: String,
    val type: CommandType,
    val card: String?,
    val color: Color?,
    val createdDate: Long
)