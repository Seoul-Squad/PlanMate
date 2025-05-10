package org.example.data.source.remote.mongo.utils.mapper

import org.example.data.source.remote.models.UserDTO
import org.example.logic.models.User
import org.example.logic.models.UserRole
import org.example.logic.utils.toUuid
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun UserDTO.toUser(): User =
    User(
        id = id.toUuid(),
        username = username,
        password = password,
        role = UserRole.valueOf(role),
    )

@OptIn(ExperimentalUuidApi::class)
fun User.toUserDTO(): UserDTO =
    UserDTO(
        id = id.toHexString(),
        username = username,
        password = password,
        role = role.name,
    )
