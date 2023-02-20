package org.cxct.sportlottery.net

import com.hjq.gson.factory.GsonFactory
import okhttp3.OkHttpClient
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.interceptor.Http400or500Interceptor
import org.cxct.sportlottery.network.interceptor.HttpLogInterceptor
import org.cxct.sportlottery.network.interceptor.MoreBaseUrlInterceptor
import org.cxct.sportlottery.network.interceptor.RequestInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitHolder {

    fun <T> createApiService(service: Class<T>): T {
        return retrofit.create(service)
    }

    private val retrofit by lazy { createRetrofit(Constants.getBaseUrl()) }

    private val okHttpClient: OkHttpClient by lazy {
        getUnsafeOkHttpClient().connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .addNetworkInterceptor(Http400or500Interceptor()) //处理后端的沙雕行为
            .addInterceptor(MoreBaseUrlInterceptor())
            .addInterceptor(RequestInterceptor(MultiLanguagesApplication.appContext))
            .apply {
                //debug版本才打印api內容
                if (BuildConfig.DEBUG) {
//                    addInterceptor(HttpLoggingInterceptor())
                    addInterceptor(HttpLogInterceptor())
                }
            }.build()
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
//            .baseUrl("http://192.168.2.131:8900/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.getSingletonGson()))
            .build()
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {

                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
                    checkServerTrusted(chain)
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {
                    checkServerTrusted(chain)
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            })

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
    private fun checkServerTrusted(chain: Array<X509Certificate?>?) = chain?.let {
        try {
            chain[0]?.checkValidity()
        } catch (e: java.lang.Exception) {
            throw CertificateException("Certificate not valid or trusted.")
        }
    }

}