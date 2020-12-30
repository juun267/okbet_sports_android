package org.cxct.sportlottery.ui.menu.results

enum class StatusType(val code: Int) {
    FIRST_HALF(6), //上半場
    SECOND_HALF(7), //下半場
    FIRST_SECTION(13), //第一節
    SECOND_SECTION(14), //第二節
    THIRD_SECTION(15), //第三節
    FOURTH_SECTION(16), //第四節
    OVER_TIME(110), //加時
    FIRST_PLAT(8), //第一盤
    SECOND_PLAT(9), //第二盤
    THIRD_PLAT(10), //第三盤
    FOURTH_PLAT(11), //第四盤
    FIFTH_PLAT(12), //第五盤
    END_GAME(100), //完賽(賽果)
}