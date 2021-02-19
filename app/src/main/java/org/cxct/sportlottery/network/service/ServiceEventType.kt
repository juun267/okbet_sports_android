package org.cxct.sportlottery.network.service

interface ServiceEventType {
    val eventType: String
}

enum class EventType(val value: String) {
    /*投注大廳 /notify/hall/{gameType}/{cateMenuCode}/{eventId}*/
    MATCH_STATUS_CHANGE("MATCH_STATUS_CHANGE"), //賽事比分
    MATCH_CLOCK("MATCH_CLOCK"), //賽事時間
    ODDS_CHANGE("ODDS_CHANGE"), //赔率变更

    /*賠率細項 /notify/event/{eventId}*/
    MATCH_ODDS_CHANGE("MATCH_ODDS_CHANGE"), //赔率变更

    /*用戶 /user/{userId}*/
    USER_MONEY("USER_MONEY"), //余额变更
    USER_NOTICE("USER_NOTICE"), //消息通知
    ORDER_SETTLEMENT("ORDER_SETTLEMENT"), //注单结算通知
    PING_PONG("PING_PONG"), //ping-pong心跳

    /*公共 /notify/all*/
    NOTICE("NOTICE"), //公告
    GLOBAL_STOP("GLOBAL_STOP"), //所有赔率禁用，不允许投注
    PRODUCER_UP("PRODUCER_UP"), //開啟允許投注
    SYS_MAINTENANCE("SYS_MAINTENANCE"), //系统维护状态
}