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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandEntity

        if (user != other.user) return false
        if (type != other.type) return false
        if (card != other.card) return false
        if (color != other.color) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (card?.hashCode() ?: 0)
        result = 31 * result + (color?.hashCode() ?: 0)
        return result
    }


}
