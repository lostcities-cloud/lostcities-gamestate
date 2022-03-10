package io.dereknelson.lostcities.gamestate.games.dto

import io.dereknelson.lostcities.models.commands.CommandDto

data class TurnCommandRequest(
    val playOrDiscard: CommandDto,
    val draw: CommandDto,
)
