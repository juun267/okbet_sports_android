package org.cxct.sportlottery.net

import android.content.Context
import android.content.SharedPreferences
import com.hjq.gson.factory.GsonFactory
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import okhttp3.OkHttpClient
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.interceptor.*
import org.cxct.sportlottery.repository.ChatRepository
import org.cxct.sportlottery.repository.KEY_TOKEN
import org.cxct.sportlottery.repository.NAME_LOGIN
import org.cxct.sportlottery.repository.sConfigData
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

    private inline fun getContext() = MultiLanguagesApplication.appContext
    private val sharedPref: SharedPreferences? by lazy {
        getContext().getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }

    private fun getApiToken() = sharedPref?.getString(KEY_TOKEN, null)

    private val retrofit by lazy {
        val builder = getClientBulder()
        builder.addInterceptor(RequestInterceptor(getContext(), ::getApiToken)) // 给header添加token和语言
        builder.addInterceptor(HttpStatusInterceptor()) // 处理token过期
        RetrofitUrlManager.getInstance().with(builder) // 通过拦截器实现的动态切换域名

        Retrofit.Builder()
            .baseUrl(Constants.getBaseUrl())
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.getSingletonGson()))
            .build()
    }

    private val chatRrofit by lazy {
        val builder = getClientBulder()
        builder.addInterceptor(RequestInterceptor(getContext()) { ChatRepository.chatToken })
        chatUrlManager.with(builder)

        Retrofit.Builder()
            .baseUrl(sConfigData?.chatHost)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.getSingletonGson()))
            .build()
    }

    private val signRetrofit: Retrofit by lazy {
        val builder = getClientBulder()
        builder.addInterceptor(RequestInterceptor(getContext(), ::getApiToken))
        builder.addInterceptor(HttpStatusInterceptor()) // 处理token过期
        Retrofit.Builder()
            .baseUrl(Constants.getBaseUrl())
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.getSingletonGson()))
            .build()
    }

    private val chatUrlManager by lazy {
        var constructor = RetrofitUrlManager::class.java.getDeclaredConstructor()
        constructor.isAccessible = true
        constructor.newInstance()
    }

    fun <T> createApiService(service: Class<T>): T {
        return retrofit.create(service)
    }

    fun <T> createChatApiService(service: Class<T>): T {
        return chatRrofit.create(service)
    }

    fun <T> createSignApiService(service: Class<T>): T {
        return signRetrofit.create(service)
    }

    fun createNewRetrofit(baseUrl: String): Retrofit {
        val builder = getClientBulder()
        builder.addInterceptor(RequestInterceptor(getContext(), ::getApiToken))
        builder.addInterceptor(HttpStatusInterceptor()) // 处理token过期
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.getSingletonGson()))
            .build()
    }

    fun changeHost(baseUrl: String) {
        runWithCatch { RetrofitUrlManager.getInstance().setGlobalDomain(baseUrl) }
    }

    fun changeChatHost(host: String) {
        runWithCatch { chatUrlManager.setGlobalDomain(host) }
    }

    private fun getClientBulder(): OkHttpClient.Builder {
        return getUnsafeOkHttpClient().connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .addNetworkInterceptor(Http400or500Interceptor()) //处理后端的沙雕行为
            .addInterceptor(MoreBaseUrlInterceptor())
            .apply {
                //debug版本才打印api內容
                if (BuildConfig.DEBUG) {
//                    addInterceptor(HttpLoggingInterceptor())
                    addInterceptor(HttpLogInterceptor())
                }
            }
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