package org.cxct.sportlottery.network.interceptor

import android.util.Log
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okio.Buffer
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws

class LogInterceptor @JvmOverloads constructor(private val logger: Logger = Logger.DEFAULT) :
    Interceptor {
    enum class Level {
        NONE, HEADERS, BODY
    }

    interface Logger {
        fun log(message: String?)

        companion object {
            val DEFAULT: Logger =
                object : Logger {
                    override fun log(message: String?) {
                        if (message == null) return
                        val maxLogSize = 1000
                        for (i in 0..message.length / maxLogSize) {
                            val start = i * maxLogSize
                            var end = (i + 1) * maxLogSize
                            end =
                                if (end > message.length) message.length else end
                            if (i != 0)
                                Log.e(
                                    LogInterceptor::class.java.simpleName,
                                    "\n" + message.substring(start, end)
                                )
                            else
                                Log.e(
                                    LogInterceptor::class.java.simpleName,
                                    message.substring(start, end)
                                )
                        }
                    }
                }
        }
    }

    @Volatile
    var level =
        Level.NONE
        private set

    /**
     * Change the level at which this interceptor logs.
     */
    fun setLevel(level: Level?): LogInterceptor {
        if (level == null) throw NullPointerException("level == null. Use Level.NONE instead.")
        this.level = level
        return this
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val level = level
        val request = chain.request()
        if (level == Level.NONE) {
            return chain.proceed(request)
        }
        val logBody = level == Level.BODY
        val logHeaders =
            logBody || level == Level.HEADERS
        val requestBody = request.body
        val hasRequestBody = requestBody != null
        val connection = chain.connection()
        val protocol =
            connection?.protocol() ?: Protocol.HTTP_1_1
        var requestStartMessage =
            "--> " + request.method + ' ' + request.url + ' ' + protocol(
                protocol
            )
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody!!.contentLength() + "-byte body)"
        }
        logger.log(requestStartMessage)
        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody!!.contentType() != null) {
                    logger.log("Content-Type: " + requestBody.contentType())
                }
                if (requestBody.contentLength() != -1L) {
                    logger.log("Content-Length: " + requestBody.contentLength())
                }
            }
            val headers = request.headers
            var i = 0
            val count = headers.size
            while (i < count) {
                val name = headers.name(i)
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equals(
                        name,
                        ignoreCase = true
                    ) && !"Content-Length".equals(name, ignoreCase = true)
                ) {
                    logger.log("Header: " + name + ": " + headers.value(i))
                }
                i++
            }
            if (!logBody || !hasRequestBody) {
                logger.log("--> END " + request.method)
            } else if (bodyEncoded(request.headers)) {
                logger.log("--> END " + request.method + " (encoded body omitted)")
            } else {
                val buffer = Buffer()
                requestBody!!.writeTo(buffer)
                var charset = UTF8
                val contentType = requestBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }
                logger.log("")
                logger.log(buffer.readString(charset!!))
                logger.log(
                    "--> END " + request.method + " (" + requestBody.contentLength() + "-byte body)"
                )
            }
        }
        val startNs = System.nanoTime()
        val response = chain.proceed(request)
        val tookMs =
            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        val responseBody = response.body
        val contentLength = responseBody!!.contentLength()
        val bodySize =
            if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        logger.log(
            "<-- "
                    + response.code
                    + ' '
                    // + response.message
                    // + ' '
                    + response.request.url
                    + " ("
                    + tookMs
                    + "ms"
                    + (if (!logHeaders) ", $bodySize body" else "")
                    + ')'
        )
        if (logHeaders) {
            val headers = response.headers
            val head = StringBuilder()
            var i = 0
            val count = headers.size
            while (i < count) {
                head.append(headers.name(i)).append(": ").append(headers.value(i)).append(",")
                i++
            }
            logger.log(head.toString())
            if (!logBody /*|| !HttpEngine.hasBody(response)*/) {
                logger.log("<-- END HTTP")
            } else if (bodyEncoded(response.headers)) {
                logger.log("<-- END HTTP (encoded body omitted)")
            } else {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE) // Buffer the entire body.
                val buffer = source.buffer
                var charset = UTF8
                val contentType = responseBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }
                if (contentLength != 0L) {
                    logger.log("")
                    val string = buffer.clone().readString(charset!!)
                    try {
                        logger.log(JSONObject(string).toString(4))
                    } catch (e: Exception) {
                        logger.log(string)
                    }
                }
                logger.log("<-- END HTTP (" + buffer.size + "-byte body)")
            }
        }
        return response
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    companion object {
        private val UTF8 = StandardCharsets.UTF_8
        private fun protocol(protocol: Protocol): String {
            return if (protocol == Protocol.HTTP_1_0) "HTTP/1.0" else "HTTP/1.1"
        }
    }

}