package org.cxct.sportlottery.ui.bet_record

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_bet_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityBetRecordBinding
import org.cxct.sportlottery.ui.base.BaseActivity

class BetRecordActivity : BaseActivity<BetRecordViewModel>(BetRecordViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityBetRecordBinding>(this, R.layout.activity_bet_record)
        binding.apply {
            betRecordViewModel = this@BetRecordActivity.viewModel
            lifecycleOwner = this@BetRecordActivity
        }
    }
}