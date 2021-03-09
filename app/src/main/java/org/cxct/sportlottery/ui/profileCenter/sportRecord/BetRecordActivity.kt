package org.cxct.sportlottery.ui.profileCenter.sportRecord

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_other_bet_record.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity

class BetRecordActivity : BaseOddButtonActivity<BetRecordViewModel>(BetRecordViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bet_record)

        initToolbar()
    }

    private fun initToolbar() {
        toolbar.apply {
            tv_toolbar_title.text = getString(R.string.sport_bet_record)
            btn_toolbar_back.setOnClickListener {
                onBackPressed()
            }
        }
    }

}