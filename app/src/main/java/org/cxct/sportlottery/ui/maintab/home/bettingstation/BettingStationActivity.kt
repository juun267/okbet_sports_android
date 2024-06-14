package org.cxct.sportlottery.ui.maintab.home.bettingstation

import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityBettingstationBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel

class BettingStationActivity: BaseActivity<MainHomeViewModel,ActivityBettingstationBinding>() {
    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF,true)
        binding.customToolBar.setOnBackPressListener { finish() }
        binding.bettingStationView.setup(this,viewModel)
        binding.bottomView.bindServiceClick(supportFragmentManager)
    }
}