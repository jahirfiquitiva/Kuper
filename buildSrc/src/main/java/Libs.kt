@file:Suppress("unused")

object Libs {
    // Frames
    const val frames = "dev.jahir:Frames:${Versions.frames}@aar"

    // Kustom API
    private const val kustomApi = "org.bitbucket.frankmonza:kustomapi:${Versions.kustomApi}@aar"

    // OneSignal
    const val oneSignal = "com.onesignal:OneSignal:${Versions.oneSignal}"

    val dependencies = arrayOf(kustomApi)
}
