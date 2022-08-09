package org.cxct.sportlottery.util

import android.content.Context
import android.content.res.AssetManager
import androidx.annotation.DrawableRes
import com.squareup.moshi.Types
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MoneyType
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.config.TransferType
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
            mMoneyPayWayList = getRechargeConfig(mContext).fromJson<List<MoneyPayWayData>>()
        }
        return mMoneyPayWayList
    }

    fun getBankIcon(bankName: String): Int {
        return when (bankName) {
            MoneyType.BANK.code -> R.drawable.ic_bank_atm
            MoneyType.ALI.code -> R.drawable.ic_alipay
            MoneyType.WX.code -> R.drawable.ic_wechat_pay
            MoneyType.CTF.code -> R.drawable.ic_tenpay
            MoneyType.ONLINE.code -> R.drawable.ic_online_pay
            MoneyType.CRYPTO.code -> R.drawable.ic_crypto_pay
            MoneyType.JUAN_ONLINE_TYPE.code -> R.drawable.ic_juancash
            MoneyType.DISPENSHIN.code -> R.drawable.ic_juancash//202 Peter說要隱藏掉
            MoneyType.ONLINEBANK.code -> R.drawable.ic_bank_default//阿喵說照Ian回應用此圖
            MoneyType.GCASH.code -> R.drawable.ic_g_cash
            MoneyType.GRABPAY.code -> R.drawable.ic_grab_pay
            MoneyType.PAYMAYA.code -> R.drawable.ic_pay_maya
            MoneyType.PAYPAL.code -> R.drawable.ic_paypal
            MoneyType.DRAGONPAY.code -> R.drawable.ic_gragon_pay
            MoneyType.MOMOPAY.code -> R.drawable.ic_momopay
            MoneyType.ZALOPAY.code -> R.drawable.ic_zalopay
            MoneyType.VIETTELPAY.code -> R.drawable.ic_viettelpay
            MoneyType.RECHARGE_CARD.code -> R.drawable.ic_recharge_card
            else -> R.drawable.ic_bank_atm
        }
    }

    fun getBankAccountIcon(rechType: String): Int {
        return when (rechType) {
            MoneyType.ALI_TYPE.code -> R.drawable.ic_alipay_type
            MoneyType.WX_TYPE.code -> R.drawable.ic_wechat_pay_type
            MoneyType.CTF_TYPE.code -> R.drawable.ic_tenpay_type
            MoneyType.ONLINE_TYPE.code -> R.drawable.ic_online_pay_type
            MoneyType.GCASH_TYPE.code -> R.drawable.ic_g_cash_type
            MoneyType.GRABPAY_TYPE.code -> R.drawable.ic_grab_pay_type
            MoneyType.PAYMAYA_TYPE.code -> R.drawable.ic_pay_maya_type
            MoneyType.CRYPTO.code -> R.drawable.ic_crypto_pay
            MoneyType.JUAN_ONLINE_TYPE.code -> R.drawable.ic_juancash_type
            MoneyType.DISPENSHIN.code -> R.drawable.ic_juancash_type//202 Peter說要隱藏掉
            MoneyType.ONLINEBANK.code -> R.drawable.ic_bank_default//阿喵說照Ian回應用此圖
            MoneyType.GCASH.code -> R.drawable.ic_g_cash_type
            MoneyType.GRABPAY.code -> R.drawable.ic_grab_pay_type
            MoneyType.PAYMAYA.code -> R.drawable.ic_pay_maya_type
            MoneyType.PAYPAL_TYPE.code -> R.drawable.ic_paypal_type
            else -> R.drawable.ic_bank_atm
        }
    }

    fun getBankIconByBankName(bankName: String): Int {
        BankKey.values().map { if (it.bankName.equals(bankName, ignoreCase = true)) return it.iconId }
        return R.drawable.ic_bank_default
    }

    fun getCryptoIconByCryptoName(cryptoName: String): Int {
        CryptoIcon.values().map { if (it.cryptoName == cryptoName) return it.iconId }
        return R.drawable.ic_crypto
    }

    enum class CryptoIcon(val cryptoName: String, @DrawableRes val iconId: Int) {
        CRYPTO(TransferType.CRYPTO.type, R.drawable.ic_crypto)
    }

    enum class BankKey(val bankName: String, @DrawableRes val iconId: Int) {
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
        BOS("上海银行", R.drawable.ic_bank_shcc),
        BPI("BPI",  R.drawable.ic_bank_bpi),
        BDO("BDO",  R.drawable.ic_bank_bdo),
        METROPOLITAN("metropolitan",  R.drawable.ic_bank_metropolitan),
        LAND("land",  R.drawable.ic_bank_land),
        SECURITY("security",  R.drawable.ic_bank_security),
        UB("UB",  R.drawable.ic_bank_ub),
        UCPB("UCPB",  R.drawable.ic_bank_ucpb),
        RCBC("RCBC",  R.drawable.ic_bank_rcbc),
        EASTWEST("Eastwest",  R.drawable.ic_bank_eastwest),
        CHINABANK("ChinaBank",  R.drawable.ic_bank_china_bank),
        PNB("PNB",  R.drawable.ic_bank_pnb),
        GCASH("Gcash", R.drawable.ic_g_cash_type),
        PAYMAYA("Paymaya", R.drawable.ic_pay_maya_type),
        JUANCASH("JuanCash", R.drawable.ic_juancash),
        GRABPAY("Grabpay", R.drawable.ic_grab_pay_type),
        ALIPAY("Alipay", R.drawable.ic_alipay_type)
    }
}