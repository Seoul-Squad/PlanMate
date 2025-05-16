package logic.useCase

import org.example.logic.useCase.Validation
import org.example.logic.utils.BlankInputException
import org.example.logic.utils.InvalidUsernameException
import org.example.logic.utils.ProjectCreationFailedException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ValidationTest {
    private lateinit var validation: Validation

    @BeforeEach
    fun setUp() {
        validation = Validation()
    }

    // validateProjectNameOrThrow tests
    @Test
    fun `validateProjectNameOrThrow should accept valid project name`() {
        validation.validateProjectNameOrThrow("Valid Project")
    }

    @Test
    fun `validateProjectNameOrThrow should throw BlankInputException when project name is blank`() {
        assertThrows<BlankInputException> {
            validation.validateProjectNameOrThrow("")
        }
    }

    @Test
    fun `validateProjectNameOrThrow should throw BlankInputException when project name is only whitespace`() {
        assertThrows<BlankInputException> {
            validation.validateProjectNameOrThrow("   ")
        }
    }

    @Test
    fun `validateProjectNameOrThrow should throw ProjectCreationFailedException when project name is longer than 16 characters`() {
        assertThrows<ProjectCreationFailedException> {
            validation.validateProjectNameOrThrow("This Project Name Is Way Too Long")
        }
    }

    // validateCreateMateUsernameAndPasswordOrThrow tests
    @Test
    fun `validateCreateMateUsernameAndPasswordOrThrow should accept valid username and password`() {
        validation.validateCreateMateUsernameAndPasswordOrThrow("validuser", "validpass")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\t", "\n"])
    fun `validateCreateMateUsernameAndPasswordOrThrow should throw BlankInputException when username is blank`(username: String) {
        assertThrows<BlankInputException> {
            validation.validateCreateMateUsernameAndPasswordOrThrow(username, "validpass")
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\t", "\n"])
    fun `validateCreateMateUsernameAndPasswordOrThrow should throw BlankInputException when password is blank`(password: String) {
        assertThrows<BlankInputException> {
            validation.validateCreateMateUsernameAndPasswordOrThrow("validuser", password)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["user name", "user\tname", "user\nname", "user space"])
    fun `validateCreateMateUsernameAndPasswordOrThrow should throw InvalidUsernameException when username contains whitespace`(
        username: String,
    ) {
        assertThrows<InvalidUsernameException> {
            validation.validateCreateMateUsernameAndPasswordOrThrow(username, "validpass")
        }
    }

    // validateLoginUsernameAndPasswordOrThrow tests
    @Test
    fun `validateLoginUsernameAndPasswordOrThrow should accept valid username and password`() {
        validation.validateLoginUsernameAndPasswordOrThrow("validuser", "validpass")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\t", "\n"])
    fun `validateLoginUsernameAndPasswordOrThrow should throw BlankInputException when username is blank`(username: String) {
        assertThrows<BlankInputException> {
            validation.validateLoginUsernameAndPasswordOrThrow(username, "validpass")
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\t", "\n"])
    fun `validateLoginUsernameAndPasswordOrThrow should throw BlankInputException when password is blank`(password: String) {
        assertThrows<BlankInputException> {
            validation.validateLoginUsernameAndPasswordOrThrow("validuser", password)
        }
    }

    // validateInputNotBlankOrThrow tests
    @Test
    fun `validateInputNotBlankOrThrow should accept non-blank input`() {
        validation.validateInputNotBlankOrThrow("valid input")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "\t", "\n"])
    fun `validateInputNotBlankOrThrow should throw BlankInputException when input is blank`(input: String) {
        assertThrows<BlankInputException> {
            validation.validateInputNotBlankOrThrow(input)
        }
    }
}
