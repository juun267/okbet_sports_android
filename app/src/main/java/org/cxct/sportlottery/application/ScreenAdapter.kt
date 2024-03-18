package org.cxct.sportlottery.application

import android.app.Activity
import android.app.Application
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.onAdaptListener
import org.cxct.sportlottery.util.ScreenUtil

object ScreenAdapter {

    private const val SysWidthConfig = 390
    private var sysWidth = 0
    private var sysHeight = 0

    fun init(application: Application) {
        sysWidth = ScreenUtil.getScreenWidth(application)//竖屏进入 获取屏幕宽度
        sysHeight = ScreenUtil.getScreenHeight(application)//竖屏进入 获取屏幕高度
        AutoSizeConfig.getInstance().isExcludeFontScale = true  // 字体大小不随系统字体大小变化
        initAuto()
    }

    private fun initAuto() {

        // 强制以竖屏的方式去适配
        AutoSizeConfig.getInstance().setOnAdaptListener(object : onAdaptListener {
            override fun onAdaptBefore(target: Any, activity: Activity) {
                AutoSizeConfig.getInstance().setScreenWidth(sysWidth)
                AutoSizeConfig.getInstance().setScreenHeight(sysHeight)
                AutoSizeConfig.getInstance().setDesignWidthInDp(SysWidthConfig)
            }

            override fun onAdaptAfter(target: Any, activity: Activity) {}
        })
    }

}