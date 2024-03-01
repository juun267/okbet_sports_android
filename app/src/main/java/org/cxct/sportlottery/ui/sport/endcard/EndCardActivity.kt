package org.cxct.sportlottery.ui.sport.endcard

import org.cxct.sportlottery.databinding.ActivityEndcardBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.sport.SportViewModel

class EndCardActivity: BaseActivity<SportViewModel, ActivityEndcardBinding>() {

    override fun onInitView() {
        setStatusBarDarkFont()
        binding.homeToolbar.attach(this, { }, viewModel)


    }


}