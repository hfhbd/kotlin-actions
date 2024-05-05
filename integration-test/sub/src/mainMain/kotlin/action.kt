import com.github.actions.github
import com.github.actions.token

suspend fun action(token: String?): Outputs {
    return Outputs(token ?: "asdf")
}
