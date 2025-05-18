package org.example.presentation.screens

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.User
import org.example.logic.models.UserRole
import org.example.logic.useCase.CreateUserUseCase
import org.example.logic.utils.BlankInputException
import org.example.logic.utils.InvalidUsernameException
import org.example.logic.utils.UserAlreadyExistsException
import org.junit.jupiter.api.Test
import presentation.utils.io.Reader
import presentation.utils.io.Viewer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UserCreationUITest {
    private val createUserUseCase: CreateUserUseCase = mockk(relaxed = true)
    private val readerMock: Reader = mockk(relaxed = true)
    private val viewerMock: Viewer = mockk(relaxed = true)
    private val onBackMock: () -> Unit = mockk(relaxed = true)

    @Test
    fun `should return success message when user is created successfully`() {
        val username = "newUser"
        val password = "password123"
        val user =
            User(
                id = Uuid.random(),
                username = username,
                role = UserRole.USER,
                authMethod = User.AuthenticationMethod.Password(password),
            )
        every { readerMock.readString() } returns username andThen password
        coEvery { createUserUseCase(username, password) } returns Single.just(user)

        UserCreationUI(createUserUseCase, readerMock, viewerMock, onBackMock)

        verify { viewerMock.display(any()) }
        verify { onBackMock.invoke() }
    }

    @Test
    fun `should return error message when username is blank`() {
        every { readerMock.readString() } returns "" andThen "password123"
        coEvery { createUserUseCase(any(), any()) } throws BlankInputException()

        UserCreationUI(createUserUseCase, readerMock, viewerMock, onBackMock)

        verify { viewerMock.display(any()) }
        verify { onBackMock.invoke() }
    }

    @Test
    fun `should return error message when password is blank`() {
        every { readerMock.readString() } returns "newUser" andThen ""
        coEvery { createUserUseCase(any(), any()) } throws BlankInputException()

        UserCreationUI(createUserUseCase, readerMock, viewerMock, onBackMock)

        verify { viewerMock.display(any()) }
        verify { onBackMock.invoke() }
    }

    @Test
    fun `should return error message when username is invalid`() {
        every { readerMock.readString() } returns "invalid#user" andThen "password123"
        coEvery { createUserUseCase(any(), any()) } throws InvalidUsernameException()

        UserCreationUI(createUserUseCase, readerMock, viewerMock, onBackMock)

        verify { viewerMock.display(any()) }
        verify { onBackMock.invoke() }
    }

    @Test
    fun `should return error message when user already exists`() {
        every { readerMock.readString() } returns "existingUser" andThen "password123"
        coEvery { createUserUseCase(any(), any()) } throws UserAlreadyExistsException()

        UserCreationUI(createUserUseCase, readerMock, viewerMock, onBackMock)

        verify { viewerMock.display(any()) }
        verify { onBackMock.invoke() }
    }

    @Test
    fun `should return error message when an unexpected exception occurs`() {
        val exceptionMessage = "Unexpected error"
        every { readerMock.readString() } returns "newUser" andThen "password123"
        coEvery { createUserUseCase(any(), any()) } throws Exception(exceptionMessage)

        UserCreationUI(createUserUseCase, readerMock, viewerMock, onBackMock)

        verify { viewerMock.display(any()) }
        verify { onBackMock.invoke() }
    }
}
