package org.cxct.sportlottery.ui.main.entity

import androidx.annotation.DrawableRes
import org.cxct.sportlottery.R

enum class ThirdGameCategory(@DrawableRes val iconRes: Int, var title: String? = null) {
    LOCAL_SP(R.drawable.ic_sport), //體育
    CGCP(R.drawable.ic_cp), //CG遊戲
    LIVE(R.drawable.ic_live), //真人
    QP(R.drawable.ic_qp), //棋牌
    DZ(R.drawable.ic_dz), //電子
    BY(R.drawable.ic_by), //捕魚

    UNKNOWN(-1); //未知

    companion object {
        fun getCategory(cateCode: String?): ThirdGameCategory {
            return when(cateCode) {
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