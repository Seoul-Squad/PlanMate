package org.example.data.repository.sources.remote

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.User

interface RemoteAuthenticationDataSource {
    fun saveUser(user: User): Single<User>

    fun getAllUsers(): Single<List<User>>

    fun loginWithPassword(
        username: String,
        hashedPassword: String,
    ): Single<User>

    fun logout(): Completable

    fun getCurrentUser(): Single<User>
}
