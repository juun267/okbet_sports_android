package org.cxct.sportlottery.network.interceptor

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import org.cxct.sportlottery.repository.sConfigData
import java.io.IOException
import kotlin.jvm.Throws


/**
 * Retrofit 動態切換 baseUrl
 * https://juejin.cn/post/6844903807944491015
 */

const val KEY_BASE_URL = "baseUrl"
const val HEADER_UPLOAD_IMG = "uploadImg"
class MoreBaseUrlInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        val oldBaseUrl = request.url

        //拦截retrofit注解的header配置信息
        val list = request.headers(KEY_BASE_URL)

        if (list.isNotEmpty()) {
            //移除掉header 因为服务器不需要这个header,这个header只是在拦截器里用到
            builder.removeHeader(KEY_BASE_URL)

            //如果配置的header信息在HashMap里有声明
            if (list.find { it == HEADER_UPLOAD_IMG } != null) {
                val newBaseUrl = (sConfigData?.resServerHost?: "").toHttpUrl()
                //将旧的请求地址里的协议、域名、端口号替换成配置的请求地址
                val newFullUrl = oldBaseUrl.newBuilder().scheme(newBaseUrl.scheme).host(newBaseUrl.host).port(newBaseUrl.port).build()

                //创建带有新地址的新请求
                val newRequest = builder.url(newFullUrl).build()
                return chain.proceed(newRequest)
            }
        }

        return  chain.proceed(request)
    }

}
