package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseNoticeActivity

class OtherBetRecordActivity :
    BaseNoticeActivity<OtherBetRecordViewModel>(OtherBetRecordViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_bet_record)
    }
}