import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.observers.TestObserver
import org.example.logic.repositries.AuthenticationRepository
import org.example.logic.useCase.LogoutUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LogoutUseCaseTest {
    private lateinit var authenticationRepository: AuthenticationRepository
    private lateinit var logoutUseCase: LogoutUseCase

    @BeforeEach
    fun setUp() {
        authenticationRepository = mockk(relaxed = true)
        logoutUseCase = LogoutUseCase(authenticationRepository)
    }

    @Test
    fun `should call logout on authentication repository when useCase invoked`() {
        every { authenticationRepository.logout() } returns Completable.complete()

        val testObserver: TestObserver<Void> = logoutUseCase().test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        verify(exactly = 1) { authenticationRepository.logout() }
    }
}
