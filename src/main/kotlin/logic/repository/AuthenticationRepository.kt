package org.example.logic.repositries

import org.example.logic.models.User

interface AuthenticationRepository {
    suspend fun getCurrentUser(): User?

    suspend fun createUserWithPassword(
        username: String,
        password: String,
    ): User

    suspend fun loginWithPassword(
        username: String,
        password: String,
    ): User

    suspend fun logout()

    suspend fun getAllUsers(): List<User>
}
