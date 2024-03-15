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
        val httpClient = getHttpClient { builder ->
            builder.addInterceptor(RequestInterceptor(getContext(), ::getApiToken)) // 给header添加token和语言
            builder.addInterceptor(HttpStatusInterceptor()) // 处理token过期
            RetrofitUrlManager.getInstance().with(builder) // 通过拦截器实现的动态切换域名
        }

        Retrofit.Builder()
            .baseUrl(Constants.getBaseUrl())
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.getSingletonGson()))
            .build()
    }

    private val chatRetrofit by lazy {
        val httpClient = getHttpClient { builder ->
            builder.addInterceptor(RequestInterceptor(getContext()) { ChatRepository.chatToken })
            chatUrlManager.with(builder)
        }

        Retrofit.Builder()
            .baseUrl(sConfigData?.chatHost ?: Constants.getBaseUrl())
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.getSingletonGson()))
            .build()
    }

    private val signRetrofit: Retrofit by lazy {
        val httpClient = getHttpClient { builder ->
            builder.addInterceptor(RequestInterceptor(getContext(), ::getApiToken))
            builder.addInterceptor(HttpStatusInterceptor()) // 处理token过期
        }

        Retrofit.Builder()
            .baseUrl(Constants.getBaseUrl())
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.getSingletonGson()))
            .build()
    }

    private val ocrRetrofit by lazy {
        val httpClient = getHttpClient { builder ->
            builder.addInterceptor(RequestInterceptor(getContext(), ::getApiToken)) // 给header添加token和语言
            builder.addInterceptor(HttpStatusInterceptor()) // 处理token过期
            ocrUrlManager = newUrlManager()
            ocrUrlManager!!.with(builder) // 通过拦截器实现的动态切换域名
        }

        Retrofit.Builder()
            .baseUrl(sConfigData?.idScanHost ?: Constants.getBaseUrl())
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.getSingletonGson()))
            .build()
    }

    private val chatUrlManager by lazy { newUrlManager() }

    private fun newUrlManager(): RetrofitUrlManager{
        val constructor = RetrofitUrlManager::class.java.getDeclaredConstructor()
        constructor.isAccessible = true
        return constructor.newInstance()
    }

    private var ocrUrlManager: RetrofitUrlManager? = null

    fun <T> createApiService(service: Class<T>): T {
        return retrofit.create(service)
    }

    fun <T> createChatApiService(service: Class<T>): T {
        return chatRetrofit.create(service)
    }

    fun <T> createSignApiService(service: Class<T>): T {
        return signRetrofit.create(service)
    }

    fun <T> createOCRApiService(service: Class<T>): T {
        return ocrRetrofit.create(service)
    }

    fun createNewRetrofit(baseUrl: String): Retrofit {
        val httpClient = getHttpClient { builder->
            builder.addInterceptor(RequestInterceptor(getContext(), ::getApiToken))
            builder.addInterceptor(HttpStatusInterceptor()) // 处理token过期
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.getSingletonGson()))
            .build()
    }

    fun changeHost(baseUrl: String) {
        changeUrl(RetrofitUrlManager.getInstance(), baseUrl)
    }

    fun changeChatHost(host: String) {
        changeUrl(chatUrlManager, host)
    }

    fun changeORCHost(host: String) {
        ocrUrlManager?.let { changeUrl(it, host) }
    }

    private fun changeUrl(urlManger: RetrofitUrlManager, host: String) {
        try {
            urlManger.setGlobalDomain(host)
        } catch (e: Exception) {
            if (!host.startsWith("http", true)) {
                runWithCatch { urlManger.setGlobalDomain("http://$host") }
            }
        }
    }

    private fun getHttpClient(block: (OkHttpClient.Builder) -> Unit): OkHttpClient {
        val builder = getUnsafeOkHttpClient().connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .addNetworkInterceptor(Http400or500Interceptor()) //处理后端的沙雕行为
            .addInterceptor(MoreBaseUrlInterceptor())

        block.invoke(builder)
        //debug版本才打印api內容
        if (BuildConfig.DEBUG) {
            // 放到所有拦截器添加完成后再添加日志输出拦截器，避免通过拦截器添加的请求头在日志中没有输出的问题
            builder.addInterceptor(HttpLogInterceptor())
        }
        return builder.build()
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