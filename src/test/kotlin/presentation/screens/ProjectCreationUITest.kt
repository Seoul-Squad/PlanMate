import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.Project
import org.example.logic.useCase.CreateProjectUseCase
import org.example.logic.utils.*
import org.example.presentation.screens.ProjectCreationUI
import org.junit.jupiter.api.Test
import presentation.utils.io.Reader
import presentation.utils.io.Viewer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProjectCreationUITest {
    private val createProjectUseCase: CreateProjectUseCase = mockk(relaxed = true)
    private val onBackMock: () -> Unit = mockk(relaxed = true)
    private val readerMock: Reader = mockk(relaxed = true)
    private val viewerMock: Viewer = mockk(relaxed = true)

    @Test
    fun `should return success message when project is created successfully`() {
        val projectName = "New Project"
        val project =
            Project(
                id = Uuid.random(),
                name = projectName,
            )
        every { createProjectUseCase(projectName) } returns Single.just(project)

        ProjectCreationUI(createProjectUseCase, onBackMock, readerMock, viewerMock)

        verify { viewerMock.display(any()) }
        verify { onBackMock.invoke() }
    }

    @Test
    fun `should return error message when project name is blank`() {
        every { readerMock.readString() } returns ""
        every { createProjectUseCase(any()) } throws BlankInputException()

        ProjectCreationUI(createProjectUseCase, onBackMock, readerMock, viewerMock)

        verify { viewerMock.display(any()) }
        verify { onBackMock.invoke() }
    }

    @Test
    fun `should return error message when project creation fails`() {
        every { readerMock.readString() } returns "Project Name"
        every { createProjectUseCase(any()) } throws ProjectCreationFailedException()

        ProjectCreationUI(createProjectUseCase, onBackMock, readerMock, viewerMock)

        verify { viewerMock.display(any()) }
        verify { onBackMock.invoke() }
    }

    @Test
    fun `should return error message when no user is logged in`() {
        every { readerMock.readString() } returns "Project Name"
        every { createProjectUseCase(any()) } throws NoLoggedInUserException()

        ProjectCreationUI(createProjectUseCase, onBackMock, readerMock, viewerMock)

        verify { viewerMock.display(any()) }
        verify { onBackMock.invoke() }
    }

    @Test
    fun `should return error message when the user is unauthorized to create a project`() {
        every { readerMock.readString() } returns "Project Name"
        every { createProjectUseCase(any()) } throws UnauthorizedAccessException()

        ProjectCreationUI(createProjectUseCase, onBackMock, readerMock, viewerMock)

        verify { viewerMock.display(any()) }
        verify { onBackMock.invoke() }
    }

    @Test
    fun `should return error message when there is an invalid audit input`() {
        every { readerMock.readString() } returns "Project Name"
        every { createProjectUseCase(any()) } throws InvalidAuditInputException()

        ProjectCreationUI(createProjectUseCase, onBackMock, readerMock, viewerMock)

        verify { viewerMock.display(any()) }
        verify { onBackMock.invoke() }
    }

    @Test
    fun `should return error message when an unexpected exception occurs`() {
        val exceptionMessage = "Unexpected error"
        every { readerMock.readString() } returns "Project Name"
        every { createProjectUseCase(any()) } throws Exception(exceptionMessage)

        ProjectCreationUI(createProjectUseCase, onBackMock, readerMock, viewerMock)

        verify { viewerMock.display(any()) }
        verify { onBackMock.invoke() }
    }
}
