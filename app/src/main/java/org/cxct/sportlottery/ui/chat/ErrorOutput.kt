package org.cxct.sportlottery.ui.chat

open class ErrorOutput {
    constructor()
    constructor(message: String? = null, code: Int? = null) {
        this.msg = message
        this.code = code
    }

    var success: Boolean? = null

    var msg: String? = null //若 call webAPI 錯誤，彈窗顯示 msg

    var info: String? = null

    var code: Int? = null

    var statusCode: Int? = null //20192009 紀錄 http 本身回傳的狀態代碼
}