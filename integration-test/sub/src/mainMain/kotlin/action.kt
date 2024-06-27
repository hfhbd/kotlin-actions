import node.process.process

fun action(token: String, foo: String?): Outputs {
    val currentWorkspace = process.env["GITHUB_WORKSPACE"]!!
    return Outputs(foo ?: token, currentWorkspace)
}
