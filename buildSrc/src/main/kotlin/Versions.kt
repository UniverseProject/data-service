private const val undefined = "undefined"

/**
 * whether the process has been invoked by JitPack
 */
val isJitPack get() = "true" == System.getenv("JITPACK")

object Repository {
    val releasesUrl = System.getenv("REPOSITORY_RELEASE_URL") ?: undefined
    val snapshotsUrl = System.getenv("REPOSITORY_SNAPSHOT_URL") ?: undefined

    val username: String? get() = System.getenv("REPOSITORY_USER")
    val password: String? get() = System.getenv("REPOSITORY_PASSWORD")
}

object Organization {
    const val name = "UniverseProject"
    const val url = "https://github.com/UniverseProject"
}

object Developer {
    const val name = "Universe team"
}

object Issue {
    const val system = "GitHub"
    const val url = "${Library.url}/issues"
}

object SCM {
    const val connection = "scm:git:ssh://github.com/UniverseProject/DataService.git"
    const val developerConnection = "scm:git:ssh://git@github.com:UniverseProject/DataService.git"
}

object License {
    const val name = "MIT"
    const val url = "https://opensource.org/licenses/MIT"
}

object Library {
    const val name = "dataservice"
    const val group = "org.universe"
    const val description = "Allows data management through a cache and a database"
    const val url = "${Organization.url}/DataService"

    val version: String
        get() = if (isJitPack) System.getenv("RELEASE_TAG")
        else {
            val tag = System.getenv("GITHUB_TAG_NAME")
            val branch = System.getenv("GITHUB_BRANCH_NAME")
            when {
                !tag.isNullOrBlank() -> tag
                !branch.isNullOrBlank() && branch.startsWith("refs/heads/") ->
                    branch.substringAfter("refs/heads/").replace("/", "-") + "-SNAPSHOT"
                else -> "undefined"
            }

        }

    val isSnapshot: Boolean get() = version.endsWith("-SNAPSHOT")

    val isRelease: Boolean get() = !isSnapshot && !isUndefined

    val isUndefined get() = version == undefined
}