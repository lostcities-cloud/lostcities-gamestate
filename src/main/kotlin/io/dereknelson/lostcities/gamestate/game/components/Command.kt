package io.dereknelson.lostcities.gamestate.game.components

data class Command (
    val gameId: Long,
    val player: String,
    val phase: Phase,
    val draw: Boolean,
    val discard: Boolean,
    val color: Color?,
    val card: Card?
) {
    fun validate(): Boolean {
        return if(phase == Phase.PLAY_OR_DISCARD) {
            !draw && card != null
        } else if (phase == Phase.DRAW) {
            draw && (color != null)
        } else {
            false
        }
    }
}