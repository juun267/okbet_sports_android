package org.cxct.sportlottery.ui.maintab

enum class HomeTabType(val code: String) {

    //TODO 遗留 首页自定义的type字段 如果后续后台做修改会改变 目前前段去写死
    RECOMMEND("recommend"),//推荐
    LIVESTREAMING("liveStreaming"),//直播
    SPORTS("sports"),//体育
    WORDCUP("worldCup"),//世界杯
    CHESSCARDS("chessCards"),//棋牌
    ELECTRONICGAME("electronicGames")//电子
}