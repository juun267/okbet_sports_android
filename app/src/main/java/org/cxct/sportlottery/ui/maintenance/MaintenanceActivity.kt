package org.cxct.sportlottery.ui.maintenance

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_maintenance.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity

class MaintenanceActivity : BaseActivity<MaintenanceViewModel>(MaintenanceViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maintenance)

        viewModel.getConfig()

        viewModel.configResult.observe(this) {
            tv_maintenance_time.text = it?.configData?.maintainInfo
            tv_customer_service.text = it?.configData?.customerServiceUrl
        }
    }

}