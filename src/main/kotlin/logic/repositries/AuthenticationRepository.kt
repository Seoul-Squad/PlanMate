package org.example.logic.repositries

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.User

interface AuthenticationRepository {
    fun getCurrentUser(): Single<User>

    fun createUserWithPassword(
        username: String,
        password: String,
    ): Single<User>

    fun loginWithPassword(
        username: String,
        password: String,
    ): Single<User>

    fun logout(): Completable

    fun getAllUsers(): Single<List<User>>
}
