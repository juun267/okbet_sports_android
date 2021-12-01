package org.cxct.sportlottery.ui.profileCenter.sportRecord

import android.os.Bundle
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity

class BetRecordActivity : BaseSocketActivity<BetRecordViewModel>(BetRecordViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bet_record)
        initToolbar()
    }

    private fun initToolbar() {
        tv_toolbar_title.text = getString(R.string.sport_bet_record)
        btn_toolbar_back.setOnClickListener {
            onBackPressed()
        }
    }

}