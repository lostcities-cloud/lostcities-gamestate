package io.dereknelson.lostcities.gamestate.persistance

import io.dereknelson.lostcities.common.model.match.UserPair
import org.springframework.data.annotation.Id
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

    val createdDate: LocalDateTime? = null,
    val lastModifiedDate: LocalDateTime? = null,
    val createdBy: String? = null,
    var lastModifiedBy: String? = null
) {

}