package org.cxct.sportlottery.util

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.ServiceFloatingButton
import org.cxct.sportlottery.ui.game.ServiceDialog

object ServiceUtil {
    /**
     * 判斷是否需要顯示客服懸浮按鈕, 若需要則加入相對應點擊事件
     */
    fun ServiceFloatingButton.setView(activity: AppCompatActivity) {
        if (sConfigData?.customerServiceUrl.isNullOrBlank() && sConfigData?.customerServiceUrl2.isNullOrBlank()) {
            this.visibility = View.GONE
        } else {
            this.visibility = View.VISIBLE
            this.setupClickEvent(activity)
        }
    }

    /**
     * 若有兩個有效客服外部連結則彈出Dialog, 若只有一個則直接跳轉
     */
    private fun ServiceFloatingButton.setupClickEvent(activity: AppCompatActivity) {
        serviceFloatingListener = ServiceFloatingButton.ServiceFloatingListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    ServiceDialog().show(activity.supportFragmentManager, activity::class.java.simpleName)
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(activity, serviceUrl)
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(activity, serviceUrl2)
                }
            }
        }
    }
}