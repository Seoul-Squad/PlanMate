package logic.useCase

import io.mockk.every
import io.mockk.mockk
import org.example.logic.models.User
import org.example.logic.models.UserRole
import org.example.logic.repositries.AuthenticationRepository
import org.example.logic.useCase.GetCurrentUserUseCase
import org.example.logic.utils.NoLoggedInUserException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetCurrentUserUseCaseTest {
    private lateinit var authRepository: AuthenticationRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    @BeforeEach
    fun setUp() {
        authRepository = mockk()
        getCurrentUserUseCase = GetCurrentUserUseCase(authRepository)
    }

    @Test
    fun `should return current user when user is logged in`() {
        val user =
            User(
                Uuid.random(),
                "fares ",
                UserRole.USER,
                User.AuthenticationMethod.Password("f4556fd41d3s964s"),
            )

        every { authRepository.getCurrentUser() } returns
            io.reactivex.rxjava3.core.Single
                .just(user)

        val testObserver = getCurrentUserUseCase().test()

        testObserver.assertComplete()
        testObserver.assertValue { returnedUser ->
            returnedUser == user
        }
    }

    @Test
    fun `should throw NoLoggedInUserException when user is not logged in`() {
        every { authRepository.getCurrentUser() } returns
            io.reactivex.rxjava3.core.Single
                .error(NoLoggedInUserException())

        val testObserver = getCurrentUserUseCase().test()

        testObserver.assertError { throwable ->
            throwable is NoLoggedInUserException
        }
    }
}
