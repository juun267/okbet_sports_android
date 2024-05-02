package org.cxct.sportlottery.ui.profileCenter.vip

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityMyVipDetailBinding
import org.cxct.sportlottery.net.user.data.UserVip
import org.cxct.sportlottery.ui.base.BaseActivity

class MyVipDetailActivity: BaseActivity<VipViewModel,ActivityMyVipDetailBinding>() {

    override fun onInitView() {
        setStatusbar()
        binding.customToolBar.setOnBackPressListener {
            finish()
        }
        setStatus(null)
      viewModel.userVipEvent.observe(this){
          setStatus(it)
      }
      viewModel.getUserVip()
    }

    fun setStatus(userVip: UserVip?)=binding.run{
        when{
            userVip==null-> {
                linContent.gone()
                linEmpty.visible()
                linStatus.setBackgroundResource(R.drawable.bg_white_radius_12)
                tvTag.setBackgroundResource(R.drawable.bg_keepgrade_gray)
            }
            userVip.exp<userVip.upgradeExp-> {
                linContent.visible()
                linEmpty.gone()
                tvAmount.visible()
                tvAmount.text = (userVip.upgradeExp-userVip.exp).toString()
                tvContent.text = getString(R.string.P396)
                linStatus.setBackgroundResource(R.drawable.bg_vipdetails_orange)
                tvTag.setBackgroundResource(R.drawable.bg_keepgrade_orange)
                vpProgress.setTintColor(R.color.color_025BE8,R.color.color_19025BE8)
//                vpProgress.setTintColor(R.color.color_FFB828,R.color.color_A78031)
                val levelExp = userVip.rewardInfo.firstOrNull { it.levelCode == userVip.levelCode }?.upgradeExp?:0L
                vpProgress.setProgress((userVip.upgradeExp*100/levelExp).toInt())
                tvAmount.text = (userVip.upgradeExp-userVip.exp).toString()
            }
            userVip.upgradeExp==userVip.upgradeExp-> {
                linContent.visible()
                linEmpty.gone()
                tvAmount.gone()
                tvContent.text = getString(R.string.P397)
                linStatus.setBackgroundResource(R.drawable.bg_vipdetails_blue)
                tvTag.setBackgroundResource(R.drawable.bg_keepgrade_blue)
                vpProgress.setTintColor(R.color.color_025BE8,R.color.color_19025BE8)
                vpProgress.setProgress(100)
            }
        }
    }
}