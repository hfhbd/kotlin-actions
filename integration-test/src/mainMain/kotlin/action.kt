import com.github.actions.github

suspend fun action(token: String) {
    val user = github.getOctokit(token).rest.users.getAuthenticated()
    println("Hello ${user.login}")
}
