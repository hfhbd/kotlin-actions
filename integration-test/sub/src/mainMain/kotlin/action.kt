suspend fun action(token: String?, foo: String): Outputs {
    println("Got Token: $token and contextToken $foo!")
    return Outputs(token ?: foo)
}
