package org.cxct.sportlottery.ui.chat


class ChatMessage {

    constructor()

    constructor(chatType: Int, content: String?) {
        this.chatType = chatType
        this.content = content
    }

    var id: Int? = null //消息id
    var fk: String? = null //加密信息，通过 id + '|' + userName + salt 加密生成
    var chatType: Int? =
        null //1:訊息、圖片、語音, 2:进入房间, 3:离开房间, 4:禁言, 5:解除禁言, 6:跟注, 7:計劃管理員消息, 8:聊天時段, 9:系統消息撤銷, 10: 红包消息, 11:抢到红包消息, 12:计划消息, 13:聊天室關閉, 14:跟注结果, 20:用户投注记录
    var nickName: String? = null //昵称
    var content: String? = null //消息内容
    var curTime: String? = null //当前时间
    var roleId: Int? = null //角色id
    var iconUrl: String? = null //头像
    var chatUserId: Long? = null //20200714 私聊功能添加參數
    var betRecordContent: BetRecordContent? = null //投注记录明细 20200731 群聊功能添加參數

    class BetRecordContent {
        //20200731 群聊功能添加參數
        var gameId: Int? = null //游戏id
        var platCode: String? = null //平台代码
        var totalBet: Int? = null //總注數
        var totalBetMoney: Double? = null //注单总金额
        var turnNum: String? = null //期号
        var playInfoList = mutableListOf<PlayInfo>()

        class PlayInfo {
            var playCateId: Int? = null //玩法分类id
            var playIdList: Array<Int>? = null //玩法类底下之玩法id阵列
        }
    }

    //=== APP local 端自行添加的參數資料 ===//
    var role: ChatConfigOutput.Role? = null
    var levelPic: Int? = null //會員等級圖示
    var packet: RedPack? = null //添加紀錄 紅包類 訊息，供聊天室點擊紅包開啟用
    var isFollowed: Boolean = false //判斷是否跟注過(用在跟注類訊息)
    var isRewarded: Boolean = false //判斷是否打賞過(用在打賞類訊息)
    var isOutOfDate: Boolean = true // 跟注時間是否過期
    var likeCount: Int? = null //未點過讚=null，有點過讚才能設定值

    //=== APP local 端自行添加的參數資料 ===//
    var isExpand: Boolean = true //true:資料展開 false:資料收起來 給跟投機器人使用
}