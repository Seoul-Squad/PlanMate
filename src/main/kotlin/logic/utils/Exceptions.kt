package org.example.logic.utils

open class GeneralException(
    message: String,
) : Exception(message)

class BlankInputException : GeneralException("Input cannot be blank")

class InvalidInputException : GeneralException("Invalid input provided")

class DataBaseException : GeneralException("Database URI not found")

open class AuthenticationException(
    message: String,
) : Exception(message)

class InvalidUsernameException : AuthenticationException("Invalid username")

class UserAlreadyExistsException : AuthenticationException("User already exists")

class UserNotFoundException : AuthenticationException("User not found")

class NoLoggedInUserException : AuthenticationException("No user is logged in")

class UnauthorizedAccessException : AuthenticationException("Unauthorized access")

class UserCreationFailedException : AuthenticationException("Failed to create user")

open class ProjectException(
    message: String,
) : Exception(message)

class ProjectNotChangedException : ProjectException("Project was not modified")

class ProjectNotFoundException : ProjectException("Project not found")

class ProjectCreationFailedException : ProjectException("Failed to create project")

class NoProjectsFoundException : ProjectException("No projects found")

class ProjectDeletionFailedException : ProjectException("Failed to delete project")

open class TaskException(
    message: String,
) : Exception(message)

class TaskNotChangedException : TaskException("Task was not modified")

class TaskNotFoundException : TaskException("Task not found")

class TaskCreationFailedException : TaskException("Failed to create task")

class TaskDeletionFailedException : TaskException("Unable to delete task")

class NoTaskFoundException : TaskException("No task found")

class NoTasksFoundException : TaskException("No tasks found")

class ProjectStateNotFoundException : TaskException("Task state not found")

open class AuditLogException(
    message: String,
) : Exception(message)

class InvalidAuditInputException : AuditLogException("Invalid audit input")

class AuditLogNotFoundException : AuditLogException("Audit log not found")

class AuditLogCreationFailedException : AuditLogException("Failed to create audit log")

class AuditLogDeletionFailedException : AuditLogException("Failed to delete audit log")
