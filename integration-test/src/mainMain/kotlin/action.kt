import com.github.actions.github

suspend fun action(token: String) {
    val release = github.getOctokit(token).rest.repos.getLatestRelease("hfhbd", "kotlin-actions")
    println("No release yet: $release")
}
