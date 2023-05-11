package org.cxct.sportlottery.ui.chat

import org.cxct.sportlottery.util.JsonUtil

//聊天室消息发送的实体对象
open class BaseEntity {

    fun toJSONString(): String {
        return JsonUtil.toJson(this)
    }

}