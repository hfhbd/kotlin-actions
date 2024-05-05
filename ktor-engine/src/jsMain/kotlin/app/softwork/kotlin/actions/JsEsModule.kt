package app.softwork.kotlin.actions

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import js.buffer.BufferSource
import js.iterable.toList
import js.objects.jso
import js.promise.await
import js.typedarrays.asInt8Array
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import web.http.BodyInit
import web.http.RequestInit
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
            rawResponse.headers.keys().toList().forEach { key ->
                append(key, rawResponse.headers[key]!!)
            }
        }
        val version = HttpProtocolVersion.HTTP_1_1

        val body = readBodyNode(CoroutineScope(callContext), rawResponse)

        return HttpResponseData(
            status,
            requestTime,
            headers,
            version,
            body,
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

    val bodyBytes: BufferSource? = when (val content = body) {
        is OutgoingContent.ByteArrayContent -> content.bytes().asInt8Array()
        is OutgoingContent.ReadChannelContent -> content.readFrom().readRemaining().readBytes().asInt8Array()
        is OutgoingContent.WriteChannelContent -> {
            GlobalScope.writer(callContext) {
                content.writeTo(channel)
            }.channel.readRemaining().readBytes().asInt8Array()
        }

        else -> null
    }

    return jso {
        method = this@toRaw.method.value
        headers = jsHeaders
        redirect = if (clientConfig.followRedirects) {
            web.http.RequestRedirect.follow
        } else web.http.RequestRedirect.manual
        if (bodyBytes != null) {
            body = BodyInit(bodyBytes)
        }
    }
}

internal fun readBodyNode(scope: CoroutineScope, response: web.http.Response): ByteReadChannel {
    val data = flow {
        val body = response.body ?: error("Fail to get body")
        val reader = body.getReader()
        while (true) {
            when (val result = reader.read().await()) {
                is ReadableStreamReadDoneResult -> {
                    val lastValue = result.value
                    if (lastValue != null) {
                        emit(lastValue)
                    }
                    break
                }

                is ReadableStreamReadValueResult -> emit(
                    result.value
                )
            }
        }
    }.cancellable()
    return scope.writer {
        data.collect {
            channel.writeFully(it.toByteArray())
        }
        channel.flush()
    }.channel
}
