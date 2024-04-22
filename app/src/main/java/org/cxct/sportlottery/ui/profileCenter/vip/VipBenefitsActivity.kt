package org.cxct.sportlottery.ui.profileCenter.vip

import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.common.extentions.circleOf
import org.cxct.sportlottery.common.extentions.fitsSystemStatus
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.ActivityVipBenefitsBinding
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LeftLinearSnapHelper
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.drawable.shape.ShapeGradientOrientation

class VipBenefitsActivity: BaseActivity<MainHomeViewModel, ActivityVipBenefitsBinding>() {

    override fun onInitView() = binding.run {
        setStatusBarDarkFont(false)
        initVipCard()
        initBtnStyle()
        initActivatedBenefits()
        initUnactivatedBenefits()
    }

    private fun initBtnStyle() = binding.run {
        content.fitsSystemStatus()
        ivBack.setOnClickListener { finish() }
        ivProfile.circleOf(UserInfoRepository.loginedInfo()?.iconUrl, R.drawable.ic_person_avatar)
        llBottom.background = ShapeDrawable().setSolidColor(getColor(R.color.color_F8F9FD)).setRadius(24.dp.toFloat(), 0f, 0f, 0f)
        val dp37 = 37.dp.toFloat()
        tvGrowth.background = ShapeDrawable().setSolidColor(getColor(R.color.color_ff541b), getColor(R.color.color_e91217))
            .setRadius(dp37, 0F, dp37, 0F)
            .setSolidGradientOrientation(ShapeGradientOrientation.LEFT_TO_RIGHT)
        frDetail.background = ShapeDrawable()
            .setShadowSize(1.dp)
            .setShadowOffsetX(1.dp)
            .setShadowOffsetY(1.dp)
            .setSolidColor(getColor(R.color.color_5182FF), getColor(R.color.color_0029FF))
            .setSolidGradientOrientation(ShapeGradientOrientation.LEFT_TO_RIGHT)
            .setRadius(8.dp.toFloat())
    }

    private fun initVipCard() = binding.run {
        rcvVipCard.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        val adapter = VipCardAdapter()
        adapter.setNewInstance(mutableListOf("1", "2", "3", "4", "5", "6", "7", "8", "9"))
        LeftLinearSnapHelper().attachToRecyclerView(rcvVipCard)
        rcvVipCard.adapter = adapter
    }

    private fun initActivatedBenefits() = binding.run {
        rcvActivatedBenefits.setLinearLayoutManager()
        rcvActivatedBenefits.adapter = ActivatedBenefitsAdapter()
    }

    private fun initUnactivatedBenefits() = binding.run {
        rcvUnactivatedBenefits.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        rcvUnactivatedBenefits.adapter = UnactivatedBenefitsAdapter()
    }
}