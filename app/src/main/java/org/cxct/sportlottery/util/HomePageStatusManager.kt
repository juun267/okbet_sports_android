package org.cxct.sportlottery.util

object HomePageStatusManager {
    /**
     * 球種滾球賽事當前顯示的matchId
     * <GameType, matchId>
     */
    var inPlaySelectedPage: MutableMap<String, String> = mutableMapOf()

    /**
     * 球種即將開賽賽事當前顯示的matchId
     * <GameType, matchId>
     */
    var atStartSelectedPage: MutableMap<String, String> = mutableMapOf()

    /**
     * 推薦賽事當前顯示的玩法
     * <matchId, playTypeCode>
     */
    var recommendSelectedOdd: MutableMap<String, String> = mutableMapOf()

    /**
     * 清除首頁滾球、即將、推薦賽事當前顯示狀態
     */
    fun clear() {
        inPlaySelectedPage.clear()
        atStartSelectedPage.clear()
        recommendSelectedOdd.clear()
    }
}