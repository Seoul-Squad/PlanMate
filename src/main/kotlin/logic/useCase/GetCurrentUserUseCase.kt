package org.example.logic.useCase

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.User
import org.example.logic.repositries.AuthenticationRepository

class GetCurrentUserUseCase(
    private val authenticationRepository: AuthenticationRepository,
) {
    operator fun invoke(): Single<User> = authenticationRepository.getCurrentUser()
}
