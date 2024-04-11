import com.github.actions.github
import com.github.actions.token

suspend fun action() {
    val user = github.getOctokit(github.context.token).rest.users.getAuthenticated()
    println("Hello ${user.login}")
}
