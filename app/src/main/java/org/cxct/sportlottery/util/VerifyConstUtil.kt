package org.cxct.sportlottery.util

import org.cxct.sportlottery.repository.sConfigData
import java.util.regex.Pattern

object VerifyConstUtil {
    private const val NUMBER = "0-9"
    private const val CHINESE_WORD = "\u4e00-\u9fa5"
    private const val VIETNAM_WORD = "àáãạảăắằẳẵặâấầẩẫậèéẹẻẽêềếểễệđìíĩỉịòóõọỏôốồổỗộơớờởỡợùúũụủưứừửữựỳỵỷỹýÀÁÃẠẢĂẮẰẲẴẶÂẤẦẨẪẬÈÉẸẺẼÊỀẾỂỄỆĐÌÍĨỈỊÒÓÕỌỎÔỐỒỔỖỘƠỚỜỞỠỢÙÚŨỤỦƯỨỪỬỮỰỲỴỶỸÝ"
    private const val ENGLISH_WORD = "a-zA-Z"
    private const val SYMBOL = "!#\$%&'*+-/=?^_`{|}~"

    private const val EMAIL_REGEX = "^([_$ENGLISH_WORD$NUMBER$SYMBOL]+)+@[-$ENGLISH_WORD$NUMBER$SYMBOL]+([.][-$ENGLISH_WORD$NUMBER$SYMBOL]+)*[.]((?=.*[$ENGLISH_WORD$SYMBOL])[$ENGLISH_WORD$NUMBER$SYMBOL]+([-$ENGLISH_WORD$NUMBER$SYMBOL]*[$ENGLISH_WORD$NUMBER$SYMBOL]+)*)\$"
    private const val CRYPTO_COMMON_WALLET_ADDRESS_REGEX = "^(?=.*[$ENGLISH_WORD])(?=.*[$NUMBER])[$ENGLISH_WORD$NUMBER]+$"

    //是否為越南文文字
    fun isValidVietnamWord(inputStr: CharSequence): Boolean {
        return Pattern.matches("[${ENGLISH_WORD}${VIETNAM_WORD}\\s]+", inputStr)
    }

    private fun isVerifyEmailFormat(inputStr: CharSequence, strLength: String): Boolean {
        return if (!Pattern.matches(".$strLength", inputStr))
            false
        else {
            Pattern.matches(EMAIL_REGEX, inputStr)
        }
    }

    //是否為中文文字
     fun isValidChineseWord(inputStr: CharSequence): Boolean {
        return Pattern.matches("[$CHINESE_WORD]{1,50}", inputStr)
    }
    //是否為英文文字
    fun isValidEnglishWord(inputStr: CharSequence): Boolean {
        return Pattern.matches("[$ENGLISH_WORD]{1,50}", inputStr)
    }
    fun verifyInviteCode(inviteCode: CharSequence): Boolean {
        return Pattern.matches("([_$ENGLISH_WORD$NUMBER]){4,8}$", inviteCode)
    }


    fun verifyPayPwd(pwd: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER]{4}", pwd)
    }

    fun verifyAccount(account: CharSequence): Boolean {
        return Pattern.matches("([_$ENGLISH_WORD$NUMBER]+){4,16}$", account)
    }

    fun verifyCombinationAccount(account: CharSequence): Boolean {
        return Pattern.matches("(?=.*[$NUMBER])(?=.*[$ENGLISH_WORD]).{4,16}", account)
    }

    fun verifyPwdFormat(pwd: CharSequence): Boolean {
        return !(Pattern.matches("[$NUMBER]*", pwd) || Pattern.matches("[$ENGLISH_WORD]*", pwd))
    }

    fun verifyPwd(pwd: CharSequence): Boolean {
        return Pattern.matches("(?=.*[0-9])(?=.*[a-zA-Z])([a-zA-Z0-9]+){6,20}", pwd)
    }

    //真實姓名 只允许英文和空格，不允许前后空格和连续空格
    fun verifyFullName(fullName: CharSequence?): Boolean {
        if (fullName.isNullOrBlank()||fullName.startsWith(" ")||fullName.endsWith(" ")){
            return false
        }
        if (fullName.startsWith(" ")||fullName.endsWith(" ")){
            return false
        }
        if (fullName.contains("  ")){
            return false
        }
        return Pattern.matches("[a-zA-Z\\s]{1,50}", fullName)
    }

    //提款密碼 //數字4
    fun verifyWithdrawPassword(withdrawPassword: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER]{4}", withdrawPassword)
    }

    //虛擬幣錢包地址
    fun verifyCryptoWalletAddress(walletAddress: CharSequence): Boolean {
        return Pattern.matches(CRYPTO_COMMON_WALLET_ADDRESS_REGEX, walletAddress)
    }

    //提款金額 //最低與最高同步後台設定值, 最高限制:餘額最大金額,限制提款最大金額 取小者
    //-1为低于限额，1为超过限额，
    fun verifyWithdrawAmount(
        withdrawAmount: CharSequence,
        minAmount: Double?,
        maxAmount: Double?
    ): Int {
        val withdrawAmountDouble = withdrawAmount.toString().toDouble()
        val minLimit = minAmount ?: 0.0
        if (withdrawAmountDouble < minLimit) return -1
        if (maxAmount != null && withdrawAmountDouble > maxAmount) return 1
        return 0
    }

    //充值金額
    //-1为低于限额，1为超过限额，
    fun verifyRechargeAmount(rechargeAmount: CharSequence, minAmount: Long, maxAmount: Long?): Int {
        val rechargeAmountLong = rechargeAmount.toString().toLong()
        if (rechargeAmountLong < minAmount) return -1
        if (maxAmount != null && rechargeAmountLong > maxAmount) return 1
        return 0
    }

    fun verifyFirstRechargeAmount(rechargeAmount: CharSequence): Boolean {
        val firstRechLessAmountLimit = sConfigData?.firstRechLessAmountLimit
        return when {
            //首充額度限制若為null,""或0則不限制
            firstRechLessAmountLimit.isNullOrEmpty() || firstRechLessAmountLimit.toDouble() <= 0.0 -> true
            else -> rechargeAmount.toString().toDouble() >= firstRechLessAmountLimit.toDouble()
        }
    }

    //暱稱 //中英文組合長度2–50字
    fun verifyNickname(nickname: CharSequence): Boolean {
        return Pattern.matches("[$CHINESE_WORD$ENGLISH_WORD]{2,6}", nickname)
    }

    //qq //判断qq字數小於五
    fun verifyQQ(qqAcount: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER]{5,11}", qqAcount)
    }

    //mail
    fun verifyMail(mail: CharSequence): Boolean {
        return isVerifyEmailFormat(mail, "{0,50}")
    }

    //手機號碼 //11個內全數字組合可以为10位数
    fun verifyPhone(phone: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER]{10,11}", phone)
    }

    //微信 //英文第一位大小寫 後面可以數字或英文6~20
    fun verifyWeChat(weChat: CharSequence): Boolean {
        return Pattern.matches("[$ENGLISH_WORD][-_$NUMBER$ENGLISH_WORD]{5,19}", weChat)
    }

    //Facebook //50個內英數組合電子郵件格式(含特殊字元)
    fun verifyFacebook(facebook: CharSequence): Boolean {
        return isVerifyEmailFormat(facebook, "{0,50}")
    }

    //WhatsApp //25個內英數組合電子郵件格式(含特殊字元)
    fun verifyWhatsApp(whatsApp: CharSequence): Boolean {
        return isVerifyEmailFormat(whatsApp, "{0,25}")
    }

    //Zalo //11個內全數字組合(通常是由中國或越南手機註冊認證)
    fun verifyZalo(zalo: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER]{0,11}", zalo)
    }

    //Telegram //可以數字、英文或底線, 5~32個char
    fun verifyTelegram(telegram: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER]{0,11}", telegram)
    }

    //驗證碼 //數字 4位
    fun verifyValidCode(validCode: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER]{4}", validCode)
    }

    //簡訊驗證碼 //數字 4位
    fun verifySecurityCode(securityCode: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER]{4}", securityCode)
    }

    //区块链交易ID //长度大于等于6位,小于256位
    fun verifyHashCode(hashCode: CharSequence): Boolean {
        return Pattern.matches("[$ENGLISH_WORD$NUMBER]{6,256}", hashCode)
    }

}