package org.cxct.sportlottery.network.error

import androidx.annotation.Nullable
import okhttp3.ResponseBody
import org.cxct.sportlottery.network.Constants.INDEX_CHECK_EXIST
import org.cxct.sportlottery.network.Constants.BANK_ADD
import org.cxct.sportlottery.network.Constants.BANK_MY
import org.cxct.sportlottery.network.Constants.INDEX_CONFIG
import org.cxct.sportlottery.network.Constants.INDEX_LOGIN
import org.cxct.sportlottery.network.Constants.INDEX_LOGOUT
import org.cxct.sportlottery.network.Constants.INDEX_REGISTER
import org.cxct.sportlottery.network.Constants.INDEX_SEND_SMS
import org.cxct.sportlottery.network.Constants.INDEX_VALIDATE_CODE
import org.cxct.sportlottery.network.Constants.LEAGUE_LIST
import org.cxct.sportlottery.network.Constants.MATCH_BET_ADD
import org.cxct.sportlottery.network.Constants.MATCH_BET_INFO
import org.cxct.sportlottery.network.Constants.MATCH_BET_LIST
import org.cxct.sportlottery.network.Constants.MATCH_ODDS_LIST
import org.cxct.sportlottery.network.Constants.MATCH_PRELOAD
import org.cxct.sportlottery.network.Constants.MATCH_RESULT_LIST
import org.cxct.sportlottery.network.Constants.MESSAGE_LIST
import org.cxct.sportlottery.network.Constants.OUTRIGHT_ODDS_LIST
import org.cxct.sportlottery.network.Constants.OUTRIGHT_RESULT_LIST
import org.cxct.sportlottery.network.Constants.OUTRIGHT_SEASON_LIST
import org.cxct.sportlottery.network.Constants.SPORT_MENU
import org.cxct.sportlottery.network.Constants.USER_MONEY
import org.cxct.sportlottery.network.Constants.USER_NOTICE_LIST
import org.cxct.sportlottery.network.infoCenter.InfoCenterResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bank.MyResult
import org.cxct.sportlottery.network.bank.add.BankAddResult
import org.cxct.sportlottery.network.bet.add.BetAddResult
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.bet.list.BetListResult
import org.cxct.sportlottery.network.index.checkAccount.CheckAccountResult
import org.cxct.sportlottery.network.index.config.ConfigResult
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.logout.LogoutResult
import org.cxct.sportlottery.network.index.sendSms.SmsResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.matchresult.list.MatchResultListResult
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.outright.OutrightResultListResult
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.network.outright.season.OutrightSeasonListResult
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
                throw e
            }
        }

        error?.let {
            if (it.success != null && it.code != null && it.msg != null) {
                val url = response.raw().request.url.toString()
                when {
                    (url.contains(INDEX_LOGIN) || url.contains(INDEX_REGISTER) ) -> {
                        @Suppress("UNCHECKED_CAST")
                        return LoginResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(INDEX_LOGOUT)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return LogoutResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(INDEX_CONFIG)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return ConfigResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(INDEX_VALIDATE_CODE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return ValidCodeResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(INDEX_SEND_SMS)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return SmsResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(INDEX_CHECK_EXIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return CheckAccountResult(it.code, it.msg, it.success) as T
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
                        return BetListResult(it.code, it.msg, null, it.success, null, null) as T
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
                    (url.contains(OUTRIGHT_ODDS_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return OutrightOddsListResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(OUTRIGHT_SEASON_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return OutrightSeasonListResult(
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
                    (url.contains(USER_NOTICE_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return InfoCenterResult(it.code, it.msg, it.success, null, null) as T
                    }
                    (url.contains(MATCH_RESULT_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return MatchResultListResult(it.code, it.msg, null, it.success, null) as T
                    }
                    (url.contains(BANK_MY)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return MyResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(BANK_ADD)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return BankAddResult(it.code, it.msg, it.success) as T
                    }
                }
            }
        }

        return null
    }
}