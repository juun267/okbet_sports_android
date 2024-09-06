package org.cxct.sportlottery.ui.profileCenter.vip

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityMyVipDetailBinding
import org.cxct.sportlottery.net.user.data.UserVip
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LogUtil
import java.math.RoundingMode

class MyVipDetailActivity: BaseActivity<VipViewModel,ActivityMyVipDetailBinding>() {
    override fun pageName() = "VIP详情页面"
    override fun onInitView() {
        setStatusbar()
        binding.customToolBar.setOnBackPressListener {
            finish()
        }
        binding.vpProgress.setBlueStyle()
       viewModel.userVipEvent.observe(this){
           hideLoading()
           setStatus(it)
        }
      loading()
      viewModel.getUserVip()
    }

    fun setStatus(userVip: UserVip?)=binding.run{
        when{
            userVip==null || userVip.protectionLevelGrowthValue==0L-> {
                linContent.gone()
                linEmpty.visible()
                linStatus.setBackgroundResource(R.drawable.bg_white_radius_12)
                tvTag.setBackgroundResource(R.drawable.bg_keepgrade_gray)
            }
            userVip.protectionStatus==1-> {
                linContent.visible()
                linEmpty.gone()
                tvAmount.gone()
                tvContent.text = getString(R.string.P397)
                linStatus.setBackgroundResource(R.drawable.bg_vipdetails_blue)
                tvTag.setBackgroundResource(R.drawable.bg_keepgrade_blue)
                vpProgress.setTintColor(R.color.color_025BE8,R.color.color_19025BE8)
                vpProgress.setProgress2(100.0)
            }
            else-> {
                linContent.visible()
                linEmpty.gone()
                tvAmount.visible()
                tvAmount.text = (userVip.protectionLevelGrowthValue - userVip.exp).coerceAtLeast(0).toString()
                tvContent.text = getString(R.string.P396)
                linStatus.setBackgroundResource(R.drawable.bg_vipdetails_orange)
                tvTag.setBackgroundResource(R.drawable.bg_keepgrade_orange)
                var progress = ArithUtil.div(ArithUtil.mul(userVip.exp.toDouble(),100.0), userVip.protectionLevelGrowthValue.toDouble(),1,RoundingMode.DOWN)
                    .coerceAtMost(100.0)
                vpProgress.setProgress2(progress)
            }
        }
    }
}