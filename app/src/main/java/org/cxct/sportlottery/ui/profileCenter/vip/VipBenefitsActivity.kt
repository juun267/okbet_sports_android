package org.cxct.sportlottery.ui.profileCenter.vip

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.UserVipType.setLevelTagIcon
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.ActivityVipBenefitsBinding
import org.cxct.sportlottery.databinding.ItemActivatedBenefitsBinding
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.net.user.data.UserVip
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LeftLinearSnapHelper
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.drawable.shape.ShapeGradientOrientation

class VipBenefitsActivity: BaseActivity<VipViewModel, ActivityVipBenefitsBinding>() {

    val loadingHolder by lazy { Gloading.cover(binding.vLoading) }
    private val vipCardAdapter = VipCardAdapter()
    private val activatedAdapter = ActivatedBenefitsAdapter()
    private val unActivatedAdapter = UnactivatedBenefitsAdapter()

    override fun onInitView() = binding.run {
        setStatusBarDarkFont(false)
        initVipCard()
        initBtnStyle()
        initActivatedBenefits()
        initUnactivatedBenefits()
        initObservable()
    }

    private fun initObservable() {
        viewModel.userVipEvent.observe(this){
            if (it!=null) loadingHolder.showLoadSuccess() else loadingHolder.showLoadFailed()
            it?.let {
                setUpVipCard(it)
            }
        }
    }

    private fun initBtnStyle() = binding.run {
        content.fitsSystemStatus()
        ivBack.setOnClickListener { finish() }
        tvGrowth.setOnClickListener { startActivity<MyVipDetailActivity>() }
        UserInfoRepository.loginedInfo()?.let {
            ivProfile.circleOf(it.iconUrl, R.drawable.ic_person_avatar)
            tvUserName.text = if (it.nickName.isNullOrEmpty()) {
                it.userName
            } else {
                it.nickName
            }
            ivLVTips.setLevelTagIcon(it.levelCode)
            tvId.text = "ID：${it?.userId}"
        }
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
            JumpUtil.toInternalWeb(this@VipBenefitsActivity,
                Constants.getVipRuleUrl(this@VipBenefitsActivity),
                getString(R.string.P372)
            )
        }
    }

    private fun initVipCard() = binding.run {
        rcvVipCard.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        LeftLinearSnapHelper().attachToRecyclerView(rcvVipCard)
        rcvVipCard.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    val currentPosition = (rcvVipCard.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                    onSelectLevel(currentPosition)
                }
            }
        })
        UserRepository.userVip?.let{ it-> setUpVipCard(it) }
        rcvVipCard.adapter = vipCardAdapter
    }

    private fun initActivatedBenefits() = binding.run {
        rcvActivatedBenefits.setLinearLayoutManager()
        rcvActivatedBenefits.adapter = activatedAdapter
    }

    private fun initUnactivatedBenefits() = binding.run {
        rcvUnactivatedBenefits.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        rcvUnactivatedBenefits.adapter = unActivatedAdapter
    }
    private fun setUpVipCard(userVip: UserVip){
        vipCardAdapter.userExp = userVip.exp
        vipCardAdapter.setList(userVip.rewardInfo)
        val selectPosition = userVip.rewardInfo.indexOfFirst {userVip.levelCode == it.levelCode }
        if (selectPosition>0){
            onSelectLevel(selectPosition)
        }else{
            onSelectLevel(0)
        }
    }
    private fun onSelectLevel(position: Int){
        UserRepository.userVip?.let {
            activatedAdapter.setList(null)
            activatedAdapter.removeAllFooterView()
            val currentLevel = it.rewardInfo.getOrNull(position)?:return
            val nextLevel = it.rewardInfo.getOrNull(position+1)
            activatedAdapter.setList(currentLevel.rewardDetail.filter { it.enable })
            if (currentLevel.exclusiveService){
                val exclusiveBinding = ItemActivatedBenefitsBinding.inflate(layoutInflater)
                exclusiveBinding.ivBenefits.setImageResource(R.drawable.ic_vip_bonus_support)
                exclusiveBinding.tvBenefitsName.text = getString(R.string.P402)
                exclusiveBinding.tvAmount.text = "24x7"
                exclusiveBinding.tvAction.gone()
                activatedAdapter.addFooterView(exclusiveBinding.root)
            }
            if (currentLevel.expressWithdrawal){
                val withdrawalBinding = ItemActivatedBenefitsBinding.inflate(layoutInflater)
                withdrawalBinding.ivBenefits.setImageResource(R.drawable.ic_vip_bonus_disbursement)
                withdrawalBinding.tvBenefitsName.text = getString(R.string.P404)
                withdrawalBinding.tvAmount.text = getString(R.string.P411)
                withdrawalBinding.tvAction.gone()
                activatedAdapter.addFooterView(withdrawalBinding.root)
            }
            unActivatedAdapter.setList(nextLevel?.rewardDetail?.filter { it.enable })
        }

    }
    private fun reload(){
        loadingHolder.withRetry{
            loadingHolder.showLoading()
            viewModel.getUserVip()
        }
    }
}