package org.cxct.sportlottery.network.common

enum class CateMenuCode(val code: String) {
    HDP("HDP"), //讓球
    OU("OU"), //大小
    ONEXTWO("1X2"), //獨贏
    HDP_AND_OU("HDP&OU"), //讓球 & 大小
    OUTRIGHT("OUTRIGHT") //冠軍
}