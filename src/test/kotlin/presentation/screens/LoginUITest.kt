package org.example.presentation.screens

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.User
import org.example.logic.models.UserRole
import org.example.logic.useCase.LoginUserUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import presentation.utils.io.Reader
import presentation.utils.io.Viewer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class LoginUITest {
    private lateinit var loginUserUseCase: LoginUserUseCase
    private lateinit var readerMock: Reader
    private lateinit var viewerMock: Viewer
    private lateinit var onNavigateToAdminHomeMock: () -> Unit
    private lateinit var onNavigateToShowAllProjectsMock: (userRole: UserRole) -> Unit

    @BeforeEach
    fun setUp() {
        loginUserUseCase = mockk(relaxed = true)
        readerMock = mockk()
        viewerMock = mockk(relaxed = true)
        onNavigateToAdminHomeMock = mockk(relaxed = true)
        onNavigateToShowAllProjectsMock = mockk(relaxed = true)
    }

    @Test
    fun `should navigate to Admin Home when login is successful and role is ADMIN`() {
        val username = "admin"
        val password = "admin"

        val user =
            User(
                id = Uuid.random(),
                username = username,
                role = UserRole.ADMIN,
                authMethod = User.AuthenticationMethod.Password(password),
            )

        every { readerMock.readString() } returns username andThen password
        every { loginUserUseCase(username, password) } returns Single.just(user)

        LoginUI(
            onNavigateToAdminHomeMock,
            onNavigateToShowAllProjectsMock,
            loginUserUseCase,
            readerMock,
            viewerMock,
        )

        verify { onNavigateToAdminHomeMock() }
        verify(exactly = 5) { viewerMock.display(any()) }
    }

    @Test
    fun `should navigate to Show All Projects when login is successful and role is USER`() {
        val username = "user"
        val password = "userPassword"

        // Create a mock User object directly with the role set to USER
        val user =
            User(id = Uuid.random(), username = username, role = UserRole.USER, authMethod = User.AuthenticationMethod.Password(password))

        every { readerMock.readString() } returns username andThen password
        every { loginUserUseCase(username, password) } returns Single.just(user)

        LoginUI(
            onNavigateToAdminHomeMock,
            onNavigateToShowAllProjectsMock,
            loginUserUseCase,
            readerMock,
            viewerMock,
        )

        verify { onNavigateToShowAllProjectsMock(UserRole.USER) }
        verify(exactly = 5) { viewerMock.display(any()) }
    }
}
