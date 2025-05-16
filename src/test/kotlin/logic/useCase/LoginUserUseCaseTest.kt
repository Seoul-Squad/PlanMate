package logic.useCase

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import mockdata.createUser
import org.example.logic.models.User
import org.example.logic.repositries.AuthenticationRepository
import org.example.logic.useCase.LoginUserUseCase
import org.example.logic.useCase.Validation
import org.example.logic.utils.BlankInputException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class LoginUserUseCaseTest {
    private lateinit var authenticationRepository: AuthenticationRepository
    private lateinit var validation: Validation
    private lateinit var loginUserUseCase: LoginUserUseCase
    private val ids = List(6) { Uuid.random() }
    private val users =
        listOf(
            createUser(ids[0], "testUsername"),
            createUser(ids[1], "testUsername2"),
        )

    @BeforeEach
    fun setUp() {
        authenticationRepository = mockk(relaxed = true)
        validation = mockk(relaxed = true)
        loginUserUseCase = LoginUserUseCase(authenticationRepository, validation)
    }

    @Test
    fun `should return user data when user enter username and password that exists in users data`() {
        every { validation.validateLoginUsernameAndPasswordOrThrow("testUsername", "testPassword") } returns Unit
        every {
            authenticationRepository.loginWithPassword(
                "testUsername",
                "testPassword",
            )
        } returns Single.just(users[0])

        val testObserver: TestObserver<User> = loginUserUseCase("testUsername", "testPassword").test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it == users[0]
        }
    }

    @Test
    fun `should throw exception when validation fails`() {
        val username = ""
        val password = ""

        every {
            validation.validateLoginUsernameAndPasswordOrThrow(username, password)
        } throws BlankInputException()

        assertThrows<BlankInputException> {
            loginUserUseCase(username, password)
        }
    }
}
