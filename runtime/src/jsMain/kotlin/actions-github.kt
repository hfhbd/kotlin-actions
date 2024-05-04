package com.github.actions

import node.process.Process

@JsModule("@actions/github")
external val github: GitHub

external interface GitHub {
    val context: Context
}

// https://github.com/actions/toolkit/blob/main/packages/github/src/context.ts
external interface Context {
    val eventName: String
    val sha: String
    val ref: String
    val workflow: String
    val action: String
    val actor: String
    val job: String
    val runNumber: Long
    val runId: Long
    val apiUrl: String
    val serverUrl: String
    val graphqlUrl: String
    val repo: Repo
}

external interface Repo {
    val owner: String
    val repo: String
}

val Context.token: String get() = process.env["GITHUB_TOKEN"]!!

@JsModule("process")
external val process: Process
