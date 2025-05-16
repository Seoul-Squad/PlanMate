package org.example.data.source.remote

import org.example.logic.models.UserRole
import org.example.logic.repositries.AuthenticationRepository
import org.example.logic.utils.UnauthorizedAccessException

class RoleValidationInterceptor(
    private val authenticationRepository: AuthenticationRepository,
) {
    fun <T> validateRole(
        requiredRoles: List<UserRole> = listOf(UserRole.ADMIN),
        operation: () -> T,
    ): T {
        val currentUser =
            authenticationRepository.getCurrentUser().blockingGet()

        if (currentUser.role !in requiredRoles) {
            throw UnauthorizedAccessException()
        }
        return operation()
    }
}
