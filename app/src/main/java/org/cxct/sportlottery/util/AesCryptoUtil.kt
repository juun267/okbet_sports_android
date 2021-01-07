package org.cxct.sportlottery.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.jvm.Throws

/**
 * Create by Simon Chang
 * 存取機敏資料 AES 加解密使用
 */
object AesCryptoUtil {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private val KEY_AES = "hello_world_1234".toByteArray()
    private val IV_AES = "1234567890987654".toByteArray()

    @Throws(Exception::class)
    fun encrypt(strToEncrypt: String): String {
        val secretKeySpec = SecretKeySpec(KEY_AES, "AES")
        val ivParameterSpec = IvParameterSpec(IV_AES)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val textByte = cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8))

        //20180226 Base64.DEFAULT 轉換後結尾有換行符號，在 SharedPreferences 的 key 有換行符會出錯
        //Base64.NO_WRAP 轉換後結尾不會有換行符號
        return Base64.encodeToString(textByte, Base64.NO_WRAP)
    }

    @Throws(Exception::class)
    fun decrypt(strToDecrypt: String): String {
        val secretKeySpec = SecretKeySpec(KEY_AES, "AES")
        val ivParameterSpec = IvParameterSpec(IV_AES)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val textByte = cipher.doFinal(Base64.decode(strToDecrypt.toByteArray(), Base64.NO_WRAP))
        return String(textByte, Charsets.UTF_8)
    }
}