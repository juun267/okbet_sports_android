package org.cxct.sportlottery.ui.main.entity

enum class ThirdGameCategory(var title: String? = null) {
    CGCP, //CG遊戲
    BY, //捕魚
    QP, //棋牌
    LIVE, //真人
    DZ, //電子
    SP, //體育
    DJ, //電競

    UNKNOWN; //未知

    companion object {
        fun getCategory(cateCode: String?): ThirdGameCategory {
            return when(cateCode) {
                CGCP.name -> CGCP
                BY.name -> BY
                QP.name -> QP
                LIVE.name -> LIVE
                DZ.name -> DZ
                SP.name -> SP
                DJ.name -> DJ
                else -> UNKNOWN
            }
        }
    }
}