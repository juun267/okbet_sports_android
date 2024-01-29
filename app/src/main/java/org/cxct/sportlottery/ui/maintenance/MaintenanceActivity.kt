package org.cxct.sportlottery.ui.maintenance

import android.text.TextUtils
import android.view.View
import androidx.core.view.isVisible
import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.fitsSystemStatus
import org.cxct.sportlottery.databinding.ActivityMaintenanceBinding
import org.cxct.sportlottery.network.index.config.ConfigData
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.JumpUtil

class MaintenanceActivity : BaseActivity<MaintenanceViewModel, ActivityMaintenanceBinding>() {

    enum class MaintainType(val value: Int) {
        NORMAL(0), FIXING(1)
    }

    override fun onInitView() {
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .fitsSystemWindows(false)
            .init()
        findViewById<View>(R.id.root).fitsSystemStatus()
        initObserver()
        initServiceButton(sConfigData)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getConfig()
    }

    private fun initObserver() {
        viewModel.configResult.observe(this) {
            //確認當前平台是否維護中
            it?.configData?.let { initServiceButton(it) }
            when (it?.configData?.maintainStatus) {
                FLAG_OPEN -> {
                    //仍然維護中則更新該平台目前的維護資訊
                    binding.tvMaintenanceTime.text = it.configData.maintainInfo
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

        binding.btnService.isVisible = !TextUtils.isEmpty(serviceUrl) || !TextUtils.isEmpty(serviceUrl2)
        binding.btnService.setOnClickListener {

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


}