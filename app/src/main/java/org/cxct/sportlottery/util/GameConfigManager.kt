package org.cxct.sportlottery.util

import androidx.annotation.DrawableRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.SportType
import java.util.*

object GameConfigManager {

    /**
     * 獲取第三方遊戲 首頁 icon 資源
     *
     *  @param gameCategory: 需轉換成小寫
     *  @param firmCode: 需轉換成小寫
     */
    fun getThirdGameHomeIcon(gameCategory: String?, firmCode: String?): String {
        return Constants.getBaseUrl() + "/staticResource/img/game/${gameCategory?.toLowerCase(Locale.getDefault())}/home-vision__${firmCode?.toLowerCase(Locale.getDefault())}.png"
    }

    /**
     * 獲取第三方遊戲 遊戲大廳 icon 資源
     * 第二層對應 gameFirmMap
     *  @param gameCategory: 需轉換成小寫
     *  @param firmCode: 需轉換成小寫
     */
    fun getThirdGameHallIconUrl(gameCategory: String?, firmCode: String?): String {
        return Constants.getBaseUrl() + "/staticResource/img/game/${gameCategory?.toLowerCase(Locale.getDefault())}/game-hall-vision__${firmCode?.toLowerCase(Locale.getDefault())}.png"
    }

    /**
     * 獲取第三方遊戲 遊戲大廳 icon 資源 (電子 tab icon 使用)
     * 第二層對應 gameFirmMap
     *  @param gameCategory: 需轉換成小寫
     *  @param firmCode: 需轉換成小寫
     */
    fun getThirdGameHallDZIconUrl(gameCategory: String?, firmCode: String?): String {
        return Constants.getBaseUrl() + "/staticResource/img/game/${gameCategory?.toLowerCase(Locale.getDefault())}/game-hall-vision__${firmCode?.toLowerCase(Locale.getDefault())}_active.png"
    }

    /**
     * 獲取第三方遊戲 遊戲大廳 icon 資源
     *
     *  @param gameCategory: 需轉換成小寫
     *  @param firmCode: 需轉換成小寫
     */
    fun getThirdGameLogoIconUrl(gameCategory: String?, firmCode: String?): String {
        return Constants.getBaseUrl() + "/staticResource/img/game/${gameCategory?.toLowerCase(Locale.getDefault())}/logo-${firmCode?.toLowerCase(Locale.getDefault())}.png"
    }

    /**
     * 獲取第三方遊戲 遊戲大廳 icon 資源
     * 第三層對應 thirdDictMap
     * @param gameCategory: 需轉換成小寫
     */
    fun getThirdGameIconUrl(gameCategory: String?, h5ImageName: String?): String {
        return Constants.getBaseUrl() + "/staticResource/img/gameicons/${gameCategory?.toLowerCase(Locale.getDefault())}/newimg/$h5ImageName"
    }




    //
    //賽事首頁
    //
    @DrawableRes
    fun getGameIcon(code: String?): Int {
        return when (code) {
            SportType.FOOTBALL.code -> R.drawable.ic_soccer
            SportType.BASKETBALL.code -> R.drawable.ic_basketball_icon
            SportType.TENNIS.code -> R.drawable.ic_tennis_icon
            SportType.VOLLEYBALL.code -> R.drawable.ic_volley_ball
            SportType.BADMINTON.code -> R.drawable.ic_badminton_icon
            else -> -1
        }
    }

    @DrawableRes
    fun getTitleBarBackground(code: String?): Int {
        return when (code) {
            SportType.FOOTBALL.code -> R.drawable.img_home_title_soccer_background
            SportType.BASKETBALL.code -> R.drawable.img_home_title_basketball_background
            SportType.TENNIS.code -> R.drawable.img_home_title_tennis_background
            SportType.VOLLEYBALL.code -> R.drawable.img_home_title_volleyball_background
            SportType.BADMINTON.code -> -1 //20210624 紀錄：說沒有羽球賽事了，所以沒做圖
            else -> -1
        }
    }

}