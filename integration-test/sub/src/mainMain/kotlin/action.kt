import com.github.actions.github

suspend fun action(token: String, foo: String?): Outputs {
    return Outputs(foo ?: token)
}
