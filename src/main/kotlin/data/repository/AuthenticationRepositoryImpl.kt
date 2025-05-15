package org.example.data.repository

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.data.repository.mapper.mapExceptionsToDomainExceptionRx
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
    override fun getCurrentUser(): Single<User> = remoteAuthenticationDataSource.getCurrentUser()

    @OptIn(ExperimentalUuidApi::class)
    override fun createUserWithPassword(
        username: String,
        password: String,
    ): Single<User> =
        mapExceptionsToDomainExceptionRx(UserCreationFailedException()) {
            val hashedPassword = hashWithMD5(password)
            val user = User(username = username, authMethod = User.AuthenticationMethod.Password(hashedPassword), role = UserRole.USER)
            remoteAuthenticationDataSource.saveUser(user)
        }

    override fun loginWithPassword(
        username: String,
        password: String,
    ): Single<User> =
        mapExceptionsToDomainExceptionRx(UserNotFoundException()) {
            val hashedPassword = hashWithMD5(password)
            remoteAuthenticationDataSource.loginWithPassword(username, hashedPassword)
        }

    override fun logout(): Completable = remoteAuthenticationDataSource.logout()

    override fun getAllUsers(): Single<List<User>> =
        mapExceptionsToDomainExceptionRx(UserNotFoundException()) {
            remoteAuthenticationDataSource.getAllUsers()
        }
}
