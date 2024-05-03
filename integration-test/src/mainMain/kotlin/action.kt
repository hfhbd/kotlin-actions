import com.github.actions.github

suspend fun action(token: String) {
    val actor = github.context.actor
    println("Hello $actor")
}
