package org.cxct.sportlottery.network.error

import androidx.annotation.Nullable
import okhttp3.ResponseBody
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.Constants.BANK_ADD
import org.cxct.sportlottery.network.Constants.BANK_DELETE
import org.cxct.sportlottery.network.Constants.BANK_MY
import org.cxct.sportlottery.network.Constants.BETTING_STATION_QUERY_INVITE
import org.cxct.sportlottery.network.Constants.BIND_FACEBOOK
import org.cxct.sportlottery.network.Constants.BIND_GOOGLE
import org.cxct.sportlottery.network.Constants.FACEBOOK_LOGIN
import org.cxct.sportlottery.network.Constants.FEEDBACK_QUERYDETAIL
import org.cxct.sportlottery.network.Constants.FEEDBACK_QUERYLIST
import org.cxct.sportlottery.network.Constants.FEEDBACK_REPLY
import org.cxct.sportlottery.network.Constants.FEEDBACK_SAVE
import org.cxct.sportlottery.network.Constants.FORGET_PASSWORD_SMS
import org.cxct.sportlottery.network.Constants.FORGET_PASSWORD_VALIDATE_EMAIL
import org.cxct.sportlottery.network.Constants.GET_TWO_FACTOR_STATUS
import org.cxct.sportlottery.network.Constants.GOOGLE_LOGIN
import org.cxct.sportlottery.network.Constants.INDEX_CHECK_TOKEN
import org.cxct.sportlottery.network.Constants.INDEX_CONFIG
import org.cxct.sportlottery.network.Constants.LOGIN
import org.cxct.sportlottery.network.Constants.INDEX_LOGOUT
import org.cxct.sportlottery.network.Constants.INDEX_PROMOTENOTICE
import org.cxct.sportlottery.network.Constants.INDEX_REGISTER
import org.cxct.sportlottery.network.Constants.INDEX_VALIDATE_CODE
import org.cxct.sportlottery.network.Constants.LOGIN_FOR_GUEST
import org.cxct.sportlottery.network.Constants.LOGIN_OR_REG
import org.cxct.sportlottery.network.Constants.LOGIN_OR_REG_SEND_VALIDCODE
import org.cxct.sportlottery.network.Constants.MATCH_BET_ADD
import org.cxct.sportlottery.network.Constants.MATCH_BET_INFO
import org.cxct.sportlottery.network.Constants.MATCH_BET_LIST
import org.cxct.sportlottery.network.Constants.MATCH_BET_REMARK_BET
import org.cxct.sportlottery.network.Constants.MATCH_ODDS_LIST
import org.cxct.sportlottery.network.Constants.MATCH_RESULT_LIST
import org.cxct.sportlottery.network.Constants.ODDS_CHANGE_OPTION
import org.cxct.sportlottery.network.Constants.OUTRIGHT_BET_INFO
import org.cxct.sportlottery.network.Constants.OUTRIGHT_ODDS_LIST
import org.cxct.sportlottery.network.Constants.OUTRIGHT_RESULT_LIST
import org.cxct.sportlottery.network.Constants.PASSWORD_VERIFY
import org.cxct.sportlottery.network.Constants.QUERY_FIRST_ORDERS
import org.cxct.sportlottery.network.Constants.QUERY_SECOND_ORDERS
import org.cxct.sportlottery.network.Constants.RECHARGE_CONFIG_MAP
import org.cxct.sportlottery.network.Constants.RESET_FORGET_PASSWORD
import org.cxct.sportlottery.network.Constants.SEND_TWO_FACTOR
import org.cxct.sportlottery.network.Constants.SPORT_MENU
import org.cxct.sportlottery.network.Constants.SPORT_MENU_CATEGORYLIST
import org.cxct.sportlottery.network.Constants.SPORT_PUBLICITY_RECOMMEND
import org.cxct.sportlottery.network.Constants.THIRD_ALL_TRANSFER_OUT
import org.cxct.sportlottery.network.Constants.THIRD_AUTO_TRANSFER
import org.cxct.sportlottery.network.Constants.THIRD_GAMES
import org.cxct.sportlottery.network.Constants.THIRD_GET_ALL_BALANCE
import org.cxct.sportlottery.network.Constants.THIRD_LOGIN
import org.cxct.sportlottery.network.Constants.THIRD_QUERY_TRANSFERS
import org.cxct.sportlottery.network.Constants.THIRD_TRANSFER
import org.cxct.sportlottery.network.Constants.UPLOAD_IMG
import org.cxct.sportlottery.network.Constants.UPLOAD_VERIFY_PHOTO
import org.cxct.sportlottery.network.Constants.USER_EDIT_ICON_URL
import org.cxct.sportlottery.network.Constants.USER_EDIT_NICKNAME
import org.cxct.sportlottery.network.Constants.USER_INFO
import org.cxct.sportlottery.network.Constants.USER_MONEY
import org.cxct.sportlottery.network.Constants.USER_NOTICE_LIST
import org.cxct.sportlottery.network.Constants.USER_RECHARGE_ADD
import org.cxct.sportlottery.network.Constants.USER_RECHARGE_LIST
import org.cxct.sportlottery.network.Constants.USER_RECHARGE_ONLINE_PAY
import org.cxct.sportlottery.network.Constants.USER_UPDATE_FUND_PWD
import org.cxct.sportlottery.network.Constants.USER_UPDATE_PWD
import org.cxct.sportlottery.network.Constants.USER_WITHDRAW_INFO
import org.cxct.sportlottery.network.Constants.VALIDATE_TWO_FACTOR
import org.cxct.sportlottery.network.Constants.WITHDRAW_ADD
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bank.my.BankMyResult
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.network.bet.list.BetListResult
import org.cxct.sportlottery.network.bet.settledDetailList.BetInfoResult
import org.cxct.sportlottery.network.bet.settledList.RemarkBetResult
import org.cxct.sportlottery.network.feedback.FeedbackListResult
import org.cxct.sportlottery.network.index.chechBetting.CheckBettingResult
import org.cxct.sportlottery.network.index.config.ConfigResult
import org.cxct.sportlottery.network.index.forgetPassword.ResetPasswordResult
import org.cxct.sportlottery.network.index.forgetPassword.SendSmsResult
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.network.infoCenter.InfoCenterResult
import org.cxct.sportlottery.network.matchresult.list.MatchResultListResult
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.money.MoneyAddResult
import org.cxct.sportlottery.network.money.RedEnvelopePrizeResult
import org.cxct.sportlottery.network.money.RedEnvelopeResult
import org.cxct.sportlottery.network.money.config.MoneyRechCfgResult
import org.cxct.sportlottery.network.money.list.RechargeListResult
import org.cxct.sportlottery.network.money.list.SportBillResult
import org.cxct.sportlottery.network.myfavorite.match.MyFavoriteAllMatchResult
import org.cxct.sportlottery.network.myfavorite.query.SportMenuFavoriteResult
import org.cxct.sportlottery.network.myfavorite.save.MyFavoriteBaseResult
import org.cxct.sportlottery.network.odds.list.OddsListResult
import org.cxct.sportlottery.network.outright.OutrightResultListResult
import org.cxct.sportlottery.network.outright.odds.OutrightOddsListResult
import org.cxct.sportlottery.network.sport.SportMenuFilterResult
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.network.sport.publicityRecommend.PublicityRecommendResult
import org.cxct.sportlottery.network.third_game.money_transfer.GetAllBalanceResult
import org.cxct.sportlottery.network.third_game.query_transfers.QueryTransfersResult
import org.cxct.sportlottery.network.third_game.third_games.ThirdGamesResult
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.OtherBetHistoryResult
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.detail.OtherBetHistoryDetailResult
import org.cxct.sportlottery.network.uploadImg.UploadImgResult
import org.cxct.sportlottery.network.uploadImg.UploadVerifyPhotoResult
import org.cxct.sportlottery.network.user.authbind.AuthBindResult
import org.cxct.sportlottery.network.user.iconUrl.IconUrlResult
import org.cxct.sportlottery.network.user.info.UserInfoResult
import org.cxct.sportlottery.network.user.money.UserMoneyResult
import org.cxct.sportlottery.network.user.setWithdrawInfo.WithdrawInfoResult
import org.cxct.sportlottery.network.withdraw.add.WithdrawAddResult
import retrofit2.Converter
import retrofit2.Response
import timber.log.Timber
import java.io.IOException


object ErrorUtils {

    @Nullable
    fun <T> parseError(response: Response<T>): T? {
        val converter: Converter<ResponseBody, APIError> = OneBoSportApi.retrofit
            .responseBodyConverter(APIError::class.java, arrayOfNulls<Annotation>(0))

        var error: APIError? = null

        response.errorBody()?.let {
            try {
                error =
                    converter.convert(it) // TODO com.squareup.moshi.JsonEncodingException: Use JsonReader.setLenient(true) to accept malformed JSON at path $
            } catch (e: IOException) {
                Timber.e("parseError: $e")
                throw e
            }
        }

        error?.let {
            if (it.success != null && it.code != null && it.msg != null) {
                val url = response.raw().request.url.toString()
                when {
                    (url.contains(Constants.USER_BASIC_INFO_UPDATE))->{
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code,it.msg,it.success) as T
                    }
                    (url.contains(Constants.SEND_SMS_FORGET) or url.contains(Constants.SEND_EMAIL_FORGET)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return SendSmsResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.endsWith(LOGIN)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return LoginResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(Constants.INDEX_SEND_LOGIN_DEVICE_SMS)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.endsWith(INDEX_REGISTER)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return LoginResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(INDEX_CHECK_TOKEN)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(INDEX_LOGOUT)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(INDEX_CONFIG)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return ConfigResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(INDEX_VALIDATE_CODE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return ValidCodeResult(it.code, it.msg, it.success, null) as T
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
                        return BetAddResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(MATCH_BET_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return BetListResult(it.code, it.msg, null, it.success, null, null) as T
                    }


                    (url.contains(MATCH_ODDS_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return OddsListResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(SPORT_MENU_CATEGORYLIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return SportMenuResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(SPORT_MENU)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return SportMenuResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(Constants.SPORT_MENU_FILTER)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return SportMenuFilterResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(Constants.MYFAVORITE_QUERY_ALL)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return MyFavoriteAllMatchResult(it.code,
                            it.msg,
                            it.success,
                            null,
                            null) as T
                    }
                    (url.contains(Constants.MYFAVORITE_QUERY)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return SportMenuFavoriteResult(it.code, it.msg, it.success, null) as T
                    }

                    (url.contains(Constants.MYFAVORITE_SAVE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return MyFavoriteBaseResult(it.code, it.msg, it.success, null) as T
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
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(USER_EDIT_ICON_URL)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return IconUrlResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(USER_UPDATE_PWD)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(USER_UPDATE_FUND_PWD)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
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
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(GET_TWO_FACTOR_STATUS)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(SEND_TWO_FACTOR)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(VALIDATE_TWO_FACTOR)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(BANK_DELETE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(WITHDRAW_ADD)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return WithdrawAddResult(it.code, it.msg, it.success,null) as T
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
                    (url.contains(OUTRIGHT_BET_INFO)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return BetInfoResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(FEEDBACK_QUERYLIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return FeedbackListResult(it.code, it.msg, it.success, null, null) as T
                    }
                    (url.contains(FEEDBACK_SAVE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(FEEDBACK_REPLY)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(FEEDBACK_QUERYDETAIL)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(THIRD_GET_ALL_BALANCE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return GetAllBalanceResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(THIRD_ALL_TRANSFER_OUT)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(THIRD_GAMES)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return ThirdGamesResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(THIRD_TRANSFER.replace("{outPlat}/{inPlat}/transfer?=amount", ""))
                            && url.contains("transfer?=")) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(THIRD_QUERY_TRANSFERS)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return QueryTransfersResult(it.code, it.msg, it.success, null, null) as T
                    }
                    (url.contains(THIRD_AUTO_TRANSFER.replace("{inPlat}/autoTransfer", ""))
                            && url.contains("autoTransfer")) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(THIRD_LOGIN.replace("{firmType}/login", ""))
                            && url.contains("login")) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(QUERY_FIRST_ORDERS)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return OtherBetHistoryResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(QUERY_SECOND_ORDERS)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return OtherBetHistoryDetailResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(USER_WITHDRAW_INFO)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return WithdrawInfoResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(LOGIN_FOR_GUEST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return LoginResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(BETTING_STATION_QUERY_INVITE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return CheckBettingResult(it.code, it.msg, it.success, null) as T
                    }


                    (url.contains(Constants.INDEX_VALIDATE_LOGIN_DEVICE_SMS)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(Constants.USER_BILL_LIST)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return SportBillResult(it.code, it.msg, listOf(), it.success, 0) as T
                    }
                    (url.contains(Constants.RED_ENVELOPE_CHECK)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return RedEnvelopeResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(Constants.RED_ENVELOPE_PRIZE_BASE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return RedEnvelopePrizeResult(it.code, it.msg, it.success, null) as T
                    }

                    (url.contains(PASSWORD_VERIFY)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(UPLOAD_VERIFY_PHOTO)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return UploadVerifyPhotoResult(it.code, it.msg, it.success,null) as T
                    }
                    (url.contains(FORGET_PASSWORD_SMS) or url.contains(FORGET_PASSWORD_VALIDATE_EMAIL)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(RESET_FORGET_PASSWORD) or url.contains(FORGET_PASSWORD_VALIDATE_EMAIL)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return ResetPasswordResult(it.code, it.msg, it.success,null) as T
                    }

                    (url.contains(ODDS_CHANGE_OPTION)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.endsWith(LOGIN_OR_REG_SEND_VALIDCODE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return NetResult(it.code, it.msg, it.success) as T
                    }
                    (url.endsWith(LOGIN_OR_REG)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return LoginResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(GOOGLE_LOGIN)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return LoginResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(FACEBOOK_LOGIN)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return LoginResult(it.code, it.msg, it.success) as T
                    }
                    (url.contains(BIND_GOOGLE)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return AuthBindResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(BIND_FACEBOOK)) -> {
                        @Suppress("UNCHECKED_CAST")
                        return AuthBindResult(it.code, it.msg, it.success, null) as T
                    }
                    (url.contains(MATCH_BET_REMARK_BET)) ->{
                        @Suppress("UNCHECKED_CAST")
                        return RemarkBetResult(it.code,it.msg,it.success,null) as T
                    }
                    (url.contains(SPORT_PUBLICITY_RECOMMEND)) ->{
                        @Suppress("UNCHECKED_CAST")
                        return PublicityRecommendResult(it.code,it.msg,it.success,null) as T
                    }

                }
            }
        }

        return null
    }
}