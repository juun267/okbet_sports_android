package org.cxct.sportlottery.network.service.order_settlement

enum class Status(val code: Int) {
    UN_CHECK(0), //未确认
    UN_DONE(1), //未结算
    WIN(2), //赢
    WIN_HALF(3),//赢半
    LOSE(4), //输
    LOSE_HALF(5), //输半
    DRAW(6), //和
    CANCEL(7)//已取消
}