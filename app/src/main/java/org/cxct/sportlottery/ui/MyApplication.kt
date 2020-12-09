package org.cxct.sportlottery.ui

import android.app.Application
import android.content.Context
import android.content.res.Resources

/**
 * Created by Simon Chang on 2018/09/11.
 */
class MyApplication : Application() {
    //Enable multidex for apps with over 64K   https://developer.android.com/studio/build/multidex
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext


    }

    companion object {
        var appContext: Context? = null
            private set

        val res: Resources
            get() = appContext!!.resources
    }
}