package org.example.logic.useCase

import io.reactivex.rxjava3.core.Completable
import org.example.logic.repositries.AuthenticationRepository

class LogoutUseCase(
    private val authenticationRepository: AuthenticationRepository,
) {
    operator fun invoke(): Completable = authenticationRepository.logout()
}
