package org.cxct.sportlottery.ui.maintab.home.bettingstation

import android.text.style.ForegroundColorSpan
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityBettingstationBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.Spanny

class BettingStationActivity: BaseActivity<MainHomeViewModel,ActivityBettingstationBinding>() {
    override fun pageName() = "投注站页面"
    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF,true)
        binding.tvTitle.text = Spanny(getString(R.string.N910)).findAndSpan("OKBET"){
            ForegroundColorSpan(getColor(R.color.color_025BE8))
        }
        binding.customToolBar.setOnBackPressListener { finish() }
        binding.bettingStationView.setup(this,viewModel)
        binding.bottomView.bindServiceClick(supportFragmentManager)
        binding.bottomView.binding.endView.setPadding(0,0,0,15.dp)
    }
}