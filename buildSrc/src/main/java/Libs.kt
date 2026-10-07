@file:Suppress("unused")

object Libs {
    // Frames
    const val frames = "dev.jahir:Frames:${Versions.frames}@aar"

    // Kustom API
    private const val kustomApi = "org.bitbucket.frankmonza:kustomapi:${Versions.kustomApi}@aar"

    val dependencies = arrayOf(kustomApi)
}
