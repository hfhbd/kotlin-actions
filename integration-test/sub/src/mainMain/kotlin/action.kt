import com.github.actions.github
import com.github.actions.token

suspend fun action(token: String?, foo: String): Outputs {
    return Outputs(token ?: github.token)
}
