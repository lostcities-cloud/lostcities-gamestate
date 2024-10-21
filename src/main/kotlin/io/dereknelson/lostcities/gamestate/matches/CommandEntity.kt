package io.dereknelson.lostcities.gamestate.matches

import io.dereknelson.lostcities.models.commands.CommandType
import io.dereknelson.lostcities.models.state.Color
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class CommandEntity(
    val user: String,
    val type: CommandType,
    val card: String?,
    val color: Color?,
    val createdDate: Long,
) {
    fun received(): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(this.createdDate),
            ZoneId.of("UTC"),
        )
    }
}
