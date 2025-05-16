package logic.useCase

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.User
import org.example.logic.models.UserRole
import org.example.logic.repositries.AuthenticationRepository
import org.example.logic.useCase.CreateUserUseCase
import org.example.logic.useCase.Validation
import org.example.logic.utils.BlankInputException
import org.example.logic.utils.InvalidUsernameException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CreateUserUseCaseTest {
    private lateinit var authenticationRepository: AuthenticationRepository
    private lateinit var validation: Validation
    private lateinit var createUserUseCase: CreateUserUseCase

    private val id1 = Uuid.random()
    private val id2 = Uuid.random()

    private val users =
        listOf(
            User(id1, "testUsername", UserRole.USER, User.AuthenticationMethod.Password("testPassword")),
            User(id2, "testUsername2", UserRole.USER, User.AuthenticationMethod.Password("testPassword2")),
        )

    @BeforeEach
    fun setUp() {
        authenticationRepository = mockk(relaxed = true)
        validation = mockk(relaxed = true)
        createUserUseCase = CreateUserUseCase(authenticationRepository, validation)
    }

    @Test
    fun `should return user data when user enters new valid username and password`() {
        val newUser =
            User(
                Uuid.random(),
                "newTestUsername",
                UserRole.USER,
                User.AuthenticationMethod.Password("testPassword"),
            )

        every { validation.validateCreateMateUsernameAndPasswordOrThrow(any(), any()) } returns Unit
        every { authenticationRepository.createUserWithPassword(any(), any()) } returns Single.just(newUser)

        val result = createUserUseCase("newTestUsername", "testPassword").blockingGet()

        assertThat(result.username).isNotIn(users.map { it.username })
    }

    @Test
    fun `should throw BlankInputException when username is blank`() {
        every { validation.validateCreateMateUsernameAndPasswordOrThrow(any(), any()) } throws BlankInputException()

        assertThrows<BlankInputException> {
            createUserUseCase("", "testPassword").blockingGet()
        }
    }

    @Test
    fun `should throw BlankInputException when password is blank`() {
        every { validation.validateCreateMateUsernameAndPasswordOrThrow(any(), any()) } throws BlankInputException()

        assertThrows<BlankInputException> {
            createUserUseCase("newTestUsername", "").blockingGet()
        }
    }

    @Test
    fun `should throw InvalidUsernameException when username contains spaces`() {
        every {
            validation.validateCreateMateUsernameAndPasswordOrThrow(
                any(),
                any(),
            )
        } throws InvalidUsernameException()

        assertThrows<InvalidUsernameException> {
            createUserUseCase("new testUsername", "testPassword").blockingGet()
        }
    }
}
