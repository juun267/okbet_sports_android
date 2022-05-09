package org.cxct.sportlottery.ui.maintenance

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maintenance.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.util.JumpUtil

class MaintenanceActivity : BaseSocketActivity<MaintenanceViewModel>(MaintenanceViewModel::class) {

    enum class MaintainType(val value: Int) {
        NORMAL(0), FIXING(1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maintenance)

        initObserver()
        initSocketObserver()
        initServiceButton()
    }

    private fun initObserver() {
        viewModel.getConfig()

        viewModel.configResult.observe(this) {
            tv_maintenance_time.text = it?.configData?.maintainInfo
            //改為右下角客服按鈕
//            tv_customer_service.text = String.format(
//                getString(R.string.if_there_is_any_question_please_consult),
//                ""
//            )
        }
    }

    private fun initServiceButton() {
//        btn_floating_service.setView4Maintenance(this)
        btn_service.setOnClickListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this@MaintenanceActivity, serviceUrl)
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this@MaintenanceActivity, serviceUrl2)
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    JumpUtil.toExternalWeb(this@MaintenanceActivity, serviceUrl)
                }
            }
        }
    }

    private fun initSocketObserver() {
        receiver.sysMaintenance.observe(this) {
            it?.let { item ->
                if (item.status == MaintainType.NORMAL.value) {
                    if (sConfigData?.thirdOpen == FLAG_OPEN)
                        MainActivity.reStart(this)
                    else
                        GamePublicityActivity.reStart(this)
                    finish()
                } else {
                    //do nothing
                }
            }
        }
    }

}