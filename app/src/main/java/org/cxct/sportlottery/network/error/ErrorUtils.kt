package org.cxct.sportlottery.network.error

import okhttp3.ResponseBody
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.INDEX_LOGIN
import org.cxct.sportlottery.network.index.LoginResult
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException


object ErrorUtils {

    fun <T> parseError(response: Response<T>): T? {
        val converter: Converter<ResponseBody, APIError> = OneBoSportApi.retrofit
            .responseBodyConverter(APIError::class.java, arrayOfNulls<Annotation>(0))

        var error: APIError? = null

        response.errorBody()?.let {
            try {
                error = converter.convert(it)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        error?.let {
            if (it.success != null && it.code != null && it.msg != null) {
                val url = response.raw().request.url.toString()
                when {
                    (url.contains(INDEX_LOGIN)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return LoginResult(it.code, it.msg, it.success, null) as T
                    }
                }
            }
        }

        return null
    }
}