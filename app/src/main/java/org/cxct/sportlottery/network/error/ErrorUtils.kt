package org.cxct.sportlottery.network.error

import androidx.annotation.Nullable
import okhttp3.ResponseBody
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.BANK_ADD
import org.cxct.sportlottery.network.Constants.BANK_DELETE
import org.cxct.sportlottery.network.Constants.BANK_MY
import org.cxct.sportlottery.network.Constants.FEEDBACK_QUERYDETAIL
import org.cxct.sportlottery.network.Constants.FEEDBACK_QUERYLIST
import org.cxct.sportlottery.network.Constants.FEEDBACK_REPLY
import org.cxct.sportlottery.network.Constants.FEEDBACK_SAVE
import org.cxct.sportlottery.network.Constants.INDEX_CHECK_EXIST
import org.cxct.sportlottery.network.Constants.INDEX_CHECK_TOKEN
import org.cxct.sportlottery.network.Constants.INDEX_CONFIG
import org.cxct.sportlottery.network.Constants.INDEX_LOGIN
import org.cxct.sportlottery.network.Constants.INDEX_LOGOUT
import org.cxct.sportlottery.network.Constants.INDEX_PROMOTENOTICE
import org.cxct.sportlottery.network.Constants.INDEX_REGISTER
import org.cxct.sportlottery.network.Constants.INDEX_SEND_SMS
import org.cxct.sportlottery.network.Constants.INDEX_VALIDATE_CODE
import org.cxct.sportlottery.network.Constants.LEAGUE_LIST
import org.cxct.sportlottery.network.Constants.LOGIN_FOR_GUEST
import org.cxct.sportlottery.network.Constants.MATCH_BET_ADD
import org.cxct.sportlottery.network.Constants.MATCH_BET_INFO
import org.cxct.sportlottery.network.Constants.MATCH_BET_LIST
import org.cxct.sportlottery.network.Constants.MATCH_ODDS_LIST
import org.cxct.sportlottery.network.Constants.MATCH_PRELOAD
import org.cxct.sportlottery.network.Constants.MATCH_RESULT_LIST
import org.cxct.sportlottery.network.Constants.OUTRIGHT_BET_ADD
import org.cxct.sportlottery.network.Constants.OUTRIGHT_BET_INFO
import org.cxct.sportlottery.network.Constants.OUTRIGHT_ODDS_LIST
import org.cxct.sportlottery.network.Constants.OUTRIGHT_RESULT_LIST
import org.cxct.sportlottery.network.Constants.OUTRIGHT_SEASON_LIST
import org.cxct.sportlottery.network.Constants.QUERY_FIRST_ORDERS
import org.cxct.sportlottery.network.Constants.QUERY_SECOND_ORDERS
import org.cxct.sportlottery.network.Constants.RECHARGE_CONFIG_MAP
import org.cxct.sportlottery.network.Constants.SPORT_MENU
import org.cxct.sportlottery.network.Constants.THIRD_ALL_TRANSFER_OUT
import org.cxct.sportlottery.network.Constants.THIRD_AUTO_TRANSFER
import org.cxct.sportlottery.network.Constants.THIRD_GAMES
import org.cxct.sportlottery.network.Constants.THIRD_REBATES
import org.cxct.sportlottery.network.Constants.THIRD_GET_ALL_BALANCE
import org.cxct.sportlottery.network.Constants.THIRD_LOGIN
import org.cxct.sportlottery.network.Constants.THIRD_QUERY_TRANSFERS
import org.cxct.sportlottery.network.Constants.THIRD_TRANSFER
import org.cxct.sportlottery.network.Constants.UPLOAD_IMG
import org.cxct.sportlottery.network.Constants.USER_EDIT_ICON_URL
import org.cxct.sportlottery.network.Constants.USER_EDIT_NICKNAME
import org.cxct.sportlottery.network.Constants.USER_INFO
import org.cxct.sportlottery.network.Constants.USER_LEVEL_GROWTH
import org.cxct.sportlottery.network.Constants.USER_MONEY
import org.cxct.sportlottery.network.Constants.USER_NOTICE_LIST
import org.cxct.sportlottery.network.Constants.USER_RECHARGE_ADD
import org.cxct.sportlottery.network.Constants.USER_RECHARGE_LIST
import org.cxct.sportlottery.network.Constants.USER_RECHARGE_ONLINE_PAY
import org.cxct.sportlottery.network.Constants.USER_UPDATE_FUND_PWD
import org.cxct.sportlottery.network.Constants.USER_UPDATE_PWD
import org.cxct.sportlottery.network.Constants.USER_WITHDRAW_INFO
import org.cxct.sportlottery.network.Constants.WITHDRAW_ADD
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bank.add.BankAddResult
import org.cxct.sportlottery.network.bank.delete.BankDeleteResult
import org.cxct.sportlottery.network.bank.my.BankMyResult
import org.cxct.sportlottery.network.bet.add.BetAddResult
import org.cxct.sportlottery.network.bet.info.BetInfoResult
import org.cxct.sportlottery.network.bet.list.BetListResult
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.feedback.FeedBackBaseResult
import org.cxct.sportlottery.network.feedback.FeedbackListResult
import org.cxct.sportlottery.network.index.checkAccount.CheckAccountResult
import org.cxct.sportlottery.network.index.config.ConfigResult
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.logout.LogoutResult
import org.cxct.sportlottery.network.index.sendSms.SmsResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.network.infoCenter.InfoCenterResult
import org.cxct.sportlottery.network.league.LeagueListResult
import org.cxct.sportlottery.network.match.MatchPreloadResult
import org.cxct.sportlottery.network.matchresult.list.MatchResultListResult
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.money.MoneyAddResult
import org.cxct.sportlottery.network.money.config.MoneyRechCfgResult
import org.cxct.sportlottery.network.money.list.RechargeListResult
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.outright.OutrightResultListResult
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.network.outright.season.OutrightSeasonListResult
import org.cxct.sportlottery.network.sport.MyFavoriteBaseResult
import org.cxct.sportlottery.network.sport.MyFavoriteMatchResult
import org.cxct.sportlottery.network.sport.SportMenuFavoriteResult
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.network.third_game.AutoTransferResult
import org.cxct.sportlottery.network.third_game.BlankResult
import org.cxct.sportlottery.network.third_game.ThirdLoginResult
import org.cxct.sportlottery.network.third_game.money_transfer.GetAllBalanceResult
import org.cxct.sportlottery.network.third_game.query_transfers.QueryTransfersResult
import org.cxct.sportlottery.network.third_game.third_games.ThirdGamesResult
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.OtherBetHistoryResult
import org.cxct.sportlottery.network.uploadImg.UploadImgResult
import org.cxct.sportlottery.network.user.iconUrl.IconUrlResult
import org.cxct.sportlottery.network.user.info.UserInfoResult
import org.cxct.sportlottery.network.user.money.UserMoneyResult
import org.cxct.sportlottery.network.user.nickname.NicknameResult
import org.cxct.sportlottery.network.user.setWithdrawInfo.WithdrawInfoResult
import org.cxct.sportlottery.network.user.updateFundPwd.UpdateFundPwdResult
import org.cxct.sportlottery.network.user.updatePwd.UpdatePwdResult
import org.cxct.sportlottery.network.vip.growth.LevelGrowthResult
import org.cxct.sportlottery.network.vip.thirdRebates.ThirdRebatesResult
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddResult
import org.cxct.sportlottery.network.withdraw.list.WithdrawListResult
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
                    (url.contains(INDEX_LOGIN) || url.contains(INDEX_REGISTER) || url.contains(
                        INDEX_CHECK_TOKEN
                    )) -> {
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
                    (url.contains(INDEX_PROMOTENOTICE)) -> {
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
                    (url.contains(Constants.MYFAVORITE_QUERY)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return SportMenuFavoriteResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(Constants.MYFAVORITE_MATCH_QUERY)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return MyFavoriteMatchResult(it.code, it.msg, it.success, null,null) as T
                    }
                    (url.contains(Constants.MYFAVORITE_SAVE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return MyFavoriteBaseResult(it.code, it.msg, it.success,null) as T
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
                    (url.contains(USER_INFO)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return UserInfoResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(USER_MONEY)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return UserMoneyResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(USER_EDIT_NICKNAME)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NicknameResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(USER_EDIT_ICON_URL)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return IconUrlResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(USER_UPDATE_PWD)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return UpdatePwdResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(USER_UPDATE_FUND_PWD)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return UpdateFundPwdResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(USER_NOTICE_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return InfoCenterResult(it.code, it.msg, it.success, null, null) as T
                    }
                    (url.contains(MATCH_RESULT_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return MatchResultListResult(it.code, it.msg, null, it.success, null) as T
                    }
                    (url.contains(RECHARGE_CONFIG_MAP)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return MoneyRechCfgResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(UPLOAD_IMG)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return UploadImgResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(BANK_MY)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return BankMyResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(BANK_ADD)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return BankAddResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(BANK_DELETE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return BankDeleteResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(WITHDRAW_ADD)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return WithdrawAddResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(USER_RECHARGE_ADD)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return MoneyAddResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(USER_RECHARGE_ONLINE_PAY)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return MoneyAddResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(USER_RECHARGE_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return RechargeListResult(it.code, it.msg, null, it.success, null) as T
                    }
                    (url.contains(OUTRIGHT_BET_ADD)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return BetAddResult(it.code, it.msg, null, it.success, null) as T
                    }
                    (url.contains(OUTRIGHT_BET_INFO)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return BetInfoResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(WITHDRAW_ADD)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return WithdrawListResult(it.code, it.msg, null, it.success, null) as T
                    }
                    (url.contains(FEEDBACK_QUERYLIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return FeedbackListResult(it.code, it.msg, it.success, null, null) as T
                    }
                    (url.contains(FEEDBACK_SAVE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return FeedBackBaseResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(FEEDBACK_REPLY)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return FeedBackBaseResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(FEEDBACK_QUERYDETAIL)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return FeedBackBaseResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(THIRD_GET_ALL_BALANCE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return GetAllBalanceResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(THIRD_ALL_TRANSFER_OUT)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return BlankResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(THIRD_GAMES)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return ThirdGamesResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(THIRD_TRANSFER.replace("{outPlat}/{inPlat}/transfer?=amount", ""))
                            && url.contains("transfer?=")) -> {
                        @Suppress("UNCHECKED_CAST")
                        return BlankResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(THIRD_QUERY_TRANSFERS)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return QueryTransfersResult(it.code, it.msg, it.success, null, null) as T
                    }
                    (url.contains(THIRD_AUTO_TRANSFER.replace("{inPlat}/autoTransfer", ""))
                            && url.contains("autoTransfer")) -> {
                        @Suppress("UNCHECKED_CAST")
                        return AutoTransferResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(THIRD_LOGIN.replace("{firmType}/login", ""))
                            && url.contains("login")) -> {
                        @Suppress("UNCHECKED_CAST")
                        return ThirdLoginResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(USER_LEVEL_GROWTH)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return LevelGrowthResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(QUERY_FIRST_ORDERS)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return OtherBetHistoryResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(QUERY_SECOND_ORDERS)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return QueryTransfersResult(it.code, it.msg, it.success, null, null) as T
                    }
                    (url.contains(USER_WITHDRAW_INFO)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return WithdrawInfoResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(LOGIN_FOR_GUEST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return LoginResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(THIRD_REBATES)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return ThirdRebatesResult(it.code, it.msg, it.success, null) as T
                    }
                }
            }
        }

        return null
    }
}