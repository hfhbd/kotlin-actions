package app.softwork.kotlin.actions

import kotlin.test.*

class GeneratorTest {
    @Test
    fun simple() {
        val file = GeneratorTest::class.java.getResourceAsStream("/action.yml")!!.bufferedReader()

        assertEquals(
            """
                public suspend fun main() {
                  action()
                }

            """.trimIndent(),
            file.generateCode().toString(),
        )
    }

    @Test
    fun inputsAndOutputs() {
        val file = GeneratorTest::class.java.getResourceAsStream("/inputsOutputs.yml")!!.bufferedReader()

        assertEquals(
            """
                import com.github.actions.core
                
                public suspend fun main() {
                  val outputs = action(
                    whoToGreet = core.getInput("who-to-greet") {
                      required = true
                    },
                  )
                  core.setOutput("time", outputs.time)
                
                }

            """.trimIndent(),
            file.generateCode().toString(),
        )
    }
}
