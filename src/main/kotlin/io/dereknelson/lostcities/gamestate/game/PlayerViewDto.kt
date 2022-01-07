package io.dereknelson.lostcities.gamestate.game

import io.dereknelson.lostcities.gamestate.game.state.Card
import io.dereknelson.lostcities.gamestate.game.state.Phase
import io.dereknelson.lostcities.gamestate.game.state.PlayArea


class PlayerViewDto(
    val id: Long,
    val gamePhase: Phase,
    val deckRemaining: Int,
    val player: String,
    val isPlayerTurn: Boolean,
    val hand: MutableList<Card>,
    val playAreas: Map<String, PlayArea>,
    val discard: PlayArea
)