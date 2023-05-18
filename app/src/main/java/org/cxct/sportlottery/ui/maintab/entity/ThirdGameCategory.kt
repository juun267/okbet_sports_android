package org.cxct.sportlottery.ui.maintab.entity

import androidx.annotation.DrawableRes
import org.cxct.sportlottery.R

enum class ThirdGameCategory(@DrawableRes val iconRes: Int, var title: Int ?= null) {
    LOCAL_SP(R.drawable.selector_main_tab_sport, R.string.B001), //體育
    CGCP(R.drawable.selector_main_tab_lottery, R.string.lottery), //CG遊戲
    LIVE(R.drawable.selector_main_tab_live, R.string.live), //真人
    QP(R.drawable.selector_main_tab_poker, R.string.J203), //棋牌
    DZ(R.drawable.selector_main_tab_slot, R.string.slot), //電子
    BY(R.drawable.selector_main_tab_fishing, R.string.fishing), //捕魚

    MAIN(-1, null), //首頁
    UNKNOWN(-1, null); //未知

    companion object {
        fun getCategory(cateCode: String?): ThirdGameCategory {
            return when (cateCode) {
                LOCAL_SP.name -> LOCAL_SP
                CGCP.name -> CGCP
                QP.name -> QP
                LIVE.name -> LIVE
                DZ.name -> DZ
                BY.name -> BY
                else -> UNKNOWN
            }
        }
    }
}