package org.cxct.sportlottery.network.error

import okhttp3.ResponseBody
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.MATCH_BET_ADD
import org.cxct.sportlottery.network.bet.MATCH_BET_INFO
import org.cxct.sportlottery.network.bet.add.BetAddResult
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.index.INDEX_LOGIN
import org.cxct.sportlottery.network.index.LoginResult
import org.cxct.sportlottery.network.message.MESSAGE_LIST
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.SPORT_MENU
import org.cxct.sportlottery.network.sport.SportMenuResult
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
                    (url.contains(MESSAGE_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return MessageListResult(it.code, it.msg, null, it.success, null) as T
                    }
                    (url.contains(MATCH_BET_INFO)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return BetInfoResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(MATCH_BET_ADD)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return BetAddResult(it.code, it.msg, null, it.success, null) as T
                    }
                    (url.contains(SPORT_MENU)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return SportMenuResult(it.code, it.msg, it.success, null) as T
                    }
                }
            }
        }

        return null
    }
}