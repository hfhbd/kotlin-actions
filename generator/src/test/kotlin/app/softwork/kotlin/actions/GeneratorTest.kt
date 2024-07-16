package app.softwork.kotlin.actions

import kotlin.test.*

class GeneratorTest {
    @Test
    fun simple() {
        val file = GeneratorTest::class.java.getResourceAsStream("/action.yml")!!.bufferedReader()

        assertEquals(
            //language=kotlin
            """
                import kotlin.Throwable
                import kotlin.js.JsModule
                
                public suspend fun main() {
                  try {
                    action()
                  } catch (e: Throwable) {
                    setFailed(e)
                  }
                }
                
                /**
                 * https://github.com/JetBrains/kotlin-wrappers/issues/2298
                 */
                @JsModule("@actions/core")
                public external fun setFailed(error: Throwable)
                
            """.trimIndent(),
            file.generateCode().toString(),
        )
    }

    @Test
    fun inputsAndOutputs() {
        val file = GeneratorTest::class.java.getResourceAsStream("/inputsOutputs.yml")!!.bufferedReader()

        assertEquals(
            //language=kotlin
            """
                import actions.core.InputOptions
                import actions.core.getInput
                import actions.core.setOutput
                import kotlin.String
                import kotlin.Throwable
                import kotlin.js.JsModule
                
                public suspend fun main() {
                  try {
                    val outputs: Outputs = action(
                      nonnullRequired = getInput("nonnull-required", InputOptions(required = true)),
                      nonnullNotRequiredButDefault = getInput("nonnull-not-required-but-default").ifEmpty { null },
                      nullable = getInput("nullable").ifEmpty { null },
                    )

                    setOutput("ti-me", outputs.tiMe)
                  } catch (e: Throwable) {
                    setFailed(e)
                  }
                }
                
                public data class Outputs(
                  /**
                   * The time we greeted you
                   */
                  public val tiMe: String,
                )
                
                /**
                 * https://github.com/JetBrains/kotlin-wrappers/issues/2298
                 */
                @JsModule("@actions/core")
                public external fun setFailed(error: Throwable)

            """.trimIndent(),
            file.generateCode().toString(),
        )
    }
}
