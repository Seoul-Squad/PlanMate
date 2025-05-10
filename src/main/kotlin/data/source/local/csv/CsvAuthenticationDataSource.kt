package org.example.data.source.local.csv

import org.example.data.repository.sources.local.LocalAuthenticationDataSource
import org.example.data.source.local.csv.utils.CSVReader
import org.example.data.source.local.csv.utils.CSVWriter
import org.example.data.source.local.csv.utils.mapper.toCsvRow
import org.example.data.source.local.csv.utils.mapper.toUsers
import org.example.logic.models.User
import org.example.logic.utils.UserAlreadyExistsException
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class CsvAuthenticationDataSource(
    private val csvWriter: CSVWriter,
    private val csvReader: CSVReader,
) : LocalAuthenticationDataSource {
    private var currentUser: User? = null

    override fun saveUser(user: User) {
        if (isUserExists(user.username)) {
            throw UserAlreadyExistsException()
        }
        val allUsersRows = csvReader.readLines() + user.toCsvRow()
        csvWriter.writeLines(allUsersRows)
    }

    override fun getAllUsers(): List<User> = csvReader.readLines().toUsers()

    override fun login(
        username: String,
        hashedPassword: String,
    ): User {
        try {
            return getAllUsers()
                .first { it.username == username && it.password == hashedPassword }
                .also { currentUser = it }
                .let { User(it.id, it.username, it.password, it.role) }
        } catch (e: NoSuchElementException) {
            throw NoSuchElementException()
        }
    }

    override fun logout() {
        currentUser = null
    }

    override fun getCurrentUser(): User? = currentUser

    private fun isUserExists(username: String) = getAllUsers().any { it.username == username }
}
