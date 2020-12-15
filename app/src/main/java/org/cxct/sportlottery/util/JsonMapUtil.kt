package org.cxct.sportlottery.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.jvm.Throws

object JsonMapUtil {

    @Throws(JSONException::class)
    fun jsonToMap(json: JSONObject): Map<String?, Any?> {
        var retMap: Map<String?, Any?> =
            HashMap()
        if (json !== JSONObject.NULL) {
            retMap = toMap(json)
        }
        return retMap
    }


    @Throws(JSONException::class)
    fun toMap(obj: JSONObject): Map<String?, Any?> {
        val map: MutableMap<String?, Any?> =
            HashMap()
        val keysItr = obj.keys()
        while (keysItr.hasNext()) {
            val key = keysItr.next()
            var value = obj[key]
            if (value is JSONArray) {
                value = toList(value)
            } else if (value is JSONObject) {
                value = toMap(value)
            }
            map[key] = value
        }
        return map
    }


    @Throws(JSONException::class)
    fun toList(array: JSONArray): List<Any> {
        val list: MutableList<Any> = ArrayList()
        for (i in 0 until array.length()) {
            var value = array[i]
            if (value is JSONArray) {
                value = toList(value)
            } else if (value is JSONObject) {
                value = toMap(value)
            }
            list.add(value)
        }
        return list
    }

}