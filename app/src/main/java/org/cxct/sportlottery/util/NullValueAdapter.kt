package org.cxct.sportlottery.util

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader

class NullValueAdapter {

    @FromJson
    open fun fromJsonString(reader: JsonReader): String {
        if (reader.peek() != JsonReader.Token.NULL) {
            return reader.nextString()
        }
        reader.nextNull<Unit>()
        return ""
    }

    @FromJson
    fun fromJsonInt(reader: JsonReader): Int {
        if (reader.peek() != JsonReader.Token.NULL) {
            return reader.nextInt()
        }
        reader.nextNull<Unit>()
        return 0
    }

}