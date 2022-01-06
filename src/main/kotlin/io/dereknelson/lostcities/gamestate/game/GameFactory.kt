package io.dereknelson.lostcities.gamestate.game

import io.dereknelson.lostcities.common.model.game.components.Card
import io.dereknelson.lostcities.common.model.game.components.Color
import io.dereknelson.lostcities.gamestate.persistance.MatchEntity
import org.springframework.stereotype.Service
import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.random.Random

@Service
class GameFactory {

    fun build(match: MatchEntity) : GameState {
        val shuffledCards = buildDeck()
            .shuffled(Random(match.seed))

        return GameState(
            match.id,
            match.players,
            LinkedHashSet(shuffledCards)
        )
    }

    fun buildDeck() : List<Card> {
        val cards : MutableList<Card> = mutableListOf()

        Color.values().forEach {
            cards.addAll(buildCardsForColor(it))
        }

        return cards
    }

    private fun buildCardsForColor(color: Color) : List<Card> {
        val cards : MutableList<Card> = IntStream.range(1, 10)
            .mapToObj { Card(color, it) }
            .collect(Collectors.toList())

        cards.add(Card(color, 0, true))
        cards.add(Card(color, 0, true))

        return cards
    }
}