package org.cxct.sportlottery.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.lang.reflect.ParameterizedType

object MoshiUtil {
    fun <T: Any>fromJson(json: String, clazz: Class<T>): T? {
        val moshi: Moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<T> = moshi.adapter(clazz)
        return adapter.fromJson(json)
    }

    //使用List<class>
    fun <T: Any>fromJson(json: String, parameterizedType: ParameterizedType): T? {
        val moshi: Moshi = Moshi.Builder().build()
        val adapter = moshi.adapter<T>(parameterizedType)
        return adapter.fromJson(json)
    }

}