package org.cxct.sportlottery.ui.money.recharge

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity

class MoneyRechargeActivity : BaseToolBarActivity<MoneyRechViewModel>(MoneyRechViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    private fun initData() {
        viewModel.getRechCfg()
    }

    override fun setContentView(): Int {
        return R.layout.activity_money_recharge
    }

    override fun setToolBarName(): String {
        return "资金"
    }

}