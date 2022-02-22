package io.dereknelson.lostcities.gamestate.game

import io.dereknelson.lostcities.gamestate.game.state.Card
import io.dereknelson.lostcities.gamestate.game.state.PlayArea
import io.dereknelson.lostcities.models.matches.PlayerEvent

class PlayerViewDto(
    val id: Long,
    val deckRemaining: Int,
    val player: String,
    val isPlayerTurn: Boolean,
    val hand: MutableList<Card>,
    val playAreas: Map<String, PlayArea>,
    val discard: PlayArea,
    val playerEvents: List<PlayerEvent>
)
