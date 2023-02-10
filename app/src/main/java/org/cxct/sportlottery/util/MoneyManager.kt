package org.cxct.sportlottery.util

import android.content.Context
import android.content.res.AssetManager
import androidx.annotation.DrawableRes
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MoneyType
import org.cxct.sportlottery.network.money.MoneyPayWayData
import org.cxct.sportlottery.network.money.OnlineType
import org.cxct.sportlottery.network.money.config.TransferType
import org.cxct.sportlottery.ui.finance.df.RechType
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
        var bf: BufferedReader? = null
        try {
            //获取assets资源管理器
            val assetManager: AssetManager = context.assets

            //通过管理器打开文件并读取
            bf = BufferedReader(InputStreamReader(assetManager.open(path)))
            var line = bf.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = bf.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            bf?.close()
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
            MoneyType.ONLINEBANK.code -> R.drawable.ic_online_banking//阿喵說照Ian回應用此圖
            MoneyType.GCASH.code -> R.drawable.ic_g_cash
            MoneyType.GRABPAY.code -> R.drawable.ic_grab_pay
            MoneyType.PAYMAYA.code -> R.drawable.ic_pay_maya
            MoneyType.PAYPAL.code -> R.drawable.ic_paypal
            MoneyType.DRAGONPAY.code -> R.drawable.ic_gragon_pay
            MoneyType.MOMOPAY.code -> R.drawable.ic_momopay
            MoneyType.ZALOPAY.code -> R.drawable.ic_zalopay
            MoneyType.VIETTELPAY.code -> R.drawable.ic_viettelpay
            MoneyType.RECHARGE_CARD.code -> R.drawable.ic_recharge_card
            MoneyType.QQONLINE.code -> R.drawable.ic_qq_online
            MoneyType.FORTUNE_PAY.code -> R.drawable.ic_fortunepay
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
            MoneyType.ONLINEBANK.code -> R.drawable.ic_online_banking//阿喵說照Ian回應用此圖
            MoneyType.GCASH.code -> R.drawable.ic_g_cash_type
            MoneyType.GRABPAY.code -> R.drawable.ic_grab_pay_type
            MoneyType.PAYMAYA.code -> R.drawable.ic_pay_maya_type
            MoneyType.PAYPAL_TYPE.code -> R.drawable.ic_paypal_type
            MoneyType.FORTUNE_PAY.code -> R.drawable.ic_fortunepay
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
        METROBANK("metrobank",  R.drawable.ic_bank_metropolitan),
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
        JUANCASH("JuanCash", R.drawable.ic_juancash_type),
        GRABPAY("Grabpay", R.drawable.ic_grab_pay_type),
        ALIPAY("Alipay", R.drawable.ic_alipay_type),
    }

    fun getMoneyBankTypeTitle(rechType:String):String{
       return when (rechType) {
            RechType.ONLINE_PAYMENT.type -> mContext.getString(R.string.recharge_channel_online)
            RechType.ADMIN_ADD_MONEY.type -> mContext.getString(R.string.recharge_channel_admin)
            RechType.CFT.type -> mContext.getString(R.string.recharge_channel_cft)
            RechType.WEIXIN.type -> mContext.getString(R.string.recharge_channel_weixin)
            RechType.ALIPAY.type -> mContext.getString(R.string.recharge_channel_alipay)
            RechType.BANK_TRANSFER.type -> mContext.getString(R.string.recharge_channel_bank)
            RechType.CRYPTO.type -> mContext.getString(R.string.recharge_channel_crypto)
            RechType.GCASH.type -> mContext.getString(R.string.recharge_channel_gcash)
            RechType.GRABPAY.type -> mContext.getString(R.string.recharge_channel_grabpay)
            RechType.PAYMAYA.type -> mContext.getString(R.string.recharge_channel_paymaya)
            RechType.BETTING_STATION.type -> mContext.getString(R.string.betting_station_deposit)
            else -> ""
        }
    }

    //在線充值帳戶選單名稱
    fun getOnlinePayTypeName(onlineType: Int?): String {
        return when (onlineType) {//在线充值类型：1-网银在线充值、2-支付宝在线充值、3-微信在线充值、4-qq在线充值、5-出款、6、信用卡在线充值、7-百度钱包、8-京东钱包
            OnlineType.WY.type -> mContext.resources.getString(R.string.online_bank)
            OnlineType.ZFB.type -> mContext.resources.getString(R.string.online_alipay)
            OnlineType.WX.type -> mContext.resources.getString(R.string.online_weixin)
            OnlineType.QQ.type -> mContext.resources.getString(R.string.online_qq)
            OnlineType.XYK.type -> mContext.resources.getString(R.string.online_credit_card)
            OnlineType.JUAN.type -> mContext.resources.getString(R.string.online_juan)
            OnlineType.DISPENSHIN.type -> mContext.resources.getString(R.string.online_dispenshing)
            OnlineType.ONLINEBANK.type -> mContext.resources.getString(R.string.online_online_bank)
            OnlineType.GCASH.type -> mContext.resources.getString(R.string.online_gcash)
            OnlineType.GRABPAY.type -> mContext.resources.getString(R.string.online_grab)
            OnlineType.PAYMAYA.type -> mContext.resources.getString(R.string.online_maya)
            OnlineType.PAYPAL.type -> mContext.resources.getString(R.string.online_paypal)
            OnlineType.DRAGON_PAY.type -> mContext.resources.getString(R.string.online_gragon_pay)
            OnlineType.MOMOPAY.type -> mContext.resources.getString(R.string.online_momopay)
            OnlineType.ZALOPAY.type -> mContext.resources.getString(R.string.online_zalopay)
            OnlineType.VIETTELPAY.type -> mContext.resources.getString(R.string.online_viettelpay)
            OnlineType.RECHARGE_CARD.type -> mContext.resources.getString(R.string.online_recharge_card_pay)
            OnlineType.FORTUNE_PAY.type -> mContext.resources.getString(R.string.online_fortune_pay)
            else -> ""
        }
    }
}