package io.dereknelson.lostcities.gamestate.game.command

import io.dereknelson.lostcities.gamestate.game.state.Color

/**
 *  Events
 Play: {type: "play", card: "${card}", user: ${user}}
 Discard: {type: "discard", card: "${card}", user: ${user}}
 Draw From Deck: {type: "draw", user: ${user}}
 Draw From Deck: {type: "draw", color: "YELLOW", user: ${user}}
 */

data class CommandDto(
    val type: CommandType,
    val card: String?,
    val color: Color?
)
