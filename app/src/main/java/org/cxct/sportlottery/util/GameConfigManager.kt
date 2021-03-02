package org.cxct.sportlottery.util

import org.cxct.sportlottery.network.Constants
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
     *
     *  @param gameCategory: 需轉換成小寫
     *  @param firmCode: 需轉換成小寫
     */
    fun getThirdGameHallIcon(gameCategory: String?, firmCode: String?): String {
        return Constants.getBaseUrl() + "/staticResource/img/game/${gameCategory?.toLowerCase(Locale.getDefault())}/game-hall-vision__${firmCode?.toLowerCase(Locale.getDefault())}.png"
    }

}