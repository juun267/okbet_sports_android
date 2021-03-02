package org.cxct.sportlottery.ui.bet.record

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityBetRecordBinding
import org.cxct.sportlottery.ui.base.BaseNoticeActivity

class BetRecordActivity : BaseNoticeActivity<BetRecordViewModel>(BetRecordViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityBetRecordBinding>(this, R.layout.activity_bet_record)
        binding.apply {
            betRecordViewModel = this@BetRecordActivity.viewModel
            lifecycleOwner = this@BetRecordActivity
        }
    }
}