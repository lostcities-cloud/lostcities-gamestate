package io.dereknelson.lostcities.gamestate.commands

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("/api/")
class GameCommandApi {

    @GetMapping("/api/games/:id")
    fun getGameState(): String {
        return "test"
    }

}