package org.cxct.sportlottery.network.service

interface ServiceEventType {
    val eventType: String
}

enum class EventType(val value: String) {
    ODDS_CHANGE("ODDS_CHANGE"), //赔率变更
    ORDER_SETTLEMENT("ORDER_SETTLEMENT"), //注单结算通知
    USER_MONEY("USER_MONEY"), //余额变更
    USER_NOTICE("USER_NOTICE"), //用户消息通知
    NOTICE("NOTICE"), //公告
    PING_PONG("PING_PONG"), //ping-pong心跳
    MATCH_CLOCK("MATCH_CLOCK"), //赛事时刻
    GLOBAL_STOP("GLOBAL_STOP"), //所有赔率禁用，不允许投注
    MATCH_STATUS_CHANGE("MATCH_STATUS_CHANGE"), //賽事比分
}
/**
    投注大廳 /notify/hall/{gameType}/{cateMenuCode}/{eventId}：
    - MATCH_STATUS_CHANGE   賽事比分
    - MATCH_CLOCK           賽事時間
    - ODDS_CHANGE           賠率變更

    賠率細項 /notify/event/{eventId}：
    - MATCH_STATUS_CHANGE   賽事比分 ( 沒用，賠率詳細頁不需要 )
    - MATCH_CLOCK           賽事時間 ( 沒用，賠率詳細頁不需要 )
    - MATCH_ODDS_CHANGE     賠率變更

    用戶 /user/{userId}：
    - USER_MONEY            餘額變更
    - USER_NOTICE           訊息通知
    - ORDER_SETTLEMENT      注單結算通知
    - PING_PONG             心跳

    公共 /notify/all：
    - NOTICE                公告
    - GLOBAL_STOP           所有赔率禁用，不允许投注
 */
