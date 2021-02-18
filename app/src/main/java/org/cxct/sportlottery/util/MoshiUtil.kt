package org.cxct.sportlottery.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.ParameterizedType

object MoshiUtil {
    fun <T: Any>fromJson(json: String, clazz: Class<T>): T? {
        val moshi: Moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<T> = moshi.adapter(clazz)
        return adapter.fromJson(json)
    }

    //使用List<class>
    fun <T: Any>fromJson(json: String, parameterizedType: ParameterizedType): T? {
        val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter<T>(parameterizedType)
        return adapter.fromJson(json)
    }

    fun <T:Any>toJson(jsonObject: T,clazz: Class<T>):String{

        val userRebatesType =
            Types.newParameterizedType(List::class.java, clazz)
        val moshi: Moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<T> = moshi.adapter(userRebatesType)
        return adapter.toJson(jsonObject)
    }
}