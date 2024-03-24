@file:JsModule("@octokit")

external interface Octokit {
    val rest: Rest
}

external interface Rest {
    val repos: Repos
}

external interface Repos {
    suspend fun listReleases(owner: String, repo: String): Array<Release>
    suspend fun getLatestRelease(owner: String, repo: String): Release?
}

external interface Release {
    val url: String
    val html_url: String
    val assets_url: String
    val upload_url: String
    val tarball_url: String?
    val zipball_url: String?
    val id: Int
    val node_id: String
    val tag_name: String
    val target_commitish: String
    val name: String?
    val body: String?
    val draft: Boolean
    val prerelease: Boolean
    val created_at: String
    val published_at: String
}
