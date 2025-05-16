package di

import logic.useCase.CreateTaskUseCase
import org.example.logic.useCase.*
import org.example.logic.useCase.DeleteProjectUseCase
import org.example.logic.useCase.DeleteTaskUseCase
import org.example.logic.useCase.UpdateTaskUseCase
import org.example.logic.useCase.updateProject.UpdateProjectUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val useCaseModule =
    module {
        singleOf(::DeleteProjectUseCase)
        singleOf(::DeleteTaskUseCase)
        singleOf(::CreateUserUseCase)
        singleOf(::CreateProjectUseCase)
        singleOf(::CreateProjectStateUseCase)
        singleOf(::CreateTaskUseCase)
        singleOf(::DeleteProjectStateUseCase)
        singleOf(::GetAllProjectsUseCase)
        singleOf(::GetCurrentUserUseCase)
        singleOf(::GetEntityAuditLogsUseCase)
        singleOf(::GetProjectByIdUseCase)
        singleOf(::GetProjectTasksUseCase)
        singleOf(::GetTaskByIdUseCase)
        singleOf(::LoginUserUseCase)
        singleOf(::LogoutUseCase)
        singleOf(::UpdateProjectUseCase)
        singleOf(::UpdateProjectStateUseCase)
        singleOf(::UpdateTaskUseCase)
        singleOf(::GetProjectStatesUseCase)
        singleOf(::CreateAuditLogUseCase)
        singleOf(::GetTasksByProjectStateUseCase)
        singleOf(::Validation)
    }
