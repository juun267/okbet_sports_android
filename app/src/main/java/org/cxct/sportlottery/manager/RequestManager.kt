package org.cxct.sportlottery.manager

import android.content.Context
import org.cxct.sportlottery.interceptor.RequestInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.interceptor.LogInterceptor
import org.cxct.sportlottery.interceptor.MockApiInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class RequestManager private constructor(context: Context) {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val BASE_URL = "https://sports.cxct.org"

    var retrofit: Retrofit

    companion object {
        const val CONNECT_TIMEOUT: Long = 15 * 1000
        const val WRITE_TIMEOUT: Long = 15 * 1000
        const val READ_TIMEOUT: Long = 15 * 1000

        private var instance: RequestManager? = null
        private var staticContext: Context? = null

        fun init(c: Context) {
            staticContext = c
        }

        fun getInstance(): RequestManager {
            if (staticContext == null) {
                throw RuntimeException("You must initialize this manager before getting instance")
            }
            if (instance == null) {
                instance = RequestManager(staticContext!!)
            }
            return instance!!
        }

    }

    init {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .addInterceptor(RequestInterceptor(context))


        okHttpClientBuilder.addInterceptor(LogInterceptor().setLevel(LogInterceptor.Level.BODY))

        // mock data, 必須擺在最後
        if (BuildConfig.MOCK) {
            okHttpClientBuilder.addInterceptor(MockApiInterceptor(context))
        }

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClientBuilder.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

}
