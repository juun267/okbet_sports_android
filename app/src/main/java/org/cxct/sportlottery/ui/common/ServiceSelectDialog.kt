package org.cxct.sportlottery.ui.common

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.dialog_service_select.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.JumpUtil

/**
 * 客服選擇跳轉外部瀏覽器
 *   //TODO simon test UI 等待重新製作
 */
class ServiceSelectDialog(context: Context) : AlertDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_service_select)

        initButton()
    }

    private fun initButton() {
        //TODO simon test 客服 url 獲取
        val zxkfUrl: String = "https://www.google.com/" //AppConfigManager.INSTANCE.getAppConfig().zxkfUrl
        val zxkfUrl2: String = "https://www.yahoo.com.tw/" //AppConfigManager.INSTANCE.getAppConfig().zxkfUrl2

        btn_service1.setOnClickListener {
            JumpUtil.toOutWeb(context, zxkfUrl)
            dismiss()
        }

        btn_service2.setOnClickListener {
            JumpUtil.toOutWeb(context, zxkfUrl2)
            dismiss()
        }
    }
}