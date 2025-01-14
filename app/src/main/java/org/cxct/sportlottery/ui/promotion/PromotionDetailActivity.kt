package org.cxct.sportlottery.ui.promotion

import android.content.Context
import android.content.Intent
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.appevent.SensorsEventUtil
import org.cxct.sportlottery.common.enums.ActivityType
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityPromotionDetailBinding
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.dialog.PromotionSuccessDialog
import java.util.*


class PromotionDetailActivity :
    BaseActivity<MainHomeViewModel, ActivityPromotionDetailBinding>() {
    override fun pageName() = "优惠活动详情页面"
    companion object {
        fun start(context: Context, data: ActivityImageList, fromWhere: String, fromPage: String? = null) {
            SensorsEventUtil.activityPageVisitEvent(fromWhere,
                fromPage ?: SensorsEventUtil.getPageName(),
                data.typeName,
                "${data.titleText}")
            context.startActivity(Intent(context, PromotionDetailActivity::class.java).apply {
                putExtra("ActivityImageList", data)
            })
        }
    }

    private val activityData: ActivityImageList by lazy { intent?.getParcelableExtra("ActivityImageList")!! }

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF, true)
        binding.customToolBar.setOnBackPressListener {
            onBackPressed()
        }
        activityData?.let {
            setPromotion(it)
            if (!it.activityId.isNullOrEmpty()){
                viewModel.activityDetailH5(it.activityId)
            }else if(it.activityType==ActivityType.FIRST_DEPOSIT_BONUS||it.activityType==ActivityType.DAILY_BONUS){
                if (LoginRepository.isLogined()) {
                    viewModel.getDailyConfig()
                }
            }
        }
        viewModel.activityDetail.observe(this) {
            it?.let { it1 -> setActivity(it1) }
        }
        viewModel.activityApply.observe(this) {
            when(viewModel.activityDetail.value?.activityType){
                ActivityType.LOSE,ActivityType.PROFIT->{}
                else->{
                    binding.tvDeposit.text = TextUtil.formatMoney(0)
                }
            }
            binding.tvReward.text = TextUtil.formatMoney(0)
            binding.linApply.isEnabled = false
            binding.linApply.setBackgroundResource(R.drawable.bg_gray_radius_8)
            PromotionSuccessDialog.newInstance().show(supportFragmentManager)
        }
        viewModel.dailyConfigEvent.observe(this){
            val dailyConfig = it.firstOrNull { it.activityType == activityData?.activityType }
            binding.linActivity.show()
            binding.linApply.isEnabled = true
            binding.linApply.setBackgroundResource(R.drawable.bg_blue_radius_8)
            binding.linApply.setOnClickListener {
                MainTabActivity.reStart(this)
                binding.linApply.postDelayed(1000){
                    (AppManager.currentActivity() as? MainTabActivity)?.jumpToDeposit("申请按钮")
                }
            }
            binding.tvDepositName.text = getString(R.string.P277)
            binding.tvRewardName.text = getString(R.string.P278)
            binding.tvApply.text = getString(R.string.J285)
            if (dailyConfig?.first == 1) {
                binding.tvDeposit.text = "${TextUtil.formatMoney2(dailyConfig.additional)}%"
                binding.tvDeposit.setTextColor(getColor(R.color.color_0D2245))
                binding.tvReward.text = "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(dailyConfig.capped,0)}"
                binding.tvReward.setTextColor(getColor(R.color.color_0D2245))
            } else {
                binding.tvDeposit.text = "${activityData?.amount?.toInt()}%"
                binding.tvReward.text = "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(activityData?.reward?:0,0)}"
            }
        }
    }

    private fun setPromotion(activityData: ActivityImageList)=binding.run {
        if (activityData.contentImage.isNullOrEmpty()){
            ivBanner.gone()
        }else{
            ivBanner.show()
            ivBanner.load(sConfigData?.resServerHost+activityData.contentImage,R.drawable.img_banner01)
        }
        tvSubTitle.text = activityData.subTitleText
        val startTime = TimeUtil.timeFormat(activityData.startTime, TimeUtil.EN_DATE_FORMAT, locale = Locale.ENGLISH)
        val endTime = TimeUtil.timeFormat(activityData.endTime, TimeUtil.EN_DATE_FORMAT, locale = Locale.ENGLISH)
        tvTime.text = "${getString(R.string.N473)}: $startTime ${getString(R.string.J645)} $endTime"
        okWebView.loadDataWithBaseURL(null,(activityData.contentText?:"").formatHTML(), "text/html", "utf-8",null)
    }
    private fun setActivity(activityDetail: ActivityImageList)=binding.run {
        val curTimestamp = System.currentTimeMillis()
        //根据后台活动显示时间来判断是否显示活动
        val timeAvailable=curTimestamp>activityDetail.startTime&&(curTimestamp<activityDetail.endTime||activityDetail.endTime==0L)
        if (activityDetail.activityId.isNullOrEmpty()||!timeAvailable) {
            linActivity.gone()
        } else {
            linActivity.show()
            tvDeposit.text = TextUtil.formatMoney(activityDetail.amount)
            tvDepositName.text = when (activityDetail.activityType) {
                ActivityType.BET -> getString(R.string.H019)
                ActivityType.DEPOSIT -> getString(R.string.title_deposit_money)//充值活动
                ActivityType.LOSE -> getString(R.string.P225)//充值活动
                ActivityType.FIRST_DEPOSIT_BONUS,ActivityType.DAILY_BONUS -> getString(R.string.P277)//充值活动
                ActivityType.PROFIT -> getString(R.string.N713)//盈利金额
                else -> getString(R.string.deposits)//亏损金额
            }
            tvReward.text = TextUtil.formatMoney(activityDetail.reward)
            tvRewardName.text = when (activityDetail.activityType) {
                ActivityType.FIRST_DEPOSIT_BONUS,ActivityType.DAILY_BONUS -> getString(R.string.P278)
                else -> getString(R.string.P156)//亏损金额
            }
            //发薪日活动Payday 隐藏充值
            linDeposit.isVisible = activityDetail.isPayDay !=1
            linHistory.show()
            linHistory.setOnClickListener {
               RewardHistoryDialog.newInstance(activityDetail.activityId).show(supportFragmentManager,null)
            }
            if (activityDetail.reward == 0.0) {
                linApply.isEnabled = false
                linApply.setBackgroundResource(R.drawable.bg_gray_radius_8)
            } else {
                linApply.isEnabled = true
                linApply.setBackgroundResource(R.drawable.bg_blue_radius_8)
                linApply.setOnClickListener {
                    viewModel.activityApply(activityDetail.activityId)
                }
            }
        }
    }

}