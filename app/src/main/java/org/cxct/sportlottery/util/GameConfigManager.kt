package org.cxct.sportlottery.util

import org.cxct.sportlottery.network.Constants
import java.util.*

object GameConfigManager {

    /**
     * 獲取第三方遊戲 icon 資源
     * 第二層 tab icon, 對應 gameFirmMap
     *  @param gameCategory: 需轉換成小寫
     *  @param firmCode: 需轉換成小寫
     */
    fun getThirdGameTabIconUrlFirm(gameCategory: String?, firmCode: String?): String {
        return Constants.getBaseUrl() + "/static/img/firm_imgs/${gameCategory?.toLowerCase(Locale.getDefault())}/${firmCode?.toLowerCase(Locale.getDefault())}.png"
    }

    /**
     * 獲取第三方遊戲 icon 資源
     * 第二層對應 gameFirmMap
     *  @param gameCategory: 需轉換成小寫
     *  @param firmCode: 需轉換成小寫
     */
    fun getThirdGameIconUrlFirm(gameCategory: String?, firmCode: String?): String {
        return Constants.getBaseUrl() + "/static/img/firm_imgs/${gameCategory?.toLowerCase(Locale.getDefault())}/${firmCode?.toLowerCase(Locale.getDefault())}.png"
    }


    enum class ThirdGameIconType {
        HOME, GAME, DZ_TAB
    }

    /**
     * 獲取第三方遊戲 icon 資源
     * 第三層對應 thirdDictMap
     * @param gameCategory: 需轉換成小寫
     */
    fun getThirdGameIconUrl(gameCategory: String?, h5ImageName: String?): String {
        if (h5ImageName == null) {
            return ""
        }
        if (h5ImageName.contains("http")) {
            return h5ImageName
        }
        return Constants.getBaseUrl() + "/static/img/gameicons/${gameCategory?.toLowerCase(Locale.getDefault())}/newimg/$h5ImageName"
    }

}