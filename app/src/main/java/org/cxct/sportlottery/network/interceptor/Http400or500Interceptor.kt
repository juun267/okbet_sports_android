package org.cxct.sportlottery.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

//处理接口调用时业务失败后服务端把http状态码改为400或者500的沙雕操作
class Http400or500Interceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val response = chain.proceed(chain.request())
        if (response.code == 400 || response.code == 500) {
            return Response.Builder()
                .code(200)
                .message(response.message)
                .headers(response.headers)
                .request(response.request)
                .protocol(response.protocol)
                .body(response.body)
                .build()
        }

        return response
    }
}