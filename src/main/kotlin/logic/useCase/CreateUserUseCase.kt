package org.example.logic.useCase

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.User
import org.example.logic.repositries.AuthenticationRepository

class CreateUserUseCase(
    private val authenticationRepository: AuthenticationRepository,
    private val validation: Validation,
) {
    operator fun invoke(
        username: String,
        password: String,
    ): Single<User> {
        validation.validateCreateMateUsernameAndPasswordOrThrow(username, password)
        return authenticationRepository.createUserWithPassword(username, password)
    }
}
