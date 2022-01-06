package io.dereknelson.lostcities.gamestate.game


import io.dereknelson.lostcities.common.model.match.UserPair
import io.dereknelson.lostcities.gamestate.game.components.Card
import io.dereknelson.lostcities.gamestate.game.components.Color
import io.dereknelson.lostcities.gamestate.game.components.Phase
import io.dereknelson.lostcities.gamestate.game.components.PlayArea
import kotlin.collections.LinkedHashSet

class GameState(
    val id : Long,
    players : UserPair,
    val deck : LinkedHashSet<Card>
) {
    var phase = Phase.PLAY_OR_DISCARD
    var currentPlayer = players.user1
    val discard = PlayArea()

    val playerAreas: Map<String, PlayArea> = mapOf(
        players.user1 to PlayArea(),
        players.user2!! to PlayArea()
    )

    val playerHands: Map<String, MutableList<Card>> = mapOf(
        players.user1 to mutableListOf(),
        players.user2!! to mutableListOf()
    )

    init {
        drawXCards(players.user1, 5)
        drawXCards(players.user2!!, 5)
    }

    fun nextPhase() {
        phase = if(phase == Phase.PLAY_OR_DISCARD) {
            Phase.DRAW
        } else {
            Phase.PLAY_OR_DISCARD
        }
    }

    fun drawCard(player: String) {
        if(deck.isNotEmpty()) {
            val drawn = deck.first()
            deck.remove(drawn)
            getHand(player).add(drawn)
        }
    }

    fun drawXCards(player: String, number: Int) {
        for(x in 0 until number) {
            drawCard(player)
        }
    }

    fun drawFromDiscard(player : String, color: Color) {
        if(canDrawFromDiscard(color)) {
            val cards = discard.get(color)
            val drawn = cards.first()
            cards.remove(drawn)
            getHand(player).add(drawn)
        }
    }

    fun playCard(player : String, card : Card) {
        if(isCardInHand(player, card)) {
            getPlayerArea(player).get(card.color).add(card)
        }
    }

    fun discard(player : String, card : Card) {
        if(isCardInHand(player, card)) {
            discard.get(card.color).add(card)
        }
    }

    private fun canDrawFromDiscard(color : Color) : Boolean {
        return !discard.isEmpty(color)
    }

    private fun isCardInHand(player : String, card : Card) : Boolean {
        return getHand(player).contains(card)
    }

    private fun getHand(player : String) : MutableList<Card> {
        return playerHands[player]!!
    }

    private fun getPlayerArea(player : String) : PlayArea {
        return playerAreas[player]!!
    }
}
