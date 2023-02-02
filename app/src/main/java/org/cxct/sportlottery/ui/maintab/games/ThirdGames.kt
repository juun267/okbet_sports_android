package org.cxct.sportlottery.ui.maintab.games

import androidx.annotation.DrawableRes
import org.cxct.sportlottery.R


//data class ThirdGames(val zhName: String, val enName: String, var playCode: String, var sysOpen: Int = 1, var open: Int = 0) {
//
//    fun isMaintenance() = 0 == sysOpen //是否维护中
//    fun isEnable() = 1 == open // 该游戏可游玩
//
//    companion object {
//
//        val qipai = mutableListOf<ThirdGames>(ThirdGames("Ok棋牌", "OkGames", "CGQP"), ThirdGames("开元棋牌", "KY", "KY"))
//        val live = mutableListOf<ThirdGames>(ThirdGames("ON真人", "ON Live Casino", "CGLIVE"),
//            ThirdGames("AE真人", "AE Live Casino", "AWC"),
//            ThirdGames("Ok棋牌", "OkGames", "CGQP"))
//
//        val caipiao = mutableListOf<ThirdGames>(ThirdGames("Ok棋牌", "ON Lottery", "CGQP"))
//
//    }
//
//}

data class ThirdGames(val playCode: String,
                      val firmType: String,
                      var sysOpen: Int = 1,
                      var open: Int = 0,
                      @DrawableRes val enableImg: Int,
                      @DrawableRes val disableImg: Int,
                      @DrawableRes val maintenanceImg: Int) {

    fun isMaintenance() = 0 == sysOpen //是否维护中
    fun isEnable() = 1 == open // 该游戏可游玩

    companion object {
        val live = mutableListOf(ThirdGames("CGLIVE", "CGLIVE", -1, 0, R.drawable.img_thirdgame_live_cg0, R.drawable.img_thirdgame_live_cg1, R.drawable.img_thirdgame_live_cg2),
            ThirdGames("AGIN", "AGIN", -1, 0, R.drawable.img_thirdgame_live_ag0, R.drawable.img_thirdgame_live_ag1, R.drawable.img_thirdgame_live_ag2),
            /*ThirdGames("AWC", "AWC", 0, 0, 0, 0, 0)*/)

        val caipiao = mutableListOf(ThirdGames("CGCP", "CGCP", -1, 0, R.drawable.img_thirdgame_lott_cg0, R.drawable.img_thirdgame_lott_cg1, R.drawable.img_thirdgame_lott_cg2))

        val qipai = mutableListOf(ThirdGames("KY", "KY", -1, 0, R.drawable.img_thirdgame_pokert_ky0, R.drawable.img_thirdgame_pokert_ky1, R.drawable.img_thirdgame_pokert_ky2),
            ThirdGames("CGQP", "CGQP", -1, 0, R.drawable.img_thirdgame_pokert_on0, R.drawable.img_thirdgame_pokert_on1, R.drawable.img_thirdgame_pokert_on2))

    }


}