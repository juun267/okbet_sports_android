package org.cxct.sportlottery.ui.main.entity

import androidx.annotation.DrawableRes
import org.cxct.sportlottery.R

enum class ThirdGameCategory(@DrawableRes val iconRes: Int, var title: String? = null) {
    LOCAL_SP(R.drawable.selector_main_tab_sport), //體育
    CGCP(R.drawable.selector_main_tab_lottery), //CG遊戲
    LIVE(R.drawable.selector_main_tab_live), //真人
    QP(R.drawable.selector_main_tab_poker), //棋牌
    DZ(R.drawable.selector_main_tab_slot), //電子
    BY(R.drawable.selector_main_tab_fishing), //捕魚

    MAIN(-1), //首頁
    UNKNOWN(-1); //未知

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