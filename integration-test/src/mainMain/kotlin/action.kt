import com.github.actions.github
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

suspend fun action(token: String) {
    println("Hello ${github.context.actor}")
    val user = getUser(token)
    println("API ${user.login}")
}

suspend fun getUser(token: String): PublicUser {
    val client = HttpClient(JsEsModule) {
        install(ContentNegotiation) {
            json(
                json = Json {
                    ignoreUnknownKeys = true
                    useAlternativeNames = false
                }
            )
        }
    }
    val user = client.get("https://api.github.com/user") {
        bearerAuth(token)
    }.body<PublicUser>()
    return user
}

@Serializable
data class PublicUser(
    val login: String,
)
