package dev.jahir.kuper.data.models

import androidx.annotation.DrawableRes

data class RequiredApp(
    val name: String,
    val description: String,
    @DrawableRes val icon: Int,
    val packageName: String = ""
)