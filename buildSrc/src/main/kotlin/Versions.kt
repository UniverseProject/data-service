private const val undefined = "undefined"

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
    const val description = "Allows data management through a cache and a database"
    const val url = "${Organization.url}/DataService"
}