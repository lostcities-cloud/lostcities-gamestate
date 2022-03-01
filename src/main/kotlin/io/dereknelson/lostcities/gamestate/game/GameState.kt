package io.dereknelson.lostcities.gamestate.game

import io.dereknelson.lostcities.common.model.match.UserPair
import io.dereknelson.lostcities.models.state.Card
import io.dereknelson.lostcities.models.state.Color
import io.dereknelson.lostcities.models.state.PlayArea
import io.dereknelson.lostcities.gamestate.persistance.MatchEntity
import io.dereknelson.lostcities.models.matches.PlayerEvent
import io.dereknelson.lostcities.models.matches.PlayerEventType
import kotlin.collections.LinkedHashSet
import kotlin.random.Random

class GameState(
    val id: Long,
    players: UserPair,
    private val deck: LinkedHashSet<Card>,
    val matchEntity: MatchEntity,
    seed: Random
) {
    var currentPlayer: String
    val playerEvents = mutableListOf<PlayerEvent>()

    val playerAreas: Map<String, PlayArea> = mapOf(
        players.user1 to PlayArea(),
        players.user2!! to PlayArea()
    )

    private var nextPlayer: String
    private val discard = PlayArea()

    private val playerHands: Map<String, MutableMap<String, Card>> = mapOf(
        players.user1 to mutableMapOf(),
        players.user2!! to mutableMapOf()
    )

    init {
        val turnOrder = players.toList().shuffled(seed)

        currentPlayer = turnOrder[0]
        nextPlayer = turnOrder[1]

        drawXCards(players.user1, 8)
        drawXCards(players.user2!!, 8)
    }

    fun isGameOver(): Boolean {
        return deck.isEmpty()
    }

    fun calculateScores(): Map<String, Int> {
        return playerAreas.keys.map {
            it to calculateScoreForPlayer(it)
        }.toMap()
    }

    fun drawCard(player: String) {
        if (deck.isNotEmpty()) {
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

    fun drawFromDiscard(player: String, color: Color) {
        if (!canDrawFromDiscard(color)) {
            throw RuntimeException("Cannot draw from discard: $color")
        }

        val drawn = discard.get(color).removeLast()
        getHand(player)[drawn.id] = drawn
        playerEvents.add(PlayerEvent(id, currentPlayer, PlayerEventType.DRAW_CARD, drawn.id, null))
    }

    fun playCard(player: String, card: String) {
        if (!isCardInHand(player, card)) {
            throw RuntimeException("Cannot draw from discard: $card")
        }

        val toPlay = removeCardFromHand(player, card)
        getPlayerArea(player).get(toPlay!!.color).add(toPlay)
        playerEvents.add(PlayerEvent(id, currentPlayer, PlayerEventType.PLAY_CARD, toPlay.id, null))

    }

    fun discard(player: String, card: String) {
        if (!isCardInHand(player, card)) {
            throw RuntimeException("Cannot discard $card that is not in hand")
        }

        val removed = removeCardFromHand(player, card)
        discard.get(removed!!.color).add(0, removed)
        playerEvents.add(PlayerEvent(id, currentPlayer, PlayerEventType.DISCARD_CARD, removed.id, null))
    }

    fun isCardInHand(player: String, card: String): Boolean {
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

    private fun calculateScoreForPlayer(player: String): Int {
        val playerArea = getPlayerArea(player)

        return Color.values().map {
            scoreCards(playerArea.get(it))
        }.sumOf { it }
    }

    private fun scoreCards(cards: List<Card>): Int {
        return if(cards.isEmpty()) {
            0
        } else {
            (-20 + sumCards(cards)) * multiplier(cards)
        }
    }

    private fun multiplier(cards: List<Card>): Int {
        return 1 + cards.filter { it.isMultiplier }.size
    }

    private fun sumCards(cards: List<Card>): Int {
        return cards.sumOf { it.value }
    }

    private fun drawXCards(player: String, number: Int) {
        for (x in 0 until number) {
            drawCard(player)
        }
    }

    private fun canDrawFromDiscard(color: Color): Boolean {
        return !discard.isEmpty(color)
    }

    private fun removeCardFromHand(player: String, card: String): Card? {
        return getHand(player).remove(card)
    }

    private fun getHand(player: String): MutableMap<String, Card> {
        return playerHands[player]!!
    }

    private fun getPlayerArea(player: String): PlayArea {
        return playerAreas[player]!!
    }

    private fun UserPair.toList(): List<String> {
        return listOf(user1, user2!!)
    }

    fun asPlayerView(player: String): PlayerViewDto {
        return PlayerViewDto(
            id = this.id,
            deckRemaining = this.deck.size,
            player = player,
            isPlayerTurn = this.currentPlayer == player,
            hand = this.playerHands[player]!!.values.toMutableList(),
            playAreas = this.playerAreas,
            discard = this.discard,
            playerEvents
        )
    }
}
