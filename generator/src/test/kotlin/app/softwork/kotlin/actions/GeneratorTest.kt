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
                import com.github.actions.InputOptions
                import com.github.actions.core
                import kotlin.String
                
                public suspend fun main() {
                  val outputs: Outputs = action(
                    whoToGreet = core.getInput("who-to-greet", InputOptions(required = true)),
                  )

                  core.setOutput("ti-me", outputs.tiMe)
                }
                
                public data class Outputs(
                  /**
                   * The time we greeted you
                   */
                  public val tiMe: String,
                )

            """.trimIndent(),
            file.generateCode().toString(),
        )
    }
}
