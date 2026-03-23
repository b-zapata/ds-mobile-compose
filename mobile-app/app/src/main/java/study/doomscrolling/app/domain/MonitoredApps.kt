package study.doomscrolling.app.domain

/**
 * Package names of social media apps monitored for usage sessions.
 * Reconstruct foreground via UsageEvents (ACTIVITY_RESUMED / ACTIVITY_PAUSED).
 */
object MonitoredApps {
    val packageNames: Set<String> = setOf(
        "com.instagram.android",
        "com.instagram.barcelona",
        "com.zhiliaoapp.musically",
        "com.google.android.youtube",
        "com.spotify.music",
        "com.reddit.frontpage",
        "com.facebook.katana",
        "com.twitter.android"
    )

    fun isMonitored(packageName: String?): Boolean =
        packageName != null && packageNames.contains(packageName)

    /** Display names for Logcat. */
    fun displayName(packageName: String): String = when (packageName) {
        "com.instagram.android" -> "Instagram"
        "com.instagram.barcelona" -> "Threads"
        "com.zhiliaoapp.musically" -> "TikTok"
        "com.google.android.youtube" -> "YouTube"
        "com.spotify.music" -> "Spotify"
        "com.reddit.frontpage" -> "Reddit"
        "com.facebook.katana" -> "Facebook"
        "com.twitter.android" -> "Twitter"
        else -> packageName
    }
}
