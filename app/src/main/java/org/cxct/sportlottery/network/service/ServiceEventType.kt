package org.cxct.sportlottery.network.service

interface ServiceEventType {
    val eventType: String?
}

interface ServiceChannel {
    var channel: String?
}

enum class EventType(val value: String) {
    /*投注大廳 /notify/hall/{gameType}/{cateMenuCode}/{eventId}*/
    MATCH_STATUS_CHANGE("MATCH_STATUS_CHANGE"), //賽事比分
    MATCH_CLOCK("MATCH_CLOCK"), //賽事時間
    ODDS_CHANGE("ODDS_CHANGE"), //赔率变更
    LEAGUE_CHANGE("LEAGUE_CHANGE"), //联赛赛事状态修改
    MATCH_ODDS_LOCK("MATCH_ODDS_LOCK"), //赛事赔率上锁、解锁事件

    /*賠率細項 /notify/event/{eventId}*/
    MATCH_ODDS_CHANGE("MATCH_ODDS_CHANGE"), //赔率变更

    /*用戶 /user/{userId}*/
    USER_MONEY("USER_MONEY"), //余额变更
    USER_NOTICE("USER_NOTICE"), //消息通知
    ORDER_SETTLEMENT("ORDER_SETTLEMENT"), //注单结算通知
    USER_INFO_CHANGE("USER_INFO_CHANGE"),//用戶資訊成功
    PING_PONG("PING_PONG"), //ping-pong心跳
    LOCK_MONEY("LOCK_MONEY"),//冻结金额

    /*公共 /notify/all*/
    NOTICE("NOTICE"), //公告
    GLOBAL_STOP("GLOBAL_STOP"), //所有赔率禁用，不允许投注
    PRODUCER_UP("PRODUCER_UP"), //開啟允許投注
    SYS_MAINTENANCE("SYS_MAINTENANCE"), //系统维护状态
    PLAY_QUOTA_CHANGE("PLAY_QUOTA_CHANGE"), //所有体育玩法限额变更

    /*
    会收到此事件的频道共两个:/ws/notify/user,/ws/notify/all，
    从/ws/notify/all收到的事件discount为null
    */
    USER_DISCOUNT_CHANGE("USER_DISCOUNT_CHANGE"),

    /*公共 /ws/notify/platform/$mPlatformId */
    DATA_SOURCE_CHANGE("DATA_SOURCE_CHANGE"), //体育數據更新

    /*/ws/notify/platform*/
    USER_LEVEL_CONFIG_CHANGE("USER_LEVEL_CONFIG_CHANGE"),

    /*未被定義的頻道*/
    UNKNOWN("UNKNOWN");

    companion object {
        fun getEventType(eventType: String): EventType {
            return try {
                valueOf(eventType)
            } catch (e: Exception) {
                e.printStackTrace()
                UNKNOWN
            }
        }
    }
}