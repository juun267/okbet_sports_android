package org.cxct.sportlottery.network.manager

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import okhttp3.OkHttpClient
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.CONNECT_TIMEOUT
import org.cxct.sportlottery.network.Constants.READ_TIMEOUT
import org.cxct.sportlottery.network.Constants.WRITE_TIMEOUT
import org.cxct.sportlottery.network.interceptor.Http400or500Interceptor
import org.cxct.sportlottery.network.interceptor.HttpLogInterceptor
import org.cxct.sportlottery.network.interceptor.HttpStatusInterceptor
import org.cxct.sportlottery.network.interceptor.MoreBaseUrlInterceptor
import org.cxct.sportlottery.network.interceptor.RequestInterceptor
import org.cxct.sportlottery.repository.KEY_TOKEN
import org.cxct.sportlottery.repository.NAME_LOGIN
import org.cxct.sportlottery.util.NullValueAdapter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class RequestManager private constructor(private val context: Context) {

    private val sharedPref: SharedPreferences? by lazy {
        context.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    private fun getApiToken() = sharedPref?.getString(KEY_TOKEN, null)

    companion object {
        private lateinit var staticContext: Application
        val instance: RequestManager by lazy {
            RequestManager(staticContext)
        }

        fun init(context: Application) {
            staticContext = context
        }
    }

    val retrofit: Retrofit

    private fun getOkHttpClientBuilder(): OkHttpClient.Builder = getUnsafeOkHttpClient()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
        .addInterceptor(HttpStatusInterceptor()) // 处理token过期
        .addInterceptor(MoreBaseUrlInterceptor())
        .addInterceptor(RequestInterceptor(context, ::getApiToken))
        .addNetworkInterceptor(Http400or500Interceptor()) //处理后端的沙雕行为
        //.addInterceptor(LogInterceptor().setLevel(LogInterceptor.Level.BODY))


        .apply {
            //debug版本才打印api內容
            if (BuildConfig.DEBUG) {
//                addInterceptor(logging)
                addInterceptor(HttpLogInterceptor())
            }
        }


    private val mMoshi: Moshi = Moshi.Builder()
        .add(BigDecimalAdapter)
        .add(KotlinJsonAdapterFactory())
        .add(NullValueAdapter())
        .build()

    init {
        retrofit = Retrofit.Builder()
            .baseUrl(Constants.getBaseUrl())
            .client(getOkHttpClientBuilder().apply { RetrofitUrlManager.getInstance().with(this) }.build())
            .addConverterFactory(MoshiConverterFactory.create(mMoshi))
            .build()
    }

    fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getOkHttpClientBuilder().build())
            .addConverterFactory(MoshiConverterFactory.create(mMoshi))
            .build()
    }

    //20190617 記錄問題: OkHttp 強制信任所有認證
    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts: Array<TrustManager> = arrayOf(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?, authType: String?,
                    ) {
                        checkServerTrusted(chain)
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?, authType: String?
                    ) {
                        checkServerTrusted(chain)
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )

            // Install the all-trusting trust manager
            val sslContext: SSLContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
            val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
            builder

        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @Throws(CertificateException::class)
    fun checkServerTrusted(chain: Array<X509Certificate?>?) {
        chain?.let {
            try {
                chain[0]?.checkValidity()
            } catch (e: java.lang.Exception) {
                throw CertificateException("Certificate not valid or trusted.")
            }
        }
    }
}
