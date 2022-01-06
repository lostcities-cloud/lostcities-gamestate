package io.dereknelson.lostcities.gamestate.game.components

import com.fasterxml.jackson.annotation.JsonIgnore

data class Card(
    @JsonIgnore
    private val cardNumber: Int = 0,
    val color: Color,
    val value: Int,
    val isMultiplier: Boolean = false,
) {
    val id: String get() = "$color($cardNumber)-$value"
}
