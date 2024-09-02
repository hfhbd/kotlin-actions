import actions.github.context
import app.softwork.kotlin.actions.JsEsModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

suspend fun action(token: String) {
    println("Hello ${context.actor}")
    val user = getMainBranch(context.repo.owner, context.repo.repo, token)
    println("Branch ${user.name}")
}

suspend fun getMainBranch(
    owner: String,
    repo: String,
    token: String
): Branch {
    val client = HttpClient(JsEsModule) {
        install(ContentNegotiation) {
            json(
                json = Json {
                    ignoreUnknownKeys = true
                    useAlternativeNames = false
                }
            )
        }
        expectSuccess = true
    }
    val branch = client.get("https://api.github.com/repos/$owner/$repo/branches/main") {
        bearerAuth(token)
    }.body<Branch>()

    return branch
}

@Serializable
data class Branch(
    val name: String,
)
