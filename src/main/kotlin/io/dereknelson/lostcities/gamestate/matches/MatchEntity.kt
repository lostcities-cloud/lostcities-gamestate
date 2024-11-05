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

    val commands: LinkedHashSet<CommandEntity> = LinkedHashSet(),

    @CreatedDate
    val createdDate: LocalDateTime? = null,
    @LastModifiedDate
    val lastModifiedDate: LocalDateTime? = null,
) {
    var turns: Long = 0

    override fun toString(): String {
        return "MatchEntity(id=$id, seed=$seed, player1='$player1', player2=$player2, currentPlayer='$currentPlayer', commands=$commands, createdDate=$createdDate, lastModifiedDate=$lastModifiedDate)"
    }
}
