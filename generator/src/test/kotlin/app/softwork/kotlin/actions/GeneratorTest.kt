package app.softwork.kotlin.actions

import kotlin.test.*

class GeneratorTest {
    @Test
    fun simple() {
        val file = GeneratorTest::class.java.getResourceAsStream("/action.yml")!!.bufferedReader()

        assertEquals(
            """
                import actions.core.setFailed
                import kotlin.Error

                public suspend fun main() {
                  try {
                    action()
                  } catch (e: Error) {
                    setFailed(e)
                  }
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
                import actions.core.getInput
                import actions.core.setFailed
                import actions.core.setOutput
                import js.objects.jso
                import kotlin.Error
                import kotlin.String
                
                public suspend fun main() {
                  try {
                    val outputs: Outputs = action(
                      whoToGreet = getInput("who-to-greet", jso { required = true }),
                    )

                    setOutput("ti-me", outputs.tiMe)
                  } catch (e: Error) {
                    setFailed(e)
                  }
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
