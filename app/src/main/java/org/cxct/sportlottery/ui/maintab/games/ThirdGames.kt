package org.cxct.sportlottery.ui.maintab.games


data class ThirdGames(val zhName: String, val enName: String, var playCode: String, var sysOpen: Int = 1, var open: Int = 0) {

    fun isMaintenance() = 0 == sysOpen //是否维护中
    fun isEnable() = 1 == open // 该游戏可游玩

    companion object {

        val qipai = mutableListOf<ThirdGames>(ThirdGames("Ok棋牌", "OkGames", "CGQP"), ThirdGames("开元棋牌", "KY", "KY"))
        val live = mutableListOf<ThirdGames>(ThirdGames("ON真人", "ON Live Casino", "CGLIVE"),
            ThirdGames("AE真人", "AE Live Casino", "AWC"),
            ThirdGames("Ok棋牌", "OkGames", "CGQP"))

        val caipiao = mutableListOf<ThirdGames>(ThirdGames("Ok棋牌", "ON Lottery", "CGQP"))

    }

}