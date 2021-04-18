@file:Suppress("unused")

object Libs {
    // Frames
    const val frames = "dev.jahir:Frames:${Versions.frames}@aar"

    // Kustom API
    private const val kustomApi = "dev.jahir.KustomAPI:api:${Versions.kustomApi}@aar"

    // Lifecycle Scope
    private const val lifecycleScope =
        "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycleRuntimeKtx}"

    val dependencies = arrayOf(kustomApi, lifecycleScope)
}
