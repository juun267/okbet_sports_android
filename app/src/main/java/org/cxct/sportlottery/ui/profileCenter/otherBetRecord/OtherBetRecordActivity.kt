package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import android.os.Bundle
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.util.setTitleLetterSpacing

class OtherBetRecordActivity :
    BaseSocketActivity<OtherBetRecordViewModel>(OtherBetRecordViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_bet_record)
        initToolbar()
    }

    private fun initToolbar() {
        tv_toolbar_title.setTitleLetterSpacing()
        tv_toolbar_title.text = getString(R.string.other_bet_record)
        btn_toolbar_back.setOnClickListener {
            onBackPressed()
        }
    }

}