package org.example.presentation.screens

import org.example.logic.useCase.LoginUserUseCase

class LoginUI(
    private val onNavigateToShowAllProjects: (id:String) -> Unit,
    private val loginUserUseCase: LoginUserUseCase
) {
    init {
        printWelcomeMessage()
    }

    private fun printWelcomeMessage() {
        println("Welcome to the Task Management System!")
        println("Please log in to continue.")
        val input=readln()
        onNavigateToShowAllProjects(input)

    }

}