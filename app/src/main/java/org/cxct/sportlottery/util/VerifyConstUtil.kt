package org.cxct.sportlottery.util

import java.util.regex.Pattern
import kotlin.math.min

object VerifyConstUtil {
    private const val NUMBER = "0-9"
    private const val CHINESE_WORD = "\u4e00-\u9fa5"
    private const val VIETNAM_WORD = "àáãạảăắằẳẵặâấầẩẫậèéẹẻẽêềếểễệđìíĩỉịòóõọỏôốồổỗộơớờởỡợùúũụủưứừửữựỳỵỷỹýÀÁÃẠẢĂẮẰẲẴẶÂẤẦẨẪẬÈÉẸẺẼÊỀẾỂỄỆĐÌÍĨỈỊÒÓÕỌỎÔỐỒỔỖỘƠỚỜỞỠỢÙÚŨỤỦƯỨỪỬỮỰỲỴỶỸÝ"
    private const val ENGLISH_WORD = "a-zA-Z"

    //是否為越南文文字
    private fun isValidVietnamWord(inputStr: CharSequence): Boolean {
        return Pattern.matches("[${ENGLISH_WORD}${VIETNAM_WORD}\\s]+", inputStr)
    }

    //是否為中文文字
    private fun isValidChineseWord(inputStr: CharSequence): Boolean {
        return Pattern.matches("[$CHINESE_WORD]", inputStr)
    }

    fun verifyPayPwd(pwd: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER]{4}", pwd)
    }

    fun verifyAccount(account: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER$ENGLISH_WORD]{4,16}$", account)
    }

    fun verifyPwd(pwd: CharSequence): Boolean {
        return Pattern.matches("(?=.*[0-9])(?=.*[a-zA-Z])([a-zA-Z0-9]+){6,20}", pwd)
    }

    //真實姓名 //中文2-20,英文2-50 可空格 可點
    //20210205判斷文件只容許中文2-20
    fun verifyFullName(fullName: CharSequence): Boolean {
        return Pattern.matches("[$CHINESE_WORD]{2,20}", fullName)
//                || Pattern.matches("[\\s.$ENGLISH_WORD]{2,50}", fullName)
    }

    //持卡人姓名 //中文2-20
    fun verifyCreateName(createName: CharSequence): Boolean {
        return Pattern.matches("[\\s.$CHINESE_WORD]{2,20}", createName)
    }

    //銀行卡號 //數字12-19
    fun verifyBankCardNumber(bankCardNumber: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER]{12,19}", bankCardNumber)
    }

    //開戶網點 //中文1-25
    fun verifyNetworkPoint(networkPoint: CharSequence): Boolean {
        return Pattern.matches("[\\s.$CHINESE_WORD]{1,25}", networkPoint)
    }

    //提款密碼 //數字4
    fun verifyWithdrawPassword(networkPoint: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER]{4}", networkPoint)
    }

    //提款金額 //最低與最高同步後台設定值, 最高限制:餘額最大金額,限制提款最大金額 取小者
    fun verifyWithdrawAmount(withdrawAmount: CharSequence, minAmount: Long, maxAmount: Long?, userMoney: Double?, fee: Double): Boolean {
        val balanceMax = userMoney?.minus(fee) //餘額的最大金額 = 餘額 - 手續費
        //餘額最大金額,限制提款最大金額 取小者
        val maxLimit = when {
            maxAmount == null -> {
                balanceMax?.toLong()
            }
            balanceMax == null -> {
                maxAmount
            }
            else -> {
                min(maxAmount, balanceMax.toLong())
            }
        }
        return (withdrawAmount.toString().toLong().let { it in minAmount until ((maxLimit ?: it) + 1) })
    }

    //充值金額
    fun verifyRechargeAmount(withdrawAmount: CharSequence, minAmount: Long, maxAmount: Long?): Boolean {
        return (withdrawAmount.toString().toLong().let { it in minAmount until (maxAmount ?: it + 1) })
    }

    //暱稱 //中英文組合長度2–50字
    fun verifyNickname(nickname: CharSequence): Boolean {
        return Pattern.matches("[$CHINESE_WORD$ENGLISH_WORD]{2,6}", nickname)
    }

    //qq //判断qq字數小於五
    fun verifyQQ(text: CharSequence): Boolean {
        return text.length >= 5
    }

    //mail
    fun verifyMail(mail: CharSequence): Boolean {
        return Pattern.matches("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*", mail)
    }

    //微信 //英文第一位大小寫 後面可以數字或英文6~20
    fun verifyWeChat(weChat: CharSequence): Boolean {
        return Pattern.matches("^[$ENGLISH_WORD][-_$NUMBER$ENGLISH_WORD]{5,19}$", weChat)
    }

    //Telegram //可以數字、英文或底線, 5~32個char
    fun verifyTelegram(telegram: CharSequence): Boolean {
        return Pattern.matches("^[-_$NUMBER$ENGLISH_WORD]{5,32}$", telegram)
    }

}