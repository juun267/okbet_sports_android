package org.cxct.sportlottery.ui.promotion

import android.content.Context
import android.content.Intent
import androidx.core.os.postDelayed
import androidx.core.view.postDelayed
import org.cxct.sportlottery.R
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

    companion object {
        fun start(context: Context, data: ActivityImageList) {
            context.startActivity(Intent(context, PromotionDetailActivity::class.java).apply {
                putExtra("ActivityImageList", data)
            })
        }
    }

    private val activityData: ActivityImageList? by lazy { intent?.getParcelableExtra("ActivityImageList") }

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF, true)
        binding.customToolBar.setOnBackPressListener {
            onBackPressed()
        }
        activityData?.let {
            setPromotion(activityData!!)
            LogUtil.toJson(activityData)
            if (!it.activityId.isNullOrEmpty()){
                viewModel.activityDetailH5(it.activityId)
            }else if(it.activityType==4||it.activityType==6){
                if (LoginRepository.isLogined()) {
                    viewModel.getDailyConfig()
                }
            }
        }
        viewModel.activityDetail.observe(this) {
             setActivity(it)
        }
        viewModel.activityApply.observe(this) {
            when(viewModel.activityDetail.value?.activityType){
                3,5->{}
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
                    (AppManager.currentActivity() as? MainTabActivity)?.checkRechargeKYCVerify()
                }
            }
            binding.tvDepositName.text = getString(R.string.P277)
            binding.tvRewardName.text = getString(R.string.P278)
            binding.tvApply.text = getString(R.string.J285)
            if (dailyConfig?.first == 1) {
                binding.tvDeposit.text = "${dailyConfig.additional}%"
                binding.tvDeposit.setTextColor(getColor(R.color.color_0D2245))
                binding.tvReward.text = "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(dailyConfig.capped,0)}"
                binding.tvReward.setTextColor(getColor(R.color.color_0D2245))
            } else {
                binding.tvDeposit.text = "${activityData?.amount?.toInt()}%"
                binding.tvReward.text = "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(activityData?.reward?:0,0)}"
            }
        }
    }

    fun setPromotion(activityData: ActivityImageList)=binding.run {
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
                1 -> getString(R.string.H019)
                2 -> getString(R.string.title_deposit_money)//充值活动
                3 -> getString(R.string.P225)//充值活动
                4,6 -> getString(R.string.P277)//充值活动
                5 -> getString(R.string.N713)//盈利金额
                else -> getString(R.string.deposits)//亏损金额
            }
            tvReward.text = TextUtil.formatMoney(activityDetail.reward)
            tvRewardName.text = when (activityDetail.activityType) {
                4,6 -> getString(R.string.P278)
                else -> getString(R.string.P156)//亏损金额
            }
            linHistory.show()
            linHistory.setOnClickListener {
               RewardHistoryDialog(activityDetail.activityId).show(supportFragmentManager,null)
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