package io.dereknelson.lostcities.gamestate.api

import io.dereknelson.lostcities.models.commands.CommandDto
import io.dereknelson.lostcities.models.matches.PlayerEvent
import io.dereknelson.lostcities.models.state.Card
import io.dereknelson.lostcities.models.state.PlayArea

class PlayerViewDto(
    val id: Long,
    val deckRemaining: Int,
    val player: String,
    val isPlayerTurn: Boolean,
    val hand: MutableList<Card>,
    val playAreas: Map<String, PlayArea>,
    val discard: PlayArea,
    val playerEvents: List<PlayerEvent>,
    var log: List<CommandDto> = listOf(),
)
