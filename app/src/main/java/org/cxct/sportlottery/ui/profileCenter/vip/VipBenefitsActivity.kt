package org.cxct.sportlottery.ui.profileCenter.vip

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bigkoo.pickerview.listener.CustomListener
import com.bigkoo.pickerview.view.TimePickerView
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_vip_benefits.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.UserVipType
import org.cxct.sportlottery.common.enums.UserVipType.setLevelTagIcon
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.ActivityVipBenefitsBinding
import org.cxct.sportlottery.databinding.ItemActivatedBenefitsBinding
import org.cxct.sportlottery.databinding.ItemUnactivatedBenefitsBinding
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.net.user.data.RewardDetail
import org.cxct.sportlottery.net.user.data.RewardInfo
import org.cxct.sportlottery.net.user.data.UserVip
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.signUp.info.DateTimePickerOptions
import org.cxct.sportlottery.ui.profileCenter.profile.Uide
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LeftLinearSnapHelper
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.drawable.shape.ShapeGradientOrientation
import java.util.*

class VipBenefitsActivity: BaseActivity<VipViewModel, ActivityVipBenefitsBinding>() {

    val loadingHolder by lazy { Gloading.cover(binding.vLoading) }
    private val vipCardAdapter = VipCardAdapter()
    private val activatedAdapter = ActivatedBenefitsAdapter(::onItemClick)
    private val unActivatedAdapter = UnactivatedBenefitsAdapter()
    //生日选择
    private var dateTimePicker: TimePickerView? = null
    private var userRewardInfo: RewardInfo?=null

    override fun onInitView() = binding.run {
        setStatusBarDarkFont(false)
        initVipCard()
        initBtnStyle()
        initActivatedBenefits()
        initUnactivatedBenefits()
        initObservable()
        viewModel.getVipDetail()
    }

    private fun initObservable() {
        viewModel.userVipEvent.observe(this){
            if (it!=null) loadingHolder.showLoadSuccess() else loadingHolder.showLoadFailed()
            it?.let {
                setUpVipCard(it)
            }
        }
        viewModel.vipDetailEvent.observe(this){

        }
        viewModel.setBirthdayEvent.observe(this){
            hideLoading()
            if (it.succeeded()) {
                toast(getString(R.string.J533))
                reload()
            }else{
                toast(it.msg)
            }
        }
        viewModel.vipRewardEvent.observe(this){
            hideLoading()
            if (it.succeeded()){
                toast(getString(R.string.P454))
                reload()
            }else{
                toast(it.msg)
            }
        }
        viewModel.applyResultEvent.observe(this){
            hideLoading()
            if (it.succeeded()){
                toast(getString(R.string.P453))
                reload()
            }else{
                toast(it.msg)
            }
        }
    }

    private fun initBtnStyle() = binding.run {
        content.fitsSystemStatus()
        ivBack.setOnClickListener { finish() }
        tvGrowth.setOnClickListener { startActivity<MyVipDetailActivity>() }
        UserInfoRepository.loginedInfo()?.let {
            ivProfile.circleOf(it.iconUrl, R.drawable.ic_person_avatar)
            tvNickName.text = if (it.nickName.isNullOrEmpty()) it.userName else it.nickName
            ivLVTips.setLevelTagIcon(it.levelCode)
            tvUserName.text = "${getString(R.string.username)}：${it?.userName}"
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
            .setSolidGradientOrientation(ShapeGradientOrientation.TOP_TO_BOTTOM)
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
        val leftLinearSnapHelper = LeftLinearSnapHelper()
        leftLinearSnapHelper.attachToRecyclerView(rcvVipCard)
        rcvVipCard.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    leftLinearSnapHelper.findSnapView(rcvVipCard.layoutManager as LinearLayoutManager)?.let {
                        val position = (rcvVipCard.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                        if(position>=0){
                            onSelectLevel(position)
                        }
                    }
                }
            }
        })
        if (viewModel.userVipEvent.value==null){
            viewModel.getUserVip()
        }else{
            viewModel.userVipEvent.value?.let{ it-> setUpVipCard(it) }
        }
        rcvVipCard.adapter = vipCardAdapter
    }

    private fun initActivatedBenefits() = binding.run {
        rcvActivatedBenefits.setLinearLayoutManager()
        rcvActivatedBenefits.adapter = activatedAdapter
    }

    private fun initUnactivatedBenefits() = binding.run {
        rcvUnactivatedBenefits.layoutManager = GridLayoutManager(this@VipBenefitsActivity,3)
        rcvUnactivatedBenefits.addItemDecoration(GridSpacingItemDecoration(3, 8.dp,false))
        rcvUnactivatedBenefits.adapter = unActivatedAdapter
    }
    private fun setUpVipCard(userVip: UserVip){
        LogUtil.toJson(userVip)
        ivLVTips.setLevelTagIcon(userVip.levelCode)
        vipCardAdapter.userVip = userVip
        vipCardAdapter.setList(userVip.rewardInfo)
        val selectPosition = userVip.rewardInfo.indexOfFirst {userVip.levelCode == it.levelCode }
        userRewardInfo = userVip.rewardInfo.getOrNull(selectPosition)
        binding.rcvVipCard.scrollToPosition(selectPosition)
        if (selectPosition>0){
            onSelectLevel(selectPosition)
        }else{
            onSelectLevel(0)
        }
    }
    private fun onSelectLevel(position: Int){
        viewModel.userVipEvent?.value?.let {
            val currentRewardInfo = it.rewardInfo.getOrNull(position)?:return@let
            activatedAdapter.setList(null)
            activatedAdapter.userVip = it
            activatedAdapter.rewardInfo = currentRewardInfo
            activatedAdapter.setList(currentRewardInfo?.rewardDetail?.filter { it.enable }?.toMutableList())
            if (currentRewardInfo.exclusiveService){
                activatedAdapter.addData(RewardDetail(otherType = 1))
            }
            if (currentRewardInfo.expressWithdrawal){
                activatedAdapter.addData(RewardDetail(otherType = 2))
            }
            binding.includeActivatedEmpty.root.isVisible = activatedAdapter.itemCount==0
            val nextLevel = it.rewardInfo.getOrNull(position+1)
            if (nextLevel!=null){
                unActivatedAdapter.setList(nextLevel?.rewardDetail?.filter { it.enable })
                if (nextLevel.exclusiveService){
                    unActivatedAdapter.addData(RewardDetail(otherType = 1))
                }
                if (nextLevel.expressWithdrawal){
                    unActivatedAdapter.addData(RewardDetail(otherType = 2))
                }
            }else{
                unActivatedAdapter.setList(null)
            }
            linUnactivated.isVisible = unActivatedAdapter.itemCount!=0
        }
    }
    fun onItemClick(rewardInfo: RewardInfo, rewardDetail: RewardDetail){
        when(rewardDetail.rewardType){
            UserVipType.REWARD_TYPE_PROMOTE->{
                getReward(rewardInfo, rewardDetail)
            }
            UserVipType.REWARD_TYPE_WEEKLY->{
                getReward(rewardInfo, rewardDetail)
            }
            UserVipType.REWARD_TYPE_BIRTHDAY->{
                if (viewModel.userVipEvent.value?.birthday.isNullOrEmpty()){
                    showBirthday()
                }else{
                    getReward(rewardInfo,rewardDetail)
                }
            }
            UserVipType.REWARD_TYPE_PACKET->{
                loading()
                viewModel.vipRedenpApply(rewardInfo.levelV2Id)
            }
        }
    }
    private fun getReward(rewardInfo: RewardInfo, rewardDetail: RewardDetail){
        val activityId = viewModel.vipDetailEvent.value?.vipUserLevelLimits?.firstOrNull { it.type == rewardDetail.rewardType }?.activityId
        val levelV2Id = rewardInfo.levelV2Id
        if (activityId!=null && levelV2Id!=null){
            loading()
            viewModel.vipReward(activityId,rewardDetail.rewardType, levelV2Id)
        }
    }

    private fun reload(){
        loadingHolder.showLoading()
        viewModel.getUserVip()
    }
    fun showBirthday(){
        if (dateTimePicker==null){
            val yesterday = Calendar.getInstance()
            yesterday.add(Calendar.YEAR, -100)
            val tomorrow = Calendar.getInstance()
            tomorrow.add(Calendar.YEAR, -21)
            tomorrow.add(Calendar.DAY_OF_MONTH, -1)
            dateTimePicker = DateTimePickerOptions(this).getBuilder { date, _ ->
                BirthdayConfirmDialog.newInstance(date).show(supportFragmentManager)
            }
                .setLayoutRes(R.layout.dialog_date_select, object : CustomListener {
                    override fun customLayout(v: View) {
                        //自定义布局中的控件初始化及事件处理
                        v.findViewById<View>(R.id.btnBtmCancel).setOnClickListener {
                            dateTimePicker?.dismiss()
                        }
                        v.findViewById<View>(R.id.btnBtmDone).setOnClickListener {
                            dateTimePicker?.returnData()
                            dateTimePicker?.dismiss()
                        }

                    }
                })
                .setItemVisibleCount(6)
                .setLineSpacingMultiplier(2.0f)
                .setRangDate(yesterday, tomorrow)
                .setDate(tomorrow)
                .build()
        }
        dateTimePicker?.show()
    }
    fun setBirthday(date: Date){
        TimeUtil.dateToStringFormatYMD(date)?.let {
            loading()
            viewModel.setBirthday(it)
        }
    }
}