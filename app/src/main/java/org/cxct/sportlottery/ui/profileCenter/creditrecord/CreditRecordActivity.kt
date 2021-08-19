package org.cxct.sportlottery.ui.profileCenter.creditrecord

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketActivity

class CreditRecordActivity :
    BaseSocketActivity<CreditRecordViewModel>(CreditRecordViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_record)
    }
}