package io.dereknelson.lostcities.gamestate.game

import io.dereknelson.lostcities.common.model.game.components.Card
import io.dereknelson.lostcities.common.model.game.components.Phase
import io.dereknelson.lostcities.common.model.game.components.PlayArea

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