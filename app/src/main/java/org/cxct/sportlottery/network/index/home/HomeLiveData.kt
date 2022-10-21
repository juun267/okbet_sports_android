package org.cxct.sportlottery.network.index.home

data class HomeLiveData (
    val matchName: String?, //联赛名称 意大利甲级联赛
    val matchType: String?, //赛事类型 //篮球赛事，足球赛事
    val half: String, //上下半场时间
    val imageType: Int,//篮球足球的海报头像
    val starPlayer: Int,//明星球员的头像
    val starTitle: String,//明星球员的介绍
    val homeTeamIcon: String,//主队图标
    val awayTeamIcon: String,//客队图标
    val homeTeamName: String,//主队名字
    val awayTeamName: String,//客队名字
    val homeTeamNum: String,//主队比分
    val awayTeamNum: String,//客队比分
    )
