package org.example.logic.useCase

import org.example.logic.utils.BlankInputException
import org.example.logic.utils.InvalidUsernameException
import org.example.logic.utils.ProjectCreationFailedException

class Validation {
    fun validateProjectNameOrThrow(projectName: String) {
        validateInputNotBlankOrThrow(projectName)
        if (projectName.length > 16) {
            throw ProjectCreationFailedException()
        }
    }

    fun validateCreateMateUsernameAndPasswordOrThrow(
        username: String,
        password: String,
    ) {
        validateInputNotBlankOrThrow(username)
        if (hasSpace(username)) {
            throw InvalidUsernameException()
        }

        validateInputNotBlankOrThrow(password)
    }

    private fun hasSpace(username: String) = username.any { it.isWhitespace() }

    fun validateLoginUsernameAndPasswordOrThrow(
        username: String,
        password: String,
    ) {
        validateInputNotBlankOrThrow(username)
        validateInputNotBlankOrThrow(password)
    }

    fun validateInputNotBlankOrThrow(input: String) {
        if (input.isBlank()) {
            throw BlankInputException()
        }
    }
}
