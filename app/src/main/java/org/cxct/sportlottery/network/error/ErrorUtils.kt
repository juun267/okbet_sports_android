package org.cxct.sportlottery.network.error

import androidx.annotation.Nullable
import okhttp3.ResponseBody
import org.cxct.sportlottery.network.Constants.INDEX_LOGIN
import org.cxct.sportlottery.network.Constants.INDEX_LOGOUT
import org.cxct.sportlottery.network.Constants.LEAGUE_LIST
import org.cxct.sportlottery.network.Constants.MATCH_BET_ADD
import org.cxct.sportlottery.network.Constants.MATCH_BET_INFO
import org.cxct.sportlottery.network.Constants.MATCH_BET_LIST
import org.cxct.sportlottery.network.Constants.MATCH_ODDS_LIST
import org.cxct.sportlottery.network.Constants.MATCH_PRELOAD
import org.cxct.sportlottery.network.Constants.MESSAGE_LIST
import org.cxct.sportlottery.network.Constants.OUTRIGHT_RESULT_LIST
import org.cxct.sportlottery.network.Constants.SPORT_MENU
import org.cxct.sportlottery.network.Constants.USER_MONEY
import org.cxct.sportlottery.network.Constants.USER_NOTICE_LIST
import org.cxct.sportlottery.network.InfoCenter.InfoCenterResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.add.BetAddResult
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.bet.list.BetListResult
import org.cxct.sportlottery.network.index.LoginResult
import org.cxct.sportlottery.network.index.LogoutResult
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.outright.OutrightResultListResult
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.network.user.UserMoneyResult
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException


object ErrorUtils {

    @Nullable
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
                    (url.contains(MATCH_BET_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return BetListResult(it.code, it.msg, null, it.success, null) as T
                    }
                    (url.contains(MATCH_PRELOAD)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return MatchPreloadResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(MATCH_ODDS_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return OddsListResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(SPORT_MENU)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return SportMenuResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(LEAGUE_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return LeagueListResult(it.code, it.msg, null, it.success, null) as T
                    }
                    (url.contains(OUTRIGHT_RESULT_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return OutrightResultListResult(
                            it.code,
                            it.msg,
                            null,
                            it.success,
                            null
                        ) as T
                    }
                    (url.contains(USER_MONEY)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return UserMoneyResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(INDEX_LOGOUT)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return LogoutResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(USER_NOTICE_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return InfoCenterResult(it.code, it.msg, it.success,null,null) as T
                    }
                }
            }
        }

        return null
    }
}