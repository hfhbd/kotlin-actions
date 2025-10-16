package app.softwork.kotlin.actions

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import js.typedarrays.asInt8Array
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.flow
import kotlinx.io.readByteArray
import web.http.BodyInit
import web.http.RequestInit
import web.http.RequestMethod
import web.http.fetch
import web.streams.ReadableStreamReadDoneResult
import web.streams.ReadableStreamReadValueResult
import kotlin.coroutines.CoroutineContext

object JsEsModule : HttpClientEngineFactory<HttpClientEngineConfig> {
    override fun create(block: HttpClientEngineConfig.() -> Unit): HttpClientEngine =
        JsEsModuleEngine(HttpClientEngineConfig().apply(block))
}

private val CLIENT_CONFIG = AttributeKey<HttpClientConfig<*>>("client-config")

private class JsEsModuleEngine(
    override val config: HttpClientEngineConfig
) : HttpClientEngineBase("ktor-js") {

    init {
        check(config.proxy == null) { "Proxy unsupported in Js engine." }
    }

    @OptIn(InternalAPI::class)
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val callContext = callContext()
        val clientConfig = data.attributes[CLIENT_CONFIG]

        if (data.isUpgradeRequest()) {
            error("Not supported")
        }

        val requestTime = GMTDate()
        val rawRequest: RequestInit = data.toRaw(clientConfig, callContext)
        val rawResponse = fetch(data.url.toString(), rawRequest)

        val status = HttpStatusCode(rawResponse.status.toInt(), rawResponse.statusText)
        val headers = Headers.build {
            for ((key, value) in rawResponse.headers) {
                append(key, value)
            }
        }
        val version = HttpProtocolVersion.HTTP_1_1

        val body = readBodyNode(CoroutineScope(callContext), rawResponse)

        return HttpResponseData(
            status,
            requestTime,
            headers,
            version,
            body ?: NullBody,
            callContext
        )
    }
}

@OptIn(InternalAPI::class, DelicateCoroutinesApi::class)
internal suspend fun HttpRequestData.toRaw(
    clientConfig: HttpClientConfig<*>,
    callContext: CoroutineContext
): RequestInit {
    val jsHeaders = web.http.Headers()
    mergeHeaders(this@toRaw.headers, this@toRaw.body) { key, value ->
        jsHeaders[key] = value
    }

    val bodyBytes = when (val content = body) {
        is OutgoingContent.ByteArrayContent -> content.bytes().asInt8Array()
        is OutgoingContent.ReadChannelContent -> content.readFrom().readRemaining().readByteArray().asInt8Array()
        is OutgoingContent.WriteChannelContent -> {
            GlobalScope.writer(callContext) {
                content.writeTo(channel)
            }.channel.readRemaining().readByteArray().asInt8Array()
        }

        else -> null
    }

    return RequestInit(
        method = when(method) {
            HttpMethod.Get -> RequestMethod.GET
            HttpMethod.Post -> RequestMethod.POST
            HttpMethod.Put -> RequestMethod.PUT
            HttpMethod.Patch -> RequestMethod.PATCH
            HttpMethod.Delete -> RequestMethod.DELETE
            HttpMethod.Head -> RequestMethod.HEAD
            HttpMethod.Options -> RequestMethod.OPTIONS
            else -> error("Unsupported HTTP method $method")
        },
        headers = jsHeaders,
        redirect = if (clientConfig.followRedirects) {
            web.http.RequestRedirect.follow
        } else web.http.RequestRedirect.manual,
        body = if (bodyBytes != null) {
            BodyInit(bodyBytes)
        } else null,
    )
}

internal fun readBodyNode(scope: CoroutineScope, response: web.http.Response): ByteReadChannel? {
    val body = response.body ?: return null
    val data = flow {
        val reader = body.getReader()
        try {
            while (true) {
                when (val result = reader.read()) {
                    is ReadableStreamReadDoneResult -> {
                        val lastValue = result.value
                        if (lastValue != null) {
                            emit(lastValue)
                        }
                        break
                    }

                    is ReadableStreamReadValueResult -> emit(result.value)
                }
            }
        } finally {
            reader.cancel()
        }
    }
    return scope.writer {
        data.collect {
            channel.writeFully(it.toByteArray())
        }
        channel.flush()
    }.channel
}
