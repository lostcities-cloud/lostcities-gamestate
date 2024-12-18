package io.dereknelson.lostcities.gamestate.gamestate

import io.dereknelson.lostcities.models.commands.CommandDto
import io.dereknelson.lostcities.models.matches.PlayerEvent
import io.dereknelson.lostcities.models.matches.PlayerEventType
import io.dereknelson.lostcities.models.state.Card
import io.dereknelson.lostcities.models.state.Color
import io.dereknelson.lostcities.models.state.PlayArea
import io.dereknelson.lostcities.models.state.PlayerViewDto
import io.dereknelson.lostcities.models.state.UserPair
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import kotlin.collections.LinkedHashSet

class GameState internal constructor(
    val id: Long,
    players: UserPair,
    private val deck: LinkedHashSet<Card>,
    internal val matchEntity: MatchEntity,
) {
    private val logger: Log = LogFactory.getLog(this::class.java)
    val playerEvents = mutableListOf<PlayerEvent>()
    val log = mutableListOf<CommandDto>()
    val playerAreas: Map<String, PlayArea> = mapOf(
        players.user1 to PlayArea(),
        players.user2!! to PlayArea(),
    )

    private val discard = PlayArea()

    private val playerHands: Map<String, MutableMap<String, Card>> = mapOf(
        players.user1 to mutableMapOf(),
        players.user2!! to mutableMapOf(),
    )

    var currentPlayer: String
        get() {
            return matchEntity.currentPlayer
        }
        set(value) {
            matchEntity.currentPlayer = value
        }

    private var nextPlayer: String = players.user2!!

    init {
        currentPlayer = players.user1
        drawXCards(players.user1, 8)
        drawXCards(players.user2!!, 8)
    }

    fun currentHand(): List<Card> {
        val currentHand = playerHands[currentPlayer]

        if (currentHand.isNullOrEmpty()) {
            return emptyList()
        }

        return currentHand.values.toList().sortedBy { it.value }
    }

    fun isCurrentPlayerAi(): Boolean {
        return when (currentPlayer) {
            matchEntity.player1 -> {
                matchEntity.isPlayer1Ai
            }
            matchEntity.player2 -> {
                matchEntity.isPlayer2Ai
            }
            else -> {
                false
            }
        }
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
            playerEvents.add(
                PlayerEvent(id, currentPlayer, PlayerEventType.DRAW_CARD, drawn.id, null),
            )
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
        playerEvents.add(
            PlayerEvent(id, currentPlayer, PlayerEventType.DISCARD_CARD, removed.id, null),
        )
    }

    fun isCardInHand(player: String, card: String): Boolean {
        return getHand(player).contains(card)
    }

    fun endTurn() {
        val current = nextPlayer
        val next = currentPlayer

        logger.info("GAME=$id PLAYER=$currentPlayer End Of Turn")

        playerEvents.add(PlayerEvent(id, currentPlayer, PlayerEventType.END_TURN, null, null))

        currentPlayer = current
        nextPlayer = next

        playerEvents.add(PlayerEvent(id, currentPlayer, PlayerEventType.START_TURN, null, null))
        logger.info("GAME=$id PLAYER=$currentPlayer Start Of Turn")
    }

    private fun calculateScoreForPlayer(player: String): Int {
        val playerArea = getPlayerArea(player)

        return Color.values().map {
            scoreCards(playerArea.get(it))
        }.sumOf { it }
    }

    private fun scoreCards(cards: List<Card>): Int {
        return if (cards.isEmpty()) {
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
            playerEvents,
            this.log,
        )
    }
}
