package org.cxct.sportlottery.common.extentions

import android.graphics.Color
import android.text.TextUtils

fun String?.toIntS(default: Int = 0) = if (null == this) default else try { toInt() } catch (e: Exception) { default }

fun Int?.toStringS(default: String = "") = this?.toString() ?: default

fun String?.parseColor(default: Int = Color.WHITE) = if (null == this) default else try {  Color.parseColor(this) } catch (e: Exception) { default }

fun String?.toFloatS(default: Float = 0f) = if (null == this) default else try { toFloat() } catch (e: Exception) { default }

fun String?.toLongS(default: Long = 0L) = if (null == this) default else try { toLong() } catch (e: Exception) { default }

fun String?.toDoubleS(default: Double = 0.0): Double {
    if (null == this) {
        return default
    }
    return try {
        toDouble()
    } catch (e: Exception) {
        default
    }
}

fun String?.isEmptyStr() = TextUtils.isEmpty(this)