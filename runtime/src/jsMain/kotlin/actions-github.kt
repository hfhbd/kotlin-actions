package com.github.actions

@JsModule("@actions/github")
@JsNonModule
external val github: GitHub

external interface GitHub {
    fun getOctokit(token: String): Octokit
    val context: Context
}

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
