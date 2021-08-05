package org.cxct.sportlottery.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.internal.Util
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.Buffer
import okio.BufferedSource
import java.io.InputStream
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object MoshiUtil {
    fun <T: Any>fromJson(json: String, clazz: Class<T>): T? {
        val moshi: Moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<T> = moshi.adapter(clazz)
        return adapter.fromJson(json)
    }

    //使用List<class>
    fun <T : Any> fromJson(json : String, parameterizedType : ParameterizedType) : T? {
        val moshi : Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
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

    /**
     * Created by jingzz on 2020/7/13.
     */

    val moshiBuild = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    //使用Kotlin-Reflect包时，这里改一下:
    //val moshiBuild = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()


    //普通序列化
    fun <T> fromJson(json : String, type : Type) : T? = getAdapter<T>(type).fromJson(json)
    fun <T> fromJons(buffer : BufferedSource, type : Type) : T? = getAdapter<T>(type).fromJson(buffer)
    fun <T> fromJons(`is` : InputStream, type : Type) : T? = getAdapter<T>(type).fromJson(Buffer().readFrom(`is`))
    fun <T> fromJons(reader : JsonReader, type : Type) : T? = getAdapter<T>(type).fromJson(reader)

    //自动获取type序列化,性能较差
    inline fun <reified T : Any> fromJson(json : String) : T? = getAdapter<T>().fromJson(json)
    inline fun <reified T> fromJson(buffer : BufferedSource) : T? = getAdapter<T>().fromJson(buffer)
    inline fun <reified T> fromJson(`is` : InputStream) : T? = getAdapter<T>().fromJson(Buffer().readFrom(`is`))
    inline fun <reified T> fromJson(reader : JsonReader) : T? = getAdapter<T>().fromJson(reader)

    //高效序列化为list
    inline fun <reified T> listFromJson(json : String) : MutableList<T> =
            fromJson(json, Types.newParameterizedType(MutableList::class.java, T::class.java))
                    ?: mutableListOf()

    inline fun <reified T> listFromJson(buffer : BufferedSource) : MutableList<T> =
            fromJons(buffer, Types.newParameterizedType(MutableList::class.java, T::class.java))
                    ?: mutableListOf()

    inline fun <reified T> listFromJson(`is` : InputStream) : MutableList<T> =
            fromJons(`is`, Types.newParameterizedType(MutableList::class.java, T::class.java))
                    ?: mutableListOf()

    inline fun <reified T> listFromJson(reader : JsonReader) : MutableList<T> =
            fromJons(reader, Types.newParameterizedType(MutableList::class.java, T::class.java))
                    ?: mutableListOf()


    //高效序列化为map
    inline fun <reified K, reified V> mapFromJson(json : String) : MutableMap<K, V> = fromJson(
            json,
            Types.newParameterizedType(MutableMap::class.java, K::class.java, V::class.java)
    ) ?: mutableMapOf()

    inline fun <reified K, reified V> mapFromJson(buffer : BufferedSource) : MutableMap<K, V> =
            fromJons(
                    buffer,
                    Types.newParameterizedType(MutableMap::class.java, K::class.java, V::class.java)
            ) ?: mutableMapOf()

    inline fun <reified K, reified V> mapFromJson(`is` : InputStream) : MutableMap<K, V> = fromJons(
            `is`,
            Types.newParameterizedType(MutableMap::class.java, K::class.java, V::class.java)
    ) ?: mutableMapOf()

    inline fun <reified K, reified V> mapFromJson(reader : JsonReader) : MutableMap<K, V> = fromJons(
            reader,
            Types.newParameterizedType(MutableMap::class.java, K::class.java, V::class.java)
    ) ?: mutableMapOf()

    //反序列化
    inline fun <reified T> toJson(t : T) = getAdapter<T>().toJson(t) ?: ""

    fun <T> getAdapter(type : Type) : JsonAdapter<T> = moshiBuild.adapter(type)
    inline fun <reified T> getAdapter() : JsonAdapter<T> = moshiBuild.adapter(object : TypeToken<T>() {}.type)

}

abstract class TypeToken<T> {
    val type : Type
        get() = run {
            val superclass = javaClass.genericSuperclass
            Util.canonicalize((superclass as ParameterizedType).actualTypeArguments[0])
        }
}


//快捷序列化
inline fun <reified T : Any> String.fromJson() = MoshiUtil.fromJson<T>(this)

//快捷反序列化
fun Any.toJson() = MoshiUtil.toJson(this)
