import app.softwork.kotlin.actions.GITHUB_TOKEN
import kotlinx.coroutines.test.*
import kotlin.test.*

class Testing {
    @Test
    fun a() = runTest {
        val s = getMainBranch("hfhbd", "kotlin-actions", GITHUB_TOKEN)
        assertEquals("main", s.name)
    }
}
