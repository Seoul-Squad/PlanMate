package data.source.remote.mongo

import com.google.common.truth.Truth.assertThat
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.data.mapper.toProjectDTO
import org.example.data.models.ProjectDTO
import org.example.data.source.remote.contract.RemoteProjectDataSource
import org.example.data.source.remote.mongo.MongoProjectDataSource
import org.example.data.utils.Constants.ID
import org.example.logic.models.Project
import org.example.logic.models.State
import org.example.logic.utils.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MongoProjectDataSourceTest {
    private lateinit var mongoClientCollection: MongoCollection<ProjectDTO>
    private lateinit var remoteProjectDataSource: RemoteProjectDataSource
    private lateinit var testProjects: List<Project>
    private lateinit var testProjectDTOs: List<ProjectDTO>


    @BeforeEach
    fun setUp() {
        mongoClientCollection = mockk(relaxed = true)
        testProjects = listOf(
            Project(
                id = "1",
                name = "Project 1",
                states = listOf(State(id = "1", title = "To Do")),
                auditLogsIds = listOf("100"),
            ),
            Project(
                id = "2",
                name = "Project 2",
                states = listOf(State(id = "2", title = "In Progress")),
                auditLogsIds = listOf("200"),
            ),
        )
        testProjectDTOs = testProjects.map { it.toProjectDTO() }
        remoteProjectDataSource = MongoProjectDataSource(mongoClientCollection)
    }


    @Test
    fun `should return list of project  when  try to get projects from MongoDB`() = runTest {

        remoteProjectDataSource.getAllProjects()

        coVerify(exactly = 1) { mongoClientCollection.find(filter = any()) }
    }
    @Test
    fun `should throw GetItemsFailedException exception when try to get projects fails in MongoDB`() = runTest{

        coEvery {  mongoClientCollection.find(filter = any())} throws GetItemsFailedException("")

        assertThrows<GetItemsFailedException> {  remoteProjectDataSource.getAllProjects() }
    }

    @Test
    fun `should return project when create project at MongoDB`() = runTest {
        val newProject = Project(
            id = "3",
            name = "Project 3",
            states = listOf(State(id = "3", title = "Done")),
            auditLogsIds = listOf("300")
        )
        val projectDTO = ProjectDTO(
            id = "3",
            name = "Project 3",
            states = listOf(State(id = "3", title = "Done")),
            auditLogsIds = listOf("300")
        )

        val createProject = remoteProjectDataSource.createProject(newProject)

        coVerify(exactly = 1) { mongoClientCollection.insertOne(projectDTO, any()) }

        assertThat(createProject).isEqualTo(newProject)
    }

    @Test
    fun `should throw CreationItemFailedException exception when create project fails in MongoDB`() = runTest {
        val newProject = Project(
            id = "3",
            name = "Project 3",
            states = listOf(State(id = "3", title = "Done")),
            auditLogsIds = listOf("300")
        )
        val projectDTO = ProjectDTO(
            id = "3",
            name = "Project 3",
            states = listOf(State(id = "3", title = "Done")),
            auditLogsIds = listOf("300")
        )

        coEvery {  mongoClientCollection.insertOne(projectDTO, any())  } throws CreationItemFailedException("")

        assertThrows<CreationItemFailedException> { remoteProjectDataSource.createProject(newProject)   }

    }


    @Test
    fun `should return project when update project at MongoDB`() = runTest {
        val newProject = Project(
            id = "3",
            name = "Project 3",
            states = listOf(State(id = "3", title = "Done")),
            auditLogsIds = listOf("300")
        )
        val projectDTO = ProjectDTO(
            id = "3",
            name = "Project 3",
            states = listOf(State(id = "3", title = "Done")),
            auditLogsIds = listOf("300")
        )

        val createProject = remoteProjectDataSource.updateProject(newProject)

        coVerify(exactly = 1) { mongoClientCollection.replaceOne(Filters.eq(ID, newProject.id), projectDTO, any()) }


        assertThat(createProject).isEqualTo(newProject)
    }

    @Test
    fun `should throw UpdateCreationFailedException exception when update project fails in MongoDB`() = runTest {
        val newProject = Project(
            id = "3",
            name = "Project 3",
            states = listOf(State(id = "3", title = "Done")),
            auditLogsIds = listOf("300")
        )

        val projectDTO = ProjectDTO(
            id = "3",
            name = "Project 3",
            states = listOf(State(id = "3", title = "Done")),
            auditLogsIds = listOf("300")
        )

        coEvery { mongoClientCollection.replaceOne(Filters.eq(ID, newProject.id), projectDTO, any())} throws UpdateItemFailedException("")

        assertThrows<UpdateItemFailedException> {  remoteProjectDataSource.updateProject(newProject) }
    }


    @Test
    fun `should return project when get project by Id project at MongoDB`() = runTest {

        remoteProjectDataSource.getProjectById("1")

        coVerify(exactly = 1) { mongoClientCollection.find(filter = any()) }
    }


    @Test
    fun `should throw GetProjectByIdFailedException exception when get project by by ID fails in MongoDB`() = runTest {

        coEvery{ mongoClientCollection.find(filter = any()) }throws GetItemByIdFailedException("")

        assertThrows<GetItemByIdFailedException> { remoteProjectDataSource.getProjectById("1")  }
    }


    @Test
    fun `should delete project when delete project by Id at MongoDB`() = runTest {

        remoteProjectDataSource.deleteProject("1")

        coVerify(exactly = 1) { mongoClientCollection.deleteOne(filter = any(), options = any()) }
    }


    @Test
    fun `should throw DeleteProjectFailedException exception when deleting project by ID fails in MongoDB`() = runTest {

        coEvery {  mongoClientCollection.deleteOne(filter = any(), options = any()) } throws DeleteItemFailedException("")

        assertThrows<DeleteItemFailedException> { remoteProjectDataSource.deleteProject("1") }
    }


}


