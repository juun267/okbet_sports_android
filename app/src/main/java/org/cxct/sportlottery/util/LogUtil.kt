package org.cxct.sportlottery.util

import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import org.cxct.sportlottery.BuildConfig

object LogUtil {
    init {
        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .tag("oklog")
            .build()
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
    }

    fun d(msg: String) {
        Logger.d(msg)
    }

    fun d(msg: String, t: Throwable?) {
        Logger.d(msg, t)
    }

    fun e(msg: String) {
        Logger.e(msg)
    }

    fun e(msg: String, t: Throwable?) {
        Logger.e(msg, t)
    }

    fun i(msg: String) {
        Logger.i(msg)
    }

    fun i(msg: String, t: Throwable?) {
        Logger.i(msg, t)
    }

    fun v(msg: String) {
        Logger.v(msg)
    }

    fun v(msg: String, t: Throwable?) {
        Logger.v(msg, t)
    }

    fun w(msg: String) {
        Logger.w(msg)
    }

    fun w(msg: String, t: Throwable?) {
        Logger.w(msg, t)
    }

    fun json(json: String?) {
        Logger.json(json)
    }

    fun xml(xml: String?) {
        Logger.xml(xml)
    }

    fun toJson(obj: Any?) {
        if (BuildConfig.DEBUG) {
            if (obj == null) {
                Logger.json("null")
            } else {
                Logger.json(JsonUtil.toJson(obj))
            }
        }
    }
}