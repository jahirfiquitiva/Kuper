@file:Suppress("unused")

object Libs {
    // Frames
    private const val frames = "dev.jahir:Frames:${Versions.frames}@aar"

    // Kustom API
    private const val kustomApi = "dev.jahir.KustomAPI:api:${Versions.kustomApi}@aar"

    val dependencies = arrayOf(frames, kustomApi)
}