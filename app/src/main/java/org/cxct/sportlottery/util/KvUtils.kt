package org.cxct.sportlottery.util

import android.os.Parcelable
import android.text.TextUtils
import com.tencent.mmkv.MMKV
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.application.MultiLanguagesApplication

object KvUtils {


    //是否处于市场审核中,上架市场，隐藏功能的开关
    const val MARKET_SWITCH = "marketSwitch"
    const val KEY_ENDCARD_GUIDE = "endcardGuide"
    const val KEY_ENDCARD_CLEAR = "EndCardClear"
    const val KEY_ENDCARD_DELETE_GUIDE = "endcardDeleteGuide"
    const val KEY_NOTIFICATION_PERMISSION = "notificationPermission"
    const val REMIND_KYC_DATE = "remindKYCDate"

    private val mv: MMKV by lazy {
        MMKV.initialize(MultiLanguagesApplication.appContext)
        if (BuildConfig.DEBUG) {
            MMKV.defaultMMKV()
        } else {
            MMKV.mmkvWithID(BuildConfig.APPLICATION_ID)
        }
    }

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param key
     * @param object
     */
    fun put(key: String, obj: Any) {
        when (obj) {
            is String -> {
                mv.encode(key, obj)
            }

            is Int -> {
                mv.encode(key, obj)
            }

            is Boolean -> {
                mv.encode(key, obj)
            }

            is Float -> {
                mv.encode(key, obj)
            }

            is Long -> {
                mv.encode(key, obj)
            }

            is Double -> {
                mv.encode(key, obj)
            }

            is ByteArray -> {
                mv.encode(key, obj)
            }

            else -> {
                mv.encode(key, obj.toString())
            }
        }
    }


    fun putObject(obj: Any) {
        putObject(obj.javaClass.name, JsonUtil.toJson(obj))
    }

    fun putObject(key: String, obj: Any?) {
        if (obj == null) {
            mv.removeValueForKey(key)
        } else if (obj is String) {
            mv.encode(key, obj)
        } else {
            mv.encode(key, JsonUtil.toJson(obj))
        }
    }

     fun <T> getObject(clazz: Class<T>): T? {
        return getObject(clazz.name, clazz)
    }

    fun <T> getList(key: String, clazz: Class<T>): ArrayList<T>? {
        val jsonString = mv.decodeString(key, null)
        if (TextUtils.isEmpty(jsonString)) {
            return null
        }
        return JsonUtil.listFrom(jsonString!!, clazz)
    }

    fun <T> getObject(key: String, clazz: Class<T>): T? {
        val jsonString = mv.decodeString(key, null)
        if (TextUtils.isEmpty(jsonString)) {
            return null
        }
        return JsonUtil.fromJson(jsonString!!, clazz)
    }

    fun encodeSet(key: String?, sets: Set<String?>?) {
        mv.encode(key, sets)
    }

    fun encodeParcelable(key: String?, obj: Parcelable?) {
        mv.encode(key, obj)
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     */
    fun decodeInt(key: String?, defaultValue: Int = 0): Int {
        return mv.decodeInt(key, defaultValue)
    }

    fun decodeDouble(key: String?): Double {
        return mv.decodeDouble(key, 0.00)!!
    }

    fun decodeLong(key: String?): Long {
        return mv.decodeLong(key, 0L)!!
    }

    fun decodeBoolean(key: String?): Boolean {
        return mv.decodeBool(key, false)
    }

    fun decodeBooleanTure(key: String?,defaultValue: Boolean): Boolean {
        return mv.decodeBool(key, defaultValue) == true
    }

    fun decodeFloat(key: String?): Float {
        return mv.decodeFloat(key, 0f)!!
    }

    fun decodeBytes(key: String?): ByteArray {
        return mv.decodeBytes(key)!!
    }

    fun decodeString(key: String?): String {
        return mv.decodeString(key, "").toString()
    }
    fun decodeStringDef(key: String?,defaultValue: String): String {
        return mv.decodeString(key, defaultValue).toString()
    }

    operator fun contains(key: String?): Boolean {
        return mv.containsKey(key)
    }


    fun decodeStringSet(key: String?): LinkedHashSet<String> {
        return mv.decodeStringSet(key, LinkedHashSet(), LinkedHashSet::class.java) as LinkedHashSet<String>
    }

    fun <T : Parcelable?> decodeParcelable(key: String?, tClass: Class<T>?): T? {
        return mv.decodeParcelable(key, tClass)
    }


    fun removeObject(objClass: Class<*>) {
        removeKey(objClass.name)
    }


    /**
     * 移除某个key对
     *
     * @param key
     */
    fun removeKey(key: String?) {
        mv.removeValueForKey(key)
    }

    /**
     * 清除所有key
     */
    fun clearAll() {
        mv.clearAll()
    }

//    /**
//     * 从sp中迁移到mmvk
//     */
//    fun testImportSharedPreferences(context: Context){
//        val oldMan = context.getSharedPreferences("yxsSp", Context.MODE_PRIVATE)
//        // 迁移旧数据
//        mv?.importFromSharedPreferences(oldMan)
//        // 清空旧数据
//        oldMan.edit().clear().commit()
//    }




}