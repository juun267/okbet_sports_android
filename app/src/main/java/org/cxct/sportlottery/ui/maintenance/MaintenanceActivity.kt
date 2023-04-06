package org.cxct.sportlottery.ui.maintenance

import android.os.Bundle
import android.text.TextUtils
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_maintenance.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.index.config.ConfigData
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.JumpUtil

class MaintenanceActivity : BaseSocketActivity<MaintenanceViewModel>(MaintenanceViewModel::class) {

    enum class MaintainType(val value: Int) {
        NORMAL(0), FIXING(1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        setContentView(R.layout.activity_maintenance)

        viewModel.getConfig()
        initObserver()
        initSocketObserver()
        initServiceButton(sConfigData)
    }

    private fun initObserver() {
        viewModel.configResult.observe(this) {
            //確認當前平台是否維護中
            it?.configData?.let { initServiceButton(it) }
            when (it?.configData?.maintainStatus) {
                FLAG_OPEN -> {
                    //仍然維護中則更新該平台目前的維護資訊
                    tv_maintenance_time.text = it.configData.maintainInfo
                }
                else -> {
//                    if (sConfigData?.thirdOpen == FLAG_OPEN)
//                        MainActivity.reStart(this)
//                    else
                    MainTabActivity.reStart(this)
                    finish()
                }
            }
            //改為右下角客服按鈕
//            tv_customer_service.text = String.format(
//                getString(R.string.if_there_is_any_question_please_consult),
//                ""
//            )
        }
    }

    private fun initServiceButton(configData: ConfigData?) {
//        btn_floating_service.setView4Maintenance(this)

        val serviceUrl = configData?.customerServiceUrl
        val serviceUrl2 = configData?.customerServiceUrl2

        btn_service.isVisible = !TextUtils.isEmpty(serviceUrl) || !TextUtils.isEmpty(serviceUrl2)
        btn_service.setOnClickListener {

            if (!TextUtils.isEmpty(serviceUrl)) {
                JumpUtil.toExternalWeb(this@MaintenanceActivity, serviceUrl)
                return@setOnClickListener
            }

            if (!TextUtils.isEmpty(serviceUrl2)) {
                JumpUtil.toExternalWeb(this@MaintenanceActivity, serviceUrl2)
                return@setOnClickListener
            }
        }
    }

    private fun initSocketObserver() {
        receiver.sysMaintenance.observe(this) {
            //接收到系統維護狀態變化時, 請求config確認當前平台是否維護中
            viewModel.getConfig()
        }
    }

}