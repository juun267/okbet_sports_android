package org.cxct.sportlottery.ui.profileCenter.sportRecord

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseNoticeActivity

class BetRecordActivity : BaseNoticeActivity<BetRecordViewModel>(BetRecordViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bet_record)
    }
}