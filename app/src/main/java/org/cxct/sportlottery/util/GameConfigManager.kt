package org.cxct.sportlottery.util

import androidx.annotation.DrawableRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.GameType
import java.util.*

object GameConfigManager {

    var maxBetMoney: Int? = null //会员对应vip层级的单注最大下注额

    var maxParlayBetMoney: Int? = null//会员对应vip层级的串关最大下注额

    var maxCpBetMoney: Int? = null//会员对应vip层级的单注冠军最大下注额


    /**
     * 獲取第三方遊戲 首頁 icon 資源
     *
     *  @param gameCategory: 需轉換成小寫
     *  @param firmCode: 需轉換成小寫
     */
    fun getThirdGameHomeIcon(gameCategory: String?, firmCode: String?, language: String): String {
        return Constants.getBaseUrl() + "/staticResource/img/game/${gameCategory?.toLowerCase(Locale.getDefault())}/${language}/home-vision__${
            firmCode?.toLowerCase(
                Locale.getDefault()
            )
        }.png"
    }

    /**
     * 獲取第三方遊戲 遊戲大廳 icon 資源
     * 第二層對應 gameFirmMap
     *  @param gameCategory: 需轉換成小寫
     *  @param firmCode: 需轉換成小寫
     */
    fun getThirdGameHallIconUrl(
        gameCategory: String?,
        firmCode: String?,
        language: String
    ): String {
        return Constants.getBaseUrl() + "/staticResource/img/game/${gameCategory?.toLowerCase(Locale.getDefault())}/${language}/game-hall-vision__${
            firmCode?.toLowerCase(
                Locale.getDefault()
            )
        }.png"
    }

    /**
     * 獲取第三方遊戲 遊戲大廳 icon 資源 (電子 tab icon 使用)
     * 第二層對應 gameFirmMap
     *  @param gameCategory: 需轉換成小寫
     *  @param firmCode: 需轉換成小寫
     */
    fun getThirdGameHallDZIconUrl(
        gameCategory: String?,
        firmCode: String?,
        language: String
    ): String {
        return Constants.getBaseUrl() + "/staticResource/img/game/${gameCategory?.toLowerCase(Locale.getDefault())}/${language}/game-hall-vision__${
            firmCode?.toLowerCase(
                Locale.getDefault()
            )
        }_active.png"
    }

    /**
     * 獲取第三方遊戲 遊戲大廳 icon 資源
     *
     *  @param gameCategory: 需轉換成小寫
     *  @param firmCode: 需轉換成小寫
     */
    fun getThirdGameLogoIconUrl(gameCategory: String?, firmCode: String?): String {
        return Constants.getBaseUrl() + "/staticResource/img/game/${gameCategory?.toLowerCase(Locale.getDefault())}/logo-${
            firmCode?.toLowerCase(
                Locale.getDefault()
            )
        }.png"
    }

    /**
     * 獲取第三方遊戲 遊戲大廳 icon 資源
     * 第三層對應 thirdDictMap
     * @param gameCategory: 需轉換成小寫
     */
    fun getThirdGameIconUrl(gameCategory: String?, h5ImageName: String?): String {
        return Constants.getBaseUrl() + "/staticResource/img/gameicons/${
            gameCategory?.toLowerCase(
                Locale.getDefault()
            )
        }/newimg/$h5ImageName"
    }


    //
    //賽事首頁
    //
    @DrawableRes
    fun getGameIcon(code: String?): Int? {
        return when (code) {
            GameType.FT.key -> R.drawable.ic_soccer
            GameType.BK.key -> R.drawable.ic_basketball_icon
            GameType.TN.key -> R.drawable.ic_tennis_icon
            GameType.VB.key -> R.drawable.ic_volley_ball
            GameType.BM.key -> R.drawable.ic_sport_badminton
            GameType.TT.key -> R.drawable.ic_sport_pingpong
            GameType.IH.key -> R.drawable.ic_sport_iceball
            GameType.BX.key -> R.drawable.ic_sport_boxing
            GameType.CB.key -> R.drawable.ic_sport_snooker
            GameType.CK.key -> R.drawable.ic_sport_cricket
            GameType.BB.key -> R.drawable.ic_sport_baseball
            GameType.RB.key -> R.drawable.ic_sport_rugby
            GameType.AFT.key -> R.drawable.ic_sport_amfootball
            GameType.MR.key -> R.drawable.ic_sport_racing
            GameType.GF.key -> R.drawable.ic_sport_golf
            else -> null
        }
    }

    @DrawableRes
    fun getTitleBarBackground(code: String?, nightMode: Boolean): Int? {
        if (nightMode) {
            return R.drawable.bg_transaction_record
        } else {
            return when (code) {
                GameType.FT.key -> R.drawable.img_home_title_soccer_background
                GameType.BK.key -> R.drawable.img_home_title_basketball_background
                GameType.TN.key -> R.drawable.img_home_title_tennis_background
                GameType.VB.key -> R.drawable.img_home_title_volleyball_background
                GameType.BM.key -> R.drawable.img_home_title_bm_background
                GameType.TT.key -> R.drawable.img_home_title_tt_background
                GameType.IH.key -> R.drawable.img_home_title_ih_background
                GameType.BX.key -> R.drawable.img_home_title_bx_background
                GameType.CB.key -> R.drawable.img_home_title_cb_background
                GameType.CK.key -> R.drawable.img_home_title_ck_background
                GameType.BB.key -> R.drawable.img_home_title_bb_background
                GameType.RB.key -> R.drawable.img_home_title_rb_background
                GameType.AFT.key -> R.drawable.img_home_title_aft_background
                GameType.MR.key -> R.drawable.img_home_title_mr_background
                GameType.GF.key -> R.drawable.img_home_title_gf_background
                GameType.ES.key -> R.drawable.img_home_title_es_background
                else -> null
            }
        }
    }

    @DrawableRes
    fun getTitleBarBackgroundInPublicPage(code: String?, nightMode: Boolean): Int? {
        if (nightMode) {
            return R.drawable.bg_transaction_record
        } else {
            return when (code) {
                GameType.FT.key -> R.drawable.soccer48
                GameType.BK.key -> R.drawable.basketball48
                GameType.TN.key -> R.drawable.tennis48
                GameType.VB.key -> R.drawable.volleyball48
                GameType.BM.key -> R.drawable.badminton_100
                GameType.TT.key -> R.drawable.pingpong_100
                GameType.IH.key -> R.drawable.icehockey_100
                GameType.BX.key -> R.drawable.boxing_100
                GameType.CB.key -> R.drawable.snooker_100
                GameType.CK.key -> R.drawable.cricket_100
                GameType.BB.key -> R.drawable.baseball_100
                GameType.RB.key -> R.drawable.rugby_100
                GameType.AFT.key -> R.drawable.amfootball_100
                GameType.MR.key -> R.drawable.rancing_100
                GameType.GF.key -> R.drawable.golf_108
                GameType.ES.key -> R.drawable.esport_100
                else -> null
            }
        }
    }


}