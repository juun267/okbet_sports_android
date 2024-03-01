package org.cxct.sportlottery.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.JsonUtil
import java.nio.charset.StandardCharsets

// 处理token过期问题
class HttpStatusInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.code == 401) {
            postErrorToken(response.body)
            return response
        }
        return response
    }

    private fun postErrorToken(responseBody: ResponseBody?) {
        if (responseBody == null) {
            return
        }

        val source = responseBody.source()
        source.request(Long.MAX_VALUE) // Buffer the entire body.
        val buffer = source.buffer
        var charset = StandardCharsets.UTF_8
        val contentType = responseBody.contentType()
        if (contentType != null) {
            charset = contentType.charset(StandardCharsets.UTF_8)
        }

        val jString = buffer.clone().readString(charset!!)
        buffer.close()
        if (!jString.isEmptyStr()) {
            JsonUtil.fromJson(jString, NetResult::class.java)?.let { BaseViewModel.postErrorResult(it) }
        }
    }
}