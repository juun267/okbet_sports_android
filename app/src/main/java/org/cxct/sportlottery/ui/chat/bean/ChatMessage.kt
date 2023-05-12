package org.cxct.sportlottery.ui.chat.bean

import com.chad.library.adapter.base.entity.MultiItemEntity

abstract class ChatMessage: MultiItemEntity {

    var msg: String? = null
    var seq: Int = -1//信息编号,client发送讯息id,如果为server主动推送，则为0
    var time: Long = 0
    var type: Int = -1

    fun apply(msg: String?, seq: Int, time: Long, type: Int) {
        this.msg = msg
        this.seq = seq
        this.time = time
        this.type = type
    }
}
