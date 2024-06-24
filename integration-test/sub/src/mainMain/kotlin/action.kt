import com.github.actions.github

suspend fun action(token: String?, foo: String): Outputs {
    val contextToken = github.token
    println("Got Token: $token and contextToken $contextToken!")
    return Outputs(token ?: contextToken)
}
