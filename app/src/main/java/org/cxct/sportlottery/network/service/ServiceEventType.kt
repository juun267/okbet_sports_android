package org.cxct.sportlottery.network.service

interface ServiceEventType {
    val eventType: String?
}

interface ServiceChannel {
    var channel: String?
}

object EventType {
    /*投注大廳 /notify/hall/{gameType}/{cateMenuCode}/{eventId}*/
    const val MATCH_STATUS_CHANGE = ("MATCH_STATUS_CHANGE") //賽事比分
    const val MATCH_CLOCK = ("MATCH_CLOCK") //賽事時間
    const val ODDS_CHANGE = ("ODDS_CHANGE") //赔率变更
    const val LEAGUE_CHANGE = ("LEAGUE_CHANGE") //联赛赛事状态修改
    const val MATCH_ODDS_LOCK = ("MATCH_ODDS_LOCK")  //赛事赔率上锁、解锁事件

    /*賠率細項 /notify/event/{eventId}*/
    const val MATCH_ODDS_CHANGE = ("MATCH_ODDS_CHANGE")  //赔率变更

    /*用戶 /user/{userId}*/
    const val USER_MONEY = ("USER_MONEY")  //余额变更
    const val USER_NOTICE = ("USER_NOTICE")  //消息通知
    const val ORDER_SETTLEMENT = ("ORDER_SETTLEMENT")  //注单结算通知
    const val USER_INFO_CHANGE = ("USER_INFO_CHANGE") //用戶資訊成功
    const val PING_PONG = ("PING_PONG")  //ping-pong心跳
    const val LOCK_MONEY = ("LOCK_MONEY") //冻结金额

    /*公共 /notify/all*/
    const val NOTICE = ("NOTICE")  //公告
    const val GLOBAL_STOP = ("GLOBAL_STOP")  //所有赔率禁用，不允许投注
    const val PRODUCER_UP = ("PRODUCER_UP")  //開啟允許投注
    const val SYS_MAINTENANCE = ("SYS_MAINTENANCE")  //系统维护状态
    const val PLAY_QUOTA_CHANGE = ("PLAY_QUOTA_CHANGE")  //所有体育玩法限额变更
    const val SPORT_MAINTAIN_STATUS=("SPORT_MAINTAIN_STATUS") //体育维护状态   1开启  0关闭
    const val RECORD_RESULT_JACKPOT_OK_GAMES=("RECORD_RESULT_JACKPOT_OK_GAMES") //jackpot监听
    const val CASH_OUT = ("CASH_OUT")  //提前兑换注单确认 与 ORDER_SETTLEMENT事件类似，只是增加cashout相关参数
    const val CASHOUT_SWITCH = ("CASHOUT_SWITCH") //提前结算开关，针对全部体育赛事
    const val CASHOUT_MATCH_STATUS = ("CASHOUT_MATCH_STATUS") //提前结算开关，针对具体体育赛事
    const val RECORD_RESULT_JACKPOT_GAMES = "RECORD_RESULT_JACKPOT_GAMES" //提前结算开关，针对具体体育赛事

    /* /ws/notify/all/encrypted*/
    const val CLOSE_PLAY_CATE = ("CLOSE_PLAY_CATE")  //關閉玩法

    /*
    会收到此事件的频道共两个:/ws/notify/user /ws/notify/all，
    从/ws/notify/all收到的事件discount为null
    */
    const val USER_DISCOUNT_CHANGE = ("USER_DISCOUNT_CHANGE")

    /*公共 /ws/notify/platform/$mPlatformId */
    const val DATA_SOURCE_CHANGE = ("DATA_SOURCE_CHANGE")  //体育數據更新

    /*/ws/notify/platform*/
    const val USER_LEVEL_CONFIG_CHANGE = ("USER_LEVEL_CONFIG_CHANGE")

    const val THIRD_GAME_STATU_CHANGED = "GAME_FIRM_MAINTAIN_RESULT" //三方游戏维护状态变化

    /*未被定義的頻道*/
    const val UNKNOWN = ("UNKNOWN")
}