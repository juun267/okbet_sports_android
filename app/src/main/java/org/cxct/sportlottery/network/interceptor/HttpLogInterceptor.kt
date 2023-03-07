package org.cxct.sportlottery.network.interceptor

import android.util.Log
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import okio.GzipSource
import timber.log.Timber
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

/**
 * @author Martin
 * 自定義網路請求Log
 */
class HttpLogInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body
        val connection = chain.connection()
        var requestMessage = "$requestPrefixStart ${request.method} ${request.url} ${if (connection != null) connection.protocol() else ""}\n"
        requestMessage += parseHeaders(request.headers)

        if (bodyHasUnknownEncoding(request.headers)) {
            requestMessage += "\n$requestPrefixEnd END ${request.method} (encoded body omitted)"
        } else if (requestBody != null) {
            requestMessage += "\n"
            val contentType = requestBody.contentType()
            if (contentType.toString().contains("multipart")) {
                requestMessage += "\n$requestPrefixEnd END ${request.method} (multipart binary ${requestBody.contentLength()} -byte body omitted)"
            } else {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                var charset: Charset? = UTF8
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }
                if (isPlaintext(buffer)) {
                    requestMessage += buffer.readString(charset!!)
                    requestMessage += "\n$requestPrefixEnd END ${request.method}"
                } else {
                    requestMessage += "\n$requestPrefixEnd END ${request.method} (binary ${requestBody.contentLength()} -byte body omitted)"
                }
            }
        } else {
            requestMessage += "\n$requestPrefixEnd END ${request.method} (no request body)"
        }

        Timber.tag(logTag).d(requestMessage)

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Timber.tag(logTag).d("$responsePrefixStart HTTP FAILED: $e")
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        val responseBody = response.body
        val contentLength = responseBody!!.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        var responseMessage = "$responsePrefixStart ${response.code} ${request.method} ${if (response.message.isEmpty()) "" else response.message} "
        responseMessage += response.request.url
        responseMessage += " (" + tookMs + "ms" + ", $bodySize body)\n"

        val headers = response.headers
        responseMessage += parseHeaders(headers)
        if (!response.promisesBody()) {
            responseMessage += "\n$responsePrefixEnd END HTTP"
        } else if (bodyHasUnknownEncoding(response.headers)) {
            responseMessage += "\n$responsePrefixEnd END HTTP (encoded body omitted)"
        } else {
            val source = responseBody.source()
            source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
            var buffer = source.buffer
            var gzippedLength: Long? = null
            if ("gzip".equals(headers.get("Content-Encoding"), ignoreCase = true)) {
                gzippedLength = buffer.size
                GzipSource(buffer.clone()).use { gzippedResponseBody ->
                    buffer = Buffer()
                    buffer.writeAll(gzippedResponseBody)
                }
            }

            var charset: Charset? = UTF8
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }

            responseMessage += "\n"
            if (!isPlaintext(buffer)) {
                responseMessage += "\n$responsePrefixEnd END HTTP (binary " + buffer.size + "-byte body omitted)"
                return response
            }

            if (contentLength != 0L) {
                val responseData = buffer.clone().readString(charset!!)
//                responseMessage += try {
//                    JSONObject(responseData).toString(2)
//                } catch (e: JSONException) {
//                    // 不是json
//                    responseData
//                }
                responseMessage += responseData
            }

            responseMessage += if (gzippedLength != null) {
                "\n$responsePrefixEnd END HTTP (" + buffer.size + "-byte, $gzippedLength-gzipped-byte body)"
            } else {
                "\n$responsePrefixEnd END HTTP (" + buffer.size + "-byte body)"
            }
        }


        d(logTag, responseMessage)

        return response
    }


    fun d(tag: String?, msg: String?) {
        var msg = msg
        if (tag == null || tag.isEmpty() || msg == null || msg.isEmpty()) return
        val segmentSize = 3 * 1024
        val length = msg.length.toLong()
        if (length <= segmentSize) {
            Log.d(tag, msg)
        } else {
            while (msg!!.length > segmentSize) {
                val logContent = msg.substring(0, segmentSize)
                msg = msg.replace(logContent, "")
                Log.d(tag, logContent)
            }
            Log.d(tag, msg)
        }
    }

    private fun parseHeaders(headers: Headers): String {
        var headerStr = ""
        headers.names().forEach { name ->
            headerStr += " $name:${headers.get(name)}"
        }
        //[Martin]先不印Header
        return "headers = {$headerStr}"
        //return ""
    }

    companion object {

        private const val logTag = "NetLog"
        private const val requestPrefixStart = "--->"
        private const val requestPrefixEnd = "--------------------------------------->"
        private const val responsePrefixStart = "<---"
        private const val responsePrefixEnd = "<-------------------------------------"

        private val UTF8 = Charset.forName("UTF-8")

        internal fun isPlaintext(buffer: Buffer): Boolean {
            try {
                val prefix = Buffer()
                val byteCount = if (buffer.size < 64) buffer.size else 64
                buffer.copyTo(prefix, 0, byteCount)
                for (i in 0..15) {
                    if (prefix.exhausted()) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                return true
            } catch (e: EOFException) {
                return false // Truncated UTF-8 sequence.
            }
        }

        private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
            val contentEncoding = headers.get("Content-Encoding")
            return (contentEncoding != null
                    && !contentEncoding.equals("identity", ignoreCase = true)
                    && !contentEncoding.equals("gzip", ignoreCase = true))
        }
    }
}