package org.cxct.sportlottery.network.manager

import android.content.Context
import org.cxct.sportlottery.network.interceptor.RequestInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.network.interceptor.LogInterceptor
import org.cxct.sportlottery.network.interceptor.MockApiInterceptor
import org.cxct.sportlottery.util.NullValueAdapter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

const val BASE_URL = "https://sports.cxct.org"
const val CONNECT_TIMEOUT: Long = 15 * 1000
const val WRITE_TIMEOUT: Long = 15 * 1000
const val READ_TIMEOUT: Long = 15 * 1000

class RequestManager private constructor(context: Context) {

    var retrofit: Retrofit

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(NullValueAdapter())
        .build()

    companion object {
        private lateinit var staticContext: Context
        val instance: RequestManager by lazy {
            RequestManager(staticContext)
        }

        fun init(context: Context) {
            staticContext = context
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
