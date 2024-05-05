package app.softwork.kotlin.actions

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class NodeJSEngineTest {
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

    @Test
    fun testEngine() = runTest {
        val s = getMainBranch("hfhbd", "kotlin-actions", GITHUB_TOKEN)
        assertEquals("main", s.name)
    }
}
