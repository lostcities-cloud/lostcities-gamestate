package io.dereknelson.lostcities.gamestate.game

import io.dereknelson.lostcities.common.model.game.components.Card
import io.dereknelson.lostcities.common.model.game.components.Color
import io.dereknelson.lostcities.common.model.game.components.Phase

data class CommandDto(
    val playerId: Long,
    val phase: Phase,
    val draw: Boolean,
    val discard: Boolean,
    val color: Color?,
    val card: Card?
)