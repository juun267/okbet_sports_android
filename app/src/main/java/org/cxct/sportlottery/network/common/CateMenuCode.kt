package org.cxct.sportlottery.network.common

enum class CateMenuCode(val code: String) {
    HDP("HDP"), //讓球
    OU("OU"), //大小
    ONEXTWO("1X2"), //獨贏
    HDP_AND_OU("HDP&OU"), //讓球 & 大小
    OUTRIGHT("OUTRIGHT") //冠軍
}

//首頁賽事訂閱 menuCode
enum class MenuCode(val code: String) {
    RECOMMEND("RECOMMEND"), //推荐赛事
    SPECIAL_MATCH("SPECIAL_MATCH"), //精选赛事
    SPECIAL_MATCH_MOBILE("SPECIAL_MATCH_MOBILE"), //精选赛事-移动端
    HOME_INPLAY("HOME_INPLAY"), //滚球赛事
    HOME_INPLAY_MOBILE("HOME_INPLAY_MOBILE"), //滚球赛事-移动端
    HOME_ATSTART("HOME_ATSTART"), //即将开赛
    HOME_ATSTART_MOBILE("HOME_ATSTART_MOBILE") //即将开赛-移动端
}