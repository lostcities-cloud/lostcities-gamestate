package io.dereknelson.lostcities.gamestate.api

import io.dereknelson.lostcities.common.model.match.UserPair
import io.dereknelson.lostcities.gamestate.matches.MatchEntity
import io.dereknelson.lostcities.models.state.Card
import io.dereknelson.lostcities.models.state.Color
import org.springframework.stereotype.Service
import java.util.stream.IntStream
import kotlin.random.Random

@Service
class GameFactory {

    fun build(match: MatchEntity): GameState {
        val random = Random(match.seed)
        val shuffledCards = buildDeck()
            .shuffled(random)

        val gameState = GameState(
            match.id,
            players = UserPair(
                user1 = match.player1,
                user2 = match.player2,
            ),
            LinkedHashSet(shuffledCards),
            match,
            random,
        )

        gameState.playerEvents.clear()

        return gameState
    }

    fun buildDeck(): List<Card> {
        val cards: MutableList<Card> = mutableListOf()

        Color.values().forEach {
            cards.addAll(buildCardsForColor(it))
        }

        return cards
    }

    private fun buildCardsForColor(color: Color): List<Card> {
        var i = 1
        val cards = mutableListOf<Card>()
        cards.add(Card(i++, color, 0, true))
        cards.add(Card(i++, color, 0, true))

        IntStream.range(1, 11)
            .mapToObj { Card(i++, color, it) }
            .forEach { cards.add(it) }

        return cards
    }
}
