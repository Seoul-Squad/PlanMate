package logic.useCase

import io.reactivex.rxjava3.core.Completable
import org.example.logic.useCase.Validation
import org.example.logic.utils.BlankInputException
import org.example.logic.utils.InvalidUsernameException
import org.example.logic.utils.ProjectCreationFailedException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ValidationTest {
    private lateinit var validation: Validation

    @BeforeEach
    fun setUp() {
        validation = Validation()
    }

    @Test
    fun `validateProjectNameOrThrow should accept valid project name`() {
        Completable
            .fromAction {
                validation.validateProjectNameOrThrow("Valid Project")
            }.test()
            .assertComplete()
    }

    @Test
    fun `validateProjectNameOrThrow should throw BlankInputException when project name is blank`() {
        Completable
            .fromAction {
                validation.validateProjectNameOrThrow("")
            }.test()
            .assertError(BlankInputException::class.java)
    }

    @Test
    fun `validateProjectNameOrThrow should throw BlankInputException when project name is only whitespace`() {
        Completable
            .fromAction {
                validation.validateProjectNameOrThrow("   ")
            }.test()
            .assertError(BlankInputException::class.java)
    }

    @Test
    fun `validateProjectNameOrThrow should throw ProjectCreationFailedException when project name is longer than 16 characters`() {
        Completable
            .fromAction {
                validation.validateProjectNameOrThrow("This Project Name Is Way Too Long")
            }.test()
            .assertError(ProjectCreationFailedException::class.java)
    }

    // validateCreateMateUsernameAndPasswordOrThrow tests
    @Test
    fun `validateCreateMateUsernameAndPasswordOrThrow should accept valid username and password`() {
        Completable
            .fromAction {
                validation.validateCreateMateUsernameAndPasswordOrThrow("validuser", "validpass")
            }.test()
            .assertComplete()
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\t", "\n"])
    fun `validateCreateMateUsernameAndPasswordOrThrow should throw BlankInputException when username is blank`(username: String) {
        Completable
            .fromAction {
                validation.validateCreateMateUsernameAndPasswordOrThrow(username, "validpass")
            }.test()
            .assertError(BlankInputException::class.java)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\t", "\n"])
    fun `validateCreateMateUsernameAndPasswordOrThrow should throw BlankInputException when password is blank`(password: String) {
        Completable
            .fromAction {
                validation.validateCreateMateUsernameAndPasswordOrThrow("validuser", password)
            }.test()
            .assertError(BlankInputException::class.java)
    }

    @ParameterizedTest
    @ValueSource(strings = ["user name", "user\tname", "user\nname", "user space"])
    fun `validateCreateMateUsernameAndPasswordOrThrow should throw InvalidUsernameException when username contains whitespace`(
        username: String,
    ) {
        Completable
            .fromAction {
                validation.validateCreateMateUsernameAndPasswordOrThrow(username, "validpass")
            }.test()
            .assertError(InvalidUsernameException::class.java)
    }

    // validateLoginUsernameAndPasswordOrThrow tests
    @Test
    fun `validateLoginUsernameAndPasswordOrThrow should accept valid username and password`() {
        Completable
            .fromAction {
                validation.validateLoginUsernameAndPasswordOrThrow("validuser", "validpass")
            }.test()
            .assertComplete()
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\t", "\n"])
    fun `validateLoginUsernameAndPasswordOrThrow should throw BlankInputException when username is blank`(username: String) {
        Completable
            .fromAction {
                validation.validateLoginUsernameAndPasswordOrThrow(username, "validpass")
            }.test()
            .assertError(BlankInputException::class.java)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\t", "\n"])
    fun `validateLoginUsernameAndPasswordOrThrow should throw BlankInputException when password is blank`(password: String) {
        Completable
            .fromAction {
                validation.validateLoginUsernameAndPasswordOrThrow("validuser", password)
            }.test()
            .assertError(BlankInputException::class.java)
    }

    // validateInputNotBlankOrThrow tests
    @Test
    fun `validateInputNotBlankOrThrow should accept non-blank input`() {
        Completable
            .fromAction {
                validation.validateInputNotBlankOrThrow("valid input")
            }.test()
            .assertComplete()
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\t", "\n"])
    fun `validateInputNotBlankOrThrow should throw BlankInputException when input is blank`(input: String) {
        Completable
            .fromAction {
                validation.validateInputNotBlankOrThrow(input)
            }.test()
            .assertError(BlankInputException::class.java)
    }
}
