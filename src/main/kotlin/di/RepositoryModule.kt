package di

import org.example.logic.models.User
import org.example.logic.repositries.AuthenticationRepository
import org.koin.dsl.module


val repositoryModule = module {
    single <AuthenticationRepository>{object :AuthenticationRepository{
        override fun getCurrentUser(): User? {
            TODO("Not yet implemented")
        }

        override fun createMate(username: String, hashedPassword: String): User {
            TODO("Not yet implemented")
        }

        override fun login(username: String, hashedPassword: String): User {
            TODO("Not yet implemented")
        }

        override fun getAllUsers(): List<User> {
            TODO("Not yet implemented")
        }
    }}
}