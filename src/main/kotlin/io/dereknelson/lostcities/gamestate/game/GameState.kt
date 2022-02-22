package io.dereknelson.lostcities.gamestate.game


import io.dereknelson.lostcities.common.model.match.UserPair
import io.dereknelson.lostcities.gamestate.game.state.Card
import io.dereknelson.lostcities.gamestate.game.state.Color
import io.dereknelson.lostcities.gamestate.game.state.PlayArea
import io.dereknelson.lostcities.gamestate.persistance.MatchEntity
import io.dereknelson.lostcities.models.matches.PlayerEvent
import io.dereknelson.lostcities.models.matches.PlayerEventType

import kotlin.collections.LinkedHashSet
import kotlin.random.Random


class GameState(
    val id : Long,
    players : UserPair,
    val deck : LinkedHashSet<Card>,
    val matchEntity: MatchEntity,
    seed: Random
) {
    var currentPlayer: String
    private var nextPlayer: String
    val playerEvents = mutableListOf<PlayerEvent>()

    init {
        val turnOrder = players.toList().shuffled(seed)
        currentPlayer = turnOrder[0]
        nextPlayer = turnOrder[1]
    }

    val discard = PlayArea()

    val playerAreas: Map<String, PlayArea> = mapOf(
        players.user1 to PlayArea(),
        players.user2!! to PlayArea()
    )

    val playerHands: Map<String, MutableMap<String, Card>> = mapOf(
        players.user1 to mutableMapOf(),
        players.user2!! to mutableMapOf()
    )

    init {
        drawXCards(players.user1, 8)
        drawXCards(players.user2!!, 8)
    }

    fun drawCard(player: String) {
        if(deck.isNotEmpty()) {
            val drawn = deck.last()
            deck.remove(drawn)
            getHand(player)[drawn.id] = drawn
            playerEvents.add(PlayerEvent(id, currentPlayer, PlayerEventType.DRAW_CARD, drawn.id, null))
        }
    }

    fun canPlayCard(player: String, cardId: String): Boolean {
        val card = getHand(player)[cardId]!!
        val lastPlayed = playerAreas[player]?.get(card.color)?.lastOrNull()

        return lastPlayed === null || card.value >= lastPlayed.value
    }

    fun drawFromDiscard(player : String, color: Color) {
        if(canDrawFromDiscard(color)) {
            val drawn = discard.get(color).removeLast()
            getHand(player)[drawn.id] = drawn
            playerEvents.add(PlayerEvent(id, currentPlayer, PlayerEventType.DRAW_CARD, drawn.id, null))
        }
    }

    fun playCard(player : String, card : String) {
        if(isCardInHand(player, card)) {
            val toPlay = removeCardFromHand(player, card)
            getPlayerArea(player).get(toPlay!!.color).add(toPlay)
            playerEvents.add(PlayerEvent(id, currentPlayer, PlayerEventType.PLAY_CARD, toPlay.id, null))
        }
    }

    fun discard(player : String, card : String) {
        if(isCardInHand(player, card)) {
            val removed = removeCardFromHand(player, card)
            discard.get(removed!!.color).add(removed)
            playerEvents.add(PlayerEvent(id, currentPlayer, PlayerEventType.DISCARD_CARD, removed.id, null))
        }
    }

    fun isCardInHand(player : String, card : String) : Boolean {
        return getHand(player).contains(card)
    }

    fun endTurn() {
        val current = nextPlayer
        val next = currentPlayer

        playerEvents.add(PlayerEvent(id, currentPlayer, PlayerEventType.END_TURN, null, null))
        playerEvents.add(PlayerEvent(id, currentPlayer, PlayerEventType.START_TURN, null, null))

        currentPlayer = current
        nextPlayer = next
    }

    private fun drawXCards(player: String, number: Int) {
        for(x in 0 until number) {
            drawCard(player)
        }
    }

    private fun canDrawFromDiscard(color : Color) : Boolean {
        return !discard.isEmpty(color)
    }

    private fun removeCardFromHand(player : String, card : String) : Card? {
        return getHand(player).remove(card)
    }

    private fun getHand(player : String) : MutableMap<String, Card> {
        return playerHands[player]!!
    }

    private fun getPlayerArea(player : String) : PlayArea {
        return playerAreas[player]!!
    }

    private fun UserPair.toList(): List<String> {
        return listOf(user1, user2!!)
    }

    fun asPlayerView(player: String): PlayerViewDto {
        return PlayerViewDto(
            id=this.id,
            deckRemaining=this.deck.size,
            player=player,
            isPlayerTurn=this.currentPlayer==player,
            hand=this.playerHands[player]!!.values.toMutableList(),
            playAreas=this.playerAreas,
            discard=this.discard,
            playerEvents
        )
    }
}
