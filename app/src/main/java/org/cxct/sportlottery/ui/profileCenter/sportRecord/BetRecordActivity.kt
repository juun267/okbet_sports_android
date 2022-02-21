package org.cxct.sportlottery.ui.profileCenter.sportRecord

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_bet_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity

class BetRecordActivity : BaseSocketActivity<BetRecordViewModel>(BetRecordViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bet_record)
        initToolbar()
    }

    private fun initToolbar() {
        custom_tool_bar.setOnBackPressListener {
            onBackPressed()
        }
    }

}