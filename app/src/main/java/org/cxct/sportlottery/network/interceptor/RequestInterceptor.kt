package org.cxct.sportlottery.network.interceptor

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import org.cxct.sportlottery.repository.KEY_TOKEN
import org.cxct.sportlottery.repository.NAME_LOGIN
import org.cxct.sportlottery.util.LanguageManager
import java.io.IOException

class RequestInterceptor(private val context: Context?) : Interceptor {
    private val sharedPref: SharedPreferences? by lazy {
        context?.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (context == null) {
            throw NullPointerException("Please call RequestManager.getInstance().init(context) first")
        }
        val request = chain.request()
        val builder = request.newBuilder()
        val urlBuilder = request.url.newBuilder()

        // adds the pre-encoded query parameter to this URL's query string
        // urlBuilder.addEncodedQueryParameter("encoded", "qazwsx")

        // encodes the query parameter using UTF-8 and adds it to this URL's query string
        // urlBuilder.addQueryParameter("haha", "good")

        // header
        // ex : builder.addHeader("appKey", BuildConfig.APP_KEY)

        builder.addHeader("x-lang", LanguageManager.getSelectLanguage(context).key)
//        builder.addHeader("x-session-platform-code", "spplat1")

        sharedPref?.getString(KEY_TOKEN, null)?.let {
            builder.addHeader("x-session-token", it)
        }

        val httpUrl = urlBuilder.build()
        val newRequest = builder.url(httpUrl).build()

        // 对具体的哪个接口调用没try catch的数据接口地址
        return try {
            chain.proceed(newRequest)
        } catch (e: IOException) {
            throw IOException("${e.localizedMessage} ${request.url.toString()}")
        } catch (e: RuntimeException) {
            throw RuntimeException("${e.javaClass.simpleName}: ${e.localizedMessage} ${request.url.toString()}")
        }
    }
}
