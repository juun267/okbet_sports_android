package org.cxct.sportlottery.ui.profileCenter.vip

import android.graphics.Color
import org.cxct.sportlottery.databinding.ActivityVipBenefitsBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable

class VipBenefitsActivity: BaseActivity<BaseViewModel, ActivityVipBenefitsBinding>() {

    override fun onInitView() {

        binding.llBottom.background = ShapeDrawable().setSolidColor(Color.WHITE).setRadius(24.dp.toFloat(), 0f, 0f, 0f)

    }
}