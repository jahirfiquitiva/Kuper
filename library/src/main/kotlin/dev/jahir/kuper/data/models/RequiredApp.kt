package dev.jahir.kuper.data.models

data class RequiredApp(
    val name: String,
    val description: String,
    val icon: String,
    val packageName: String = ""
)