package org.example.data.repository

import org.example.data.repository.mapper.mapExceptionsToDomainException
import org.example.data.repository.sources.remote.RemoteAuthenticationDataSource
import org.example.data.repository.utils.hashWithMD5
import org.example.logic.models.User
import org.example.logic.models.UserRole
import org.example.logic.repositries.AuthenticationRepository
import org.example.logic.utils.UserCreationFailedException
import org.example.logic.utils.UserNotFoundException
import kotlin.uuid.ExperimentalUuidApi

class AuthenticationRepositoryImpl(
    private val remoteAuthenticationDataSource: RemoteAuthenticationDataSource,
) : AuthenticationRepository {
    override suspend fun getCurrentUser(): User? = remoteAuthenticationDataSource.getCurrentUser()

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createMate(
        username: String,
        password: String,
    ): User =
        mapExceptionsToDomainException(UserCreationFailedException()) {
            val hashedPassword = hashWithMD5(password)
            val user = User(username = username, password = hashedPassword, role = UserRole.USER)
            remoteAuthenticationDataSource.saveUser(user)
            user
        }

    override suspend fun login(
        username: String,
        password: String,
    ): User =
        mapExceptionsToDomainException(UserNotFoundException()) {
            val hashedPassword = hashWithMD5(password)
            remoteAuthenticationDataSource.login(username, hashedPassword)
        }

    override suspend fun logout() {
        remoteAuthenticationDataSource.logout()
    }

    override suspend fun getAllUsers(): List<User> =
        mapExceptionsToDomainException(UserNotFoundException()) {
            remoteAuthenticationDataSource.getAllUsers()
        }
}
