package org.cxct.sportlottery.util

import android.content.Context
import org.cxct.sportlottery.application.MultiLanguagesApplication

@Deprecated("请使用KVUtils替代")
object SPUtil {
    private const val SP_NAME = "language_setting"


    //上架市场，隐藏功能的开关
    private const val MARKET_SWITCH = "market_switch"
    private val mSharedPreferences by lazy { MultiLanguagesApplication.mInstance.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE) }

    fun getString(key: String, defaultValue: String? = null): String? {
        return mSharedPreferences.getString(key, defaultValue)
    }

    fun getMarketSwitch(): Boolean {
        return mSharedPreferences.getBoolean(MARKET_SWITCH, false)
    }

    fun saveBoolean(key: String, value: Boolean) {
        mSharedPreferences.edit()
            .putBoolean(key, value)
            .apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return mSharedPreferences.getBoolean(key, defaultValue)
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