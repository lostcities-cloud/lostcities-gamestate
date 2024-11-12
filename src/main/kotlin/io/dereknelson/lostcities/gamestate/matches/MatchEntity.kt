package io.dereknelson.lostcities.gamestate.matches

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.*
import org.springframework.data.redis.core.RedisHash
import java.time.LocalDateTime

@RedisHash("matches")
@JsonIgnoreProperties(ignoreUnknown = true)
class MatchEntity(

    @Id
    var id: Long,
    var seed: Long,

    var player1: String,
    var player2: String,
    var currentPlayer: String,

    var isPlayer1Ai: Boolean = false,
    var isPlayer2Ai: Boolean = false,
    val commands: LinkedHashSet<CommandEntity> = LinkedHashSet(),
    var hash: Int? = 0,
    @CreatedDate
    val createdDate: LocalDateTime? = null,
    @LastModifiedDate
    val lastModifiedDate: LocalDateTime? = null,
) {
    var turns: Long = 0

    override fun toString(): String {
        return "MatchEntity(id=$id, seed=$seed, player1='$player1', player2=$player2, currentPlayer='$currentPlayer', commands=$commands, createdDate=$createdDate, lastModifiedDate=$lastModifiedDate)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MatchEntity

        if (id != other.id) return false
        if (seed != other.seed) return false
        if (currentPlayer != other.currentPlayer) return false
        if (commands != other.commands) return false
        if (lastModifiedDate != other.lastModifiedDate) return false
        if (turns != other.turns) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + seed.hashCode()
        result = 31 * result + currentPlayer.hashCode()
        result = 31 * result + commands.hashCode()
        result = 31 * result + (lastModifiedDate?.hashCode() ?: 0)
        result = 31 * result + turns.hashCode()
        return result
    }
}
