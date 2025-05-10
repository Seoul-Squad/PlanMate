package org.example.data.source.local.csv.utils.mapper

import org.example.logic.models.User
import org.example.logic.models.UserRole
import org.example.logic.utils.toUuid
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun User.toCsvRow(): String = "${id.toHexString()},$username,$password,$role"

fun List<User>.toCsvRows(): List<String> =
    this.map {
        it.toCsvRow()
    }

@OptIn(ExperimentalUuidApi::class)
fun List<String>.toUsers(): List<User> {
    val usersList = mutableListOf<User>()

    this.forEach { row ->
        val partsOfUser = row.split(",")
        usersList.add(User(partsOfUser[0].toUuid(), partsOfUser[1], partsOfUser[2], UserRole.valueOf(partsOfUser[3])))
    }

    return usersList
}
