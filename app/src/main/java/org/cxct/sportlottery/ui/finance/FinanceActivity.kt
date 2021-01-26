package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity

class FinanceActivity : BaseActivity<FinanceViewModel>(FinanceViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_finance)
    }
}