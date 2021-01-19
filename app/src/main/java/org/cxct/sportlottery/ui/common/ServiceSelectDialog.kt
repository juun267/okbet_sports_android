package org.cxct.sportlottery.ui.common

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.dialog_service_select.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.JumpUtil

/**
 * 客服選擇跳轉外部瀏覽器
 *   //TODO simon test UI 等待重新製作
 */
class ServiceSelectDialog(context: Context) : AlertDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_service_select)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        initButton()
    }

    private fun initButton() {
        btn_close.setOnClickListener { dismiss() }

        btn_service1.setOnClickListener {
            JumpUtil.toOutWeb(context, sConfigData?.customerServiceUrl)
            dismiss()
        }

        btn_service2.setOnClickListener {
            JumpUtil.toOutWeb(context, sConfigData?.customerServiceUrl2)
            dismiss()
        }
    }
}