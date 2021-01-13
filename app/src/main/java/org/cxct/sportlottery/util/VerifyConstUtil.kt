package org.cxct.sportlottery.util

import java.util.regex.Pattern

object VerifyConstUtil {
    private const val NUMBER = "0-9"
    private const val CHINESE_WORD = "\u4e00-\u9fa5"
    private const val VIETNAM_WORD = "àáãạảăắằẳẵặâấầẩẫậèéẹẻẽêềếểễệđìíĩỉịòóõọỏôốồổỗộơớờởỡợùúũụủưứừửữựỳỵỷỹýÀÁÃẠẢĂẮẰẲẴẶÂẤẦẨẪẬÈÉẸẺẼÊỀẾỂỄỆĐÌÍĨỈỊÒÓÕỌỎÔỐỒỔỖỘƠỚỜỞỠỢÙÚŨỤỦƯỨỪỬỮỰỲỴỶỸÝ"
    private const val ENGLISH_WORD = "a-zA-Z"

    //是否為越南文文字
    private fun isValidVietnamWord (inputStr: CharSequence) : Boolean {
        return Pattern.matches("[${ENGLISH_WORD}${VIETNAM_WORD}\\s]+", inputStr)
    }

    //是否為中文文字
    private fun isValidChineseWord (inputStr: CharSequence) : Boolean {
        return Pattern.matches("[$CHINESE_WORD]", inputStr)
    }

    fun verifyPayPwd(pwd: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER]{4}", pwd)
    }

    fun verifyAccount(account: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER$ENGLISH_WORD]{4,16}$", account)
    }

    fun verifyPwd(pwd: CharSequence): Boolean {
        return Pattern.matches("[$NUMBER$ENGLISH_WORD]{6,20}$", pwd)
    }

    //真實姓名 //中文2-20 可空格 可點 ,英文2-50 可空格 可點
    fun verifyFullName(inputStr: CharSequence): Boolean {

        return Pattern.compile("[\\s.$CHINESE_WORD]{2,5}").matcher(inputStr).matches() ||
                Pattern.compile("[\\s.$ENGLISH_WORD]{2,50}").matcher(inputStr).matches()
    }

    //qq //判断qq字數小於五
    fun verifyCharLowFive(text: CharSequence): Boolean {
        return text.length >= 5
    }

    //mail
    fun verifyMail(text: CharSequence): Boolean {
        return Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*").matcher(text).matches()
    }

    //phone //判断手机号 手機號碼 10-11 碼 (越南格式)
    fun verifyPhoneNumberInRegister(text: CharSequence): Boolean {
        return Pattern.compile("\\d{10,11}$").matcher(text).matches()
    }

    //微信 //英文第一位大小寫 後面可以數字或英文6~20
    fun verifyWeChat(text: CharSequence): Boolean {
        return Pattern.compile("^[$ENGLISH_WORD][-_$NUMBER$ENGLISH_WORD]{5,20}$").matcher(text).matches()
    }

    //Telegram //可以數字、英文或底線, 5~32個char
    fun verifyTelegram(text: CharSequence): Boolean {
        return Pattern.compile("^[-_$NUMBER$ENGLISH_WORD]{5,32}$").matcher(text).matches()
    }

}