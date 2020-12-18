package org.cxct.sportlottery.network.interceptor

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import liveData
import okhttp3.Interceptor
import okhttp3.Response
import org.cxct.sportlottery.repository.KEY_TOKEN
import org.cxct.sportlottery.repository.NAME_LOGIN
import org.cxct.sportlottery.util.LanguageManager

import java.io.IOException
import kotlin.jvm.Throws

class RequestInterceptor(private val context: Context?) : Interceptor {

    private val sharedPref: SharedPreferences by lazy {
        context!!.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    val token = sharedPref.liveData(KEY_TOKEN, "")

    companion object {
        val TAG = RequestInterceptor::class.java.simpleName
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


        val httpUrl = urlBuilder.build()
        val newRequest = builder.url(httpUrl).build()

        return try {
            chain.proceed(newRequest)
        } catch (e: Exception) {
            Log.e(TAG, "intercept Exception:$e")
            chain.proceed(request)
        }

    }

}
