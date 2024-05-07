package org.cxct.sportlottery.ui.profileCenter.vip

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.circleOf
import org.cxct.sportlottery.common.extentions.fitsSystemStatus
import org.cxct.sportlottery.common.extentions.postDelayed
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.ActivityVipBenefitsBinding
import org.cxct.sportlottery.net.user.data.RewardInfo
import org.cxct.sportlottery.net.user.data.UserVip
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.DelayRunable
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LeftLinearSnapHelper
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.drawable.shape.ShapeGradientOrientation

class VipBenefitsActivity: BaseActivity<VipViewModel, ActivityVipBenefitsBinding>() {

    private val vipCardAdapter = VipCardAdapter()
    private val loadingHolder by lazy { Gloading.cover(binding.vLoading) }

    override fun onInitView() = binding.run {
        setStatusBarDarkFont(false)
        initVipCard()
        initBtnStyle()
        initActivatedBenefits()
        initUnactivatedBenefits()
        bindUserInfo()

        loadingHolder.withRetry{ viewModel.getUserVip() }
        loadingHolder.go()
        postDelayed(3000) { loadingHolder.showLoadSuccess() }
    }

    override fun onInitData() {
        viewModel.userVipEvent.observe(this) {
            val userVip = it.getData()
            if (userVip == null) {
                loadingHolder.showLoadFailed()
                return@observe
            }

            val currentLevelCode = userVip.levelCode
            val rewardList = userVip.rewardInfo
            if (currentLevelCode == null || rewardList.isEmpty()) {
                loadingHolder.showLoadFailed()
                return@observe
            }

            vipCardAdapter.setUpdate(currentLevelCode, userVip.exp, userVip.upgradeExp.toInt(), rewardList)
            loadingHolder.showLoadSuccess()
        }
    }

    private fun initBtnStyle() = binding.run {
        content.fitsSystemStatus()
        ivBack.setOnClickListener { finish() }
        tvGrowth.setOnClickListener { startActivity<MyVipDetailActivity>() }

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
        frDetail.setOnClickListener {
            startActivity<MyVipDetailActivity>()
        }
    }

    private fun initVipCard() = binding.run {
        rcvVipCard.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        rcvVipCard.adapter = vipCardAdapter
        LeftLinearSnapHelper().attachToRecyclerView(rcvVipCard)
        rcvVipCard.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    tabChangedAction.doOnDelay(150)
                }
            }
        })
    }

    private val tabChangedAction by lazy { DelayRunable(this@VipBenefitsActivity) { onTabChange() } }

    private fun onTabChange() {
        val selectedPosition = (binding.rcvVipCard.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val item: RewardInfo = vipCardAdapter.getItemOrNull(selectedPosition) ?: return
    }

    private fun initActivatedBenefits() = binding.run {
        rcvActivatedBenefits.setLinearLayoutManager()
        rcvActivatedBenefits.adapter = ActivatedBenefitsAdapter(this@VipBenefitsActivity)
    }

    private fun initUnactivatedBenefits() = binding.run {
        rcvUnactivatedBenefits.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        rcvUnactivatedBenefits.adapter = UnactivatedBenefitsAdapter()
    }

    private fun bindUserInfo() = binding.run {
        ivProfile.circleOf(UserInfoRepository.loginedInfo()?.iconUrl, R.drawable.ic_person_avatar)
        tvUserName.text = UserInfoRepository.nickName()
        tvId.text = "ID:${UserInfoRepository.userName()}"
    }
}