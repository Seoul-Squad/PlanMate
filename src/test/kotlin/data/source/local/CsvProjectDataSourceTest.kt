package data.source.local

import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.example.data.source.local.csv.CsvProjectDataSource
import org.example.data.source.local.csv.utils.CSVReader
import org.example.data.source.local.csv.utils.CSVWriter
import org.example.data.source.local.csv.utils.mapper.toCsvLines
import org.example.logic.models.Project
import org.example.logic.utils.ProjectCreationFailedException
import org.example.logic.utils.ProjectNotChangedException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CsvProjectDataSourceTest {
    private lateinit var mockCsvReader: CSVReader
    private lateinit var mockCsvWriter: CSVWriter
    private lateinit var dataSource: CsvProjectDataSource
    private lateinit var testProjects: List<Project>
    private lateinit var testCsvLines: List<String>

    private val projectId = Uuid.random()
    private val projectId2 = Uuid.random()

    @BeforeEach
    fun setUp() {
        mockCsvReader = mockk(relaxed = true)
        mockCsvWriter = mockk(relaxed = true)

        testProjects =
            listOf(
                Project(
                    id = projectId,
                    name = "Project 1"
                ),
                Project(
                    id = projectId2,
                    name = "Project 2"
                ),
            )
        testCsvLines = testProjects.toCsvLines()

        every { mockCsvReader.readLines() } returns testCsvLines
        every { mockCsvWriter.writeLines(any()) } just runs

        dataSource = CsvProjectDataSource(mockCsvReader, mockCsvWriter)
    }

    @Test
    fun `initialization loads projects from file`() {
        verify(exactly = 1) { mockCsvReader.readLines() }

        val allProjects = dataSource.getAllProjects()
        assertThat(allProjects).hasSize(2)
        assertThat(allProjects[0].id).isEqualTo(projectId)
        assertThat(allProjects[1].id).isEqualTo(projectId2)
    }

    @Test
    fun `createProject should adds the new project and saves to file`() {
        val newProject =
            Project(
                id = projectId,
                name = "Project 3"
            )

        val result = dataSource.createProject(newProject)

        assertThat(result).isEqualTo(newProject)
        verify { mockCsvWriter.writeLines(any()) }
        val allProjects = dataSource.getAllProjects()
        assertThat(allProjects).hasSize(3)
        assertThat(allProjects).contains(newProject)
    }

    @Test
    fun `createProject should throws ProjectCreationFailedException when saving fails`() {
        val newProject =
            Project(
                id = projectId,
                name = "Project 3"
            )
        every { mockCsvWriter.writeLines(any()) } throws IOException()

        assertThrows<ProjectCreationFailedException> {
            dataSource.createProject(newProject)
        }
    }

    @Test
    fun `updateProject should updates existing project and saves to file`() {
        val updatedProject =
            Project(
                id = projectId,
                name = "Updated Project 1"
            )

        val result = dataSource.updateProject(updatedProject)

        assertThat(result).isEqualTo(updatedProject)
        verify { mockCsvWriter.writeLines(any()) }
        val allProjects = dataSource.getAllProjects()
        assertThat(allProjects).contains(updatedProject)
    }

    @Test
    fun `updateProject should throws ProjectNotChangedException when saving fails`() {
        val updatedProject =
            Project(
                id = projectId,
                name = "Updated Project 1"
            )
        every { mockCsvWriter.writeLines(any()) } throws IOException("Test exception")

        assertThrows<ProjectNotChangedException> {
            dataSource.updateProject(updatedProject)
        }
    }

    @Test
    fun `deleteProject should removes project and saves to file`() {
        val projectIdToDelete = projectId

        dataSource.deleteProject(projectIdToDelete)

        verify { mockCsvWriter.writeLines(any()) }
        val allProjects = dataSource.getAllProjects()
        assertThat(allProjects).hasSize(1)
        assertThat(allProjects[0].id).isEqualTo(projectId2)
    }

    @Test
    fun `getAllProjects should returns all projects`() {
        val result = dataSource.getAllProjects()

        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(projectId)
        assertThat(result[0].name).isEqualTo("Project 1")
        assertThat(result[1].id).isEqualTo(projectId2)
        assertThat(result[1].name).isEqualTo("Project 2")
    }

    @Test
    fun `getProjectById should returns correct project with the same id`() {
        val projectId = projectId2

        val result = dataSource.getProjectById(projectId)

        assertThat(result?.id).isEqualTo(projectId)
        assertThat(result?.name).isEqualTo("Project 2")
    }

    @Test
    fun `getProjectById should return null when project not found`() {
        val nonExistentId = Uuid.random()

        val result = dataSource.getProjectById(nonExistentId)

        assertThat(result).isNull()
    }

    @Test
    fun `deleteProject should handle exceptions during save operation`() {
        val projectIdToDelete = projectId
        every { mockCsvWriter.writeLines(any()) } throws IOException("Delete failed")

        assertThrows<IOException> {
            dataSource.deleteProject(projectIdToDelete)
        }
    }

    @Test
    fun `updateProject should add project when it doesn't exist`() {
        val newProjectId = Uuid.random()
        val newProject = Project(
            id = newProjectId,
            name = "New Project"
        )

        val result = dataSource.updateProject(newProject)

        assertThat(result).isEqualTo(newProject)
        verify { mockCsvWriter.writeLines(any()) }
        val allProjects = dataSource.getAllProjects()
        assertThat(allProjects).contains(newProject)
    }

    @Test
    fun `initialization should handle malformed CSV data gracefully`() {
        val malformedCsvLines = listOf("header", "invalid,data,format")
        every { mockCsvReader.readLines() } returns malformedCsvLines

        assertThrows<IllegalArgumentException> {
            CsvProjectDataSource(mockCsvReader, mockCsvWriter)
        }
    }
}
