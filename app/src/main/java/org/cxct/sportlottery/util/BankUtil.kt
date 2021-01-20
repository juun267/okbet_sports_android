package org.cxct.sportlottery.util

import org.cxct.sportlottery.R

object BankUtil {
    fun getBankIconByBankName(bankName: String): Int {
        BankKey.values().map { if (it.bankName == bankName) return it.iconId }
        return R.drawable.ic_bank_default
    }
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