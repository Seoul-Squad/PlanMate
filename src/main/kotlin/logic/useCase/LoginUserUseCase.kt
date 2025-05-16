package org.example.logic.useCase

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.User
import org.example.logic.repositries.AuthenticationRepository

class LoginUserUseCase(
    private val authenticationRepository: AuthenticationRepository,
    private val validation: Validation,
) {
    operator fun invoke(
        username: String,
        password: String,
    ): Single<User> {
        validation.validateLoginUsernameAndPasswordOrThrow(username, password)
        return authenticationRepository.loginWithPassword(username, password)
    }
}
