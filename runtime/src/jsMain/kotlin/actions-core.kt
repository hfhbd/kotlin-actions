package com.github.actions

@JsModule("@actions/core")
@JsNonModule
external val core: Core

external interface Core {
    fun getInput(name: String, options: InputOptions = definedExternally): String
    fun getBooleanInput(name: String, options: InputOptions = definedExternally): Boolean
    fun setOutput(name: String, value: Any)
    fun exportVariable(name: String, value: Any)
    fun isDebug(): Boolean
}

fun Core.getInput(name: String, options: InputOptions.() -> Unit): String = getInput(name, js("{}").unsafeCast<InputOptions>().apply(options))

external interface InputOptions {
    var required: Boolean?
    var trimWhitespace: Boolean?
}
