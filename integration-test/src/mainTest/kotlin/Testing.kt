import app.softwork.kotlin.actions.GITHUB_TOKEN
import kotlinx.coroutines.test.*
import kotlin.test.*

class Testing {
    @Test
    fun a() = runTest {
        val s = getUser(GITHUB_TOKEN)
        assertEquals("hfhbd", s.login)
    }
}
