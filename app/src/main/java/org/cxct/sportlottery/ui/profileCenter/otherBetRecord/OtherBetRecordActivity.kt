package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity

class OtherBetRecordActivity : BaseOddButtonActivity<OtherBetRecordViewModel>(OtherBetRecordViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_bet_record)
    }
}