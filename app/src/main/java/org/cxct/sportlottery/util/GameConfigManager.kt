package org.cxct.sportlottery.util

import androidx.annotation.DrawableRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.GameType
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
    fun getGameIcon(code: String?): Int? {
        return when (code) {
            GameType.FT.key -> R.drawable.ic_soccer
            GameType.BK.key -> R.drawable.ic_basketball_icon
            GameType.TN.key -> R.drawable.ic_tennis_icon
            GameType.VB.key -> R.drawable.ic_volley_ball
            else -> null
        }
    }

    @DrawableRes
    fun getTitleBarBackground(code: String?): Int? {
        return when (code) {
            GameType.FT.key -> R.drawable.img_home_title_soccer_background
            GameType.BK.key -> R.drawable.img_home_title_basketball_background
            GameType.TN.key -> R.drawable.img_home_title_tennis_background
            GameType.VB.key -> R.drawable.img_home_title_volleyball_background
            else -> null
        }
    }

}