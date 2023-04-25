package org.cxct.sportlottery.ui.chat

import com.google.gson.Gson

open class BaseEntity {

    fun toJSONString(): String {
        return Gson().toJson(this)
    }

}