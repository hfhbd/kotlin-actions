package com.github.actions

import kotlinx.js.JsPlainObject

@JsModule("@actions/core")
external val core: Core

external interface Core {
    fun getInput(name: String, options: InputOptions = definedExternally): String
    fun getBooleanInput(name: String, options: InputOptions = definedExternally): Boolean
    fun setOutput(name: String, value: Any)
    fun exportVariable(name: String, value: Any)
    fun isDebug(): Boolean
}

@JsPlainObject
external interface InputOptions {
    var required: Boolean?
    var trimWhitespace: Boolean?
}
