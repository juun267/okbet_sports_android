package org.cxct.sportlottery.util
import com.google.gson.Gson
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

object JsonUtil {

    private val GSON = Gson()

    fun toJson(obj: Any): String {
        return GSON.toJson(obj)
    }

    fun <T> fromJson(jsonString: String, clazz: Class<T>): T? {
        return try {
            GSON.fromJson(jsonString, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun <T, P> fromJson(jsonString: String, clazz: Class<T>, parameterClazz: Class<P>): T? {
        return try {
            GSON.fromJson(jsonString, getType(clazz, parameterClazz))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun <T> listFrom(jsonString: String, value: Class<T>): ArrayList<T>? {
        return try {
            GSON.fromJson(jsonString, getType(ArrayList::class.java, value))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun <T> linkedListFrom(jsonString: String, value: Class<T>): LinkedList<T>? {
        return try {
            GSON.fromJson(jsonString, getType(LinkedList::class.java, value))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun <K, V> linkedMapFrom(jsonString: String, key: Class<K>, value: Class<V>): LinkedHashMap<K, V>? {
        return try {
            GSON.fromJson(jsonString, getType(LinkedHashMap::class.java, key, value))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun <T>  getType(parent: Class<T>, vararg parameter: Class<*>): Type {
        return object : ParameterizedType {
            override fun getActualTypeArguments(): Array<Type> {
                return parameter as Array<Type>
            }

            override fun getRawType(): Type {
                return parent
            }

            override fun getOwnerType(): Type? {
                return null
            }
        }
    }

}