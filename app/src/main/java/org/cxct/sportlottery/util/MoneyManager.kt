package org.cxct.sportlottery.util

import android.content.Context
import android.content.res.AssetManager
import com.google.gson.Gson
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.MoneyRechCfg
import org.cxct.sportlottery.network.money.MoneyRechCfgData
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object MoneyManager {
    private val mContext = MultiLanguagesApplication.appContext

    /**
     * 獲取 充值配置 RechargeConfig.json 資料
     * @return :
     */
    private fun getRechargeConfig(context: Context): String {
        val stringBuilder = StringBuilder()
        val path = "RechargeConfig.json"
        try {
            //获取assets资源管理器
            val assetManager: AssetManager = context.assets

            //通过管理器打开文件并读取
            val bf = BufferedReader(InputStreamReader(assetManager.open(path)))
            var line = bf.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = bf.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }
    //獲取 local 端 充值配置 清單
    private var mMoneyPayWayList: List<MoneyPayWayData>? = null
    fun getMoneyPayWayList(): List<MoneyPayWayData>? {
        if (mMoneyPayWayList == null) {
            mMoneyPayWayList = listOf(Gson().fromJson(getRechargeConfig(mContext), MoneyPayWayData::class.java))
        }
        return mMoneyPayWayList
    }

    //獲取最大充值金額，沒有值就取 default local value
    fun getMaxMoney(rechConfig: MoneyRechCfg.RechConfig): Double {
        return rechConfig?.maxMoney ?: 9999999.0
    }

    //獲取最小充值金額，沒有值就取 config.json value
//    fun getMinMoney(rechConfig: MoneyRechCfg.RechConfig?): Double { //TODO Bill 等AppConfigManager做好去裡面撈最小充值金額(每個使用者不一樣)
//        return rechConfig?.minMoney ?: AppConfigManager.getAppConfig().minRechMoney!!.toDouble()
//    }
}