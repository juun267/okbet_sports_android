package org.cxct.sportlottery.ui.chat

/**
 * @author kevin
 * @create 2022/3/15
 * @description
 */
class LiveMsgEntity : BaseEntity() {
    var content: String? = null
    var type: String? = null // 1000:房间聊天訊息, 1001:发送红包, 1002:用户进入房间, 1003:用户离开房间, 1004:用户发送图片讯息
}
