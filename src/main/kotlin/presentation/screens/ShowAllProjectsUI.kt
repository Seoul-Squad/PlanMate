package org.example.presentation.screens

class ShowAllProjectsUI(
    private val userId: String,
    private val onBack:() -> Unit,
) {
    init {
        println(userId)
        if (readln()=="0"){
            onBack()
        }
    }
}