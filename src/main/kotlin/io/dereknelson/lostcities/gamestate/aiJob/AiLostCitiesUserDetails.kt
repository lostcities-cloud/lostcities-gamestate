package io.dereknelson.lostcities.gamestate.aiJob

import io.dereknelson.lostcities.common.auth.LostCitiesUserDetails
import io.dereknelson.lostcities.common.auth.entity.UserRef

internal class AiLostCitiesUserDetails(
    login: String,
) : LostCitiesUserDetails(
    id = 0,
    login = login,
    email = "",
    password = "",
    userRef = UserRef(id = 0),
    token = "",
    authority = emptySet(),
    accountNonExpired = true,
    accountNonLocked = true,
    credentialsNonExpired = true,
    enabled = true,
)
