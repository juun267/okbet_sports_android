package org.cxct.sportlottery.util

import android.content.Context
import android.content.res.AssetManager
import com.squareup.moshi.Types
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MoneyType
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.MoneyRechCfg
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
        val path = "MoneyConfigs/RechargeConfig.json"
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
            mMoneyPayWayList = MoshiUtil.fromJson(
                getRechargeConfig(mContext),
                Types.newParameterizedType(List::class.java, MoneyPayWayData::class.java)
            )
        }
        return mMoneyPayWayList
    }

    //獲取最大充值金額，沒有值就取 default local value
    fun getMaxMoney(rechConfig: MoneyRechCfg.RechConfig): Double {
        return rechConfig.maxMoney ?: 9999999.0
    }

    //獲取最小充值金額，沒有值就取 config.json value
//    fun getMinMoney(rechConfig: MoneyRechCfg.RechConfig?): Double { //TODO Bill 等AppConfigManager做好去裡面撈最小充值金額(每個使用者不一樣)
//        return rechConfig?.minMoney ?: AppConfigManager.getAppConfig().minRechMoney!!.toDouble()
//    }


    fun getBankIcon(bankName: String): Int {
        return when (bankName) {
            MoneyType.BANK.code -> R.drawable.ic_bank_atm
            MoneyType.ALI.code -> R.drawable.ic_alipay
            MoneyType.WX.code -> R.drawable.ic_wechat_pay
            MoneyType.CTF.code -> R.drawable.ic_tenpay
            MoneyType.ONLINE.code -> R.drawable.ic_online_pay
            else -> R.drawable.ic_bank_atm
        }
    }

    fun getBankIconByBankName(bankName: String): Int {
        BankKey.values().map { if (it.bankName == bankName) return it.iconId }
        return R.drawable.ic_bank_default
    }

    enum class BankKey(val bankName: String, val iconId: Int) {
        ABC("农业银行", R.drawable.ic_bank_abc),
        CCB("建设银行", R.drawable.ic_bank_ccb),
        ICBC("工商银行", R.drawable.ic_bank_icbc),
        CMB("招商银行", R.drawable.ic_bank_cmb),
        BOCO("交通银行", R.drawable.ic_bank_boco),
        CMBC("民生银行", R.drawable.ic_bank_cmbc),
        CIB("兴业银行", R.drawable.ic_bank_cib),
        BOC("中国银行", R.drawable.ic_bank_boc),
        POST("邮政银行", R.drawable.ic_bank_psbc),
        CEBBANK("光大银行", R.drawable.ic_bank_ceb),
        ECITIC("中信银行", R.drawable.ic_bank_ecit),
        CGB("广发银行", R.drawable.ic_bank_cgb),
        SPDB("浦发银行", R.drawable.ic_bank_spbd),
        HXB("华夏银行", R.drawable.ic_bank_hxb),
        PINGAN("平安银行", R.drawable.ic_bank_pingan),
        BCCB("北京银行", R.drawable.ic_bank_bccb),
        BRCB("北京农商", R.drawable.ic_bank_brcb),
        BOS("上海银行", R.drawable.ic_bank_shcc)
    }
}