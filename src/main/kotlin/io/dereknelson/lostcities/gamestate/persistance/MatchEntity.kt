package io.dereknelson.lostcities.gamestate.persistance

import io.dereknelson.lostcities.common.model.match.UserPair
import org.springframework.data.annotation.*
import org.springframework.data.redis.core.RedisHash
import java.time.LocalDateTime

@RedisHash("matches")
class MatchEntity(
    @Id val id: Long,
    val players: UserPair,
    val seed: Long,
    val isReady: Boolean = false,
    val isStarted: Boolean = false,
    val isCompleted: Boolean = false,

    val concededBy: String? = null,

    val commands: MutableList<CommandEntity> = mutableListOf(),

    @CreatedDate
    val createdDate: LocalDateTime? = null,
    @LastModifiedDate
    val lastModifiedDate: LocalDateTime? = null,

    @CreatedBy
    val createdBy: String? = null,
    @LastModifiedBy
    var lastModifiedBy: String? = null
)
