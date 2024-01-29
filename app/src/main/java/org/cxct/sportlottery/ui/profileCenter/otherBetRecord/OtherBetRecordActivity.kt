package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityOtherBetRecordBinding
import org.cxct.sportlottery.ui.base.BaseActivity

class OtherBetRecordActivity : BaseActivity<OtherBetRecordViewModel,ActivityOtherBetRecordBinding>(OtherBetRecordViewModel::class) {

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        initToolbar()
    }

    private fun initToolbar()=binding.toolBar.run {
        titleText = getString(R.string.other_bet_record)
        setOnBackPressListener {
            onBackPressed()
        }
    }

}