import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

suspend fun action(token: String) {
    val client = HttpClient(Js) {
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
    println("Hello ${user.login}")
}

@Serializable
data class PublicUser(
    val login: String,
)
