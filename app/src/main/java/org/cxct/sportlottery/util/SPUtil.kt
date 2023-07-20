package org.cxct.sportlottery.util

import android.content.Context
import android.content.SharedPreferences
import java.util.*

@Deprecated("请使用KVUtils替代")
object SPUtil {
    private const val SP_NAME = "language_setting"
    private const val TAG_LANGUAGE = "language_select"

    //上架市场，隐藏功能的开关
    const val MARKET_SWITCH = "market_switch"
    private var mSharedPreferences: SharedPreferences? = null
    var systemCurrentLocal: Locale = Locale.ENGLISH

    fun getInstance(context: Context?): SPUtil {
        if (mSharedPreferences == null)
            mSharedPreferences = context?.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        return this
    }

    fun saveLanguage(select: String?) {
        mSharedPreferences?.edit()
            ?.putString(TAG_LANGUAGE, select)
            ?.apply()
    }

    fun getSelectLanguage(): String? {
        return mSharedPreferences?.getString(TAG_LANGUAGE, null)
    }

    fun saveString(key: String, value: String?) {
        mSharedPreferences?.edit()
            ?.putString(key, value)
            ?.apply()
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return mSharedPreferences?.getString(key, defaultValue)
    }

    fun saveMarketSwitch(value: Boolean) {
        mSharedPreferences?.edit()
            ?.putBoolean(MARKET_SWITCH, value)
            ?.apply()
    }

    fun getMarketSwitch(): Boolean {
        return mSharedPreferences?.getBoolean(MARKET_SWITCH, false) == true
    }

    fun saveBoolean(key: String, value: Boolean) {
        mSharedPreferences?.edit()
            ?.putBoolean(key, value)
            ?.apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean? {
        return mSharedPreferences?.getBoolean(key, defaultValue)
    }


    fun saveLoginInfoSwitch(){
        saveBoolean("login_switch",true)
    }

    fun getLoginInfoSwitch():Boolean{
        val newSwitch=getBoolean("new_login_switch")
        if(newSwitch!=null){
            if(newSwitch){
                val switch=getBoolean("login_switch")
                if(switch!=null){
                    return switch
                }
                return false
            }else{
                saveBoolean("login_switch",false)
                saveBoolean("new_login_switch",true)
                return false
            }
        }else{
            return false
        }
    }


}