package org.cxct.sportlottery.network.error

import okhttp3.ResponseBody
import org.cxct.sportlottery.network.OneBoSportApi
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException


object ErrorUtils {

    fun <T> parseError(response: Response<T>): APIError? {
        val converter: Converter<ResponseBody, APIError> = OneBoSportApi.retrofit
            .responseBodyConverter(APIError::class.java, arrayOfNulls<Annotation>(0))

        var error: APIError? = null

        response.errorBody()?.let {
            try {
                error = converter.convert(it)
            } catch (e: IOException) {
                return APIError()
            }
        }
        return error
    }
}