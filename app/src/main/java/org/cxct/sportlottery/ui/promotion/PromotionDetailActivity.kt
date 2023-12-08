package org.cxct.sportlottery.ui.promotion

import android.content.Context
import android.content.Intent
import android.text.Html
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.ActivityPromotionDetailBinding
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.dialog.PromotionSuccessDialog
import org.koin.android.ext.android.bind
import java.util.*


class PromotionDetailActivity :
    BindingActivity<MainHomeViewModel, ActivityPromotionDetailBinding>() {

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
            it.activityId?.let {
                viewModel.activityDetailH5(it)
            }
        }
        viewModel.activityDetail.observe(this) {
             setActivity(it)
        }
        viewModel.activityApply.observe(this) {
            if (viewModel.activityDetail.value?.activityType!=3){
                binding.tvDeposit.text = TextUtil.formatMoney(0)
            }
            binding.tvReward.text = TextUtil.formatMoney(0)
            binding.linApply.isEnabled = false
            binding.linApply.setBackgroundResource(R.drawable.bg_gray_radius_8)
            PromotionSuccessDialog.newInstance().show(supportFragmentManager,null)
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
        tvTime.text = "$startTime ${getString(R.string.J645)} $endTime"
        okWebView.setBackgroundColor(ContextCompat.getColor(this@PromotionDetailActivity,R.color.color_F9FAFD))
        okWebView.loadDataWithBaseURL(null,(activityData.contentText?:"").formatHTML(), "text/html", "utf-8",null)
    }
    private fun setActivity(activityDetail: ActivityImageList)=binding.run {
        if (activityDetail.activityId.isNullOrEmpty()) {
            linActivity.gone()
        } else {
            linActivity.show()
            tvDeposit.text = TextUtil.formatMoney(activityDetail.amount)
            tvDepositName.text = when (activityDetail.activityType) {
                1 -> getString(R.string.H019)
                2 -> getString(R.string.title_deposit_money)//充值活动
                3 -> getString(R.string.P225)//充值活动
                else -> getString(R.string.deposits)//亏损金额
            }
            tvReward.text = TextUtil.formatMoney(activityDetail.reward)
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