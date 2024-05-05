suspend fun action(token: String?): Outputs {
    return Outputs(token ?: "asdf")
}
