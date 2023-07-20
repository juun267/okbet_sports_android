package org.cxct.sportlottery.ui.promotion

import android.content.Context
import android.content.Intent
import android.text.Html
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_promotion_detail.*
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
import java.util.*


class PromotionDetailActivity :
    BindingActivity<MainHomeViewModel, ActivityPromotionDetailBinding>() {

    companion object {
        fun start(context: Context, data: ActivityImageList) {
            context.startActivity(Intent(context, PromotionDetailActivity::class.java).apply {
                putExtra("ActivityImageList", data)
            })
        }

        fun start(context: Context, activityImageId: String) {
            context.startActivity(Intent(context, PromotionDetailActivity::class.java).apply {
                putExtra("activityImageId", activityImageId)
            })
        }
    }

    private val activityData: ActivityImageList? by lazy { intent?.getParcelableExtra("ActivityImageList") }
    private val activityId: String? by lazy { intent?.getStringExtra("activityImageId") }

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF, true)
        binding.customToolBar.setOnBackPressListener {
            onBackPressed()
        }
        if (activityData != null) {
            setup(activityData!!)
        } else if (!activityId.isNullOrEmpty()) {
            viewModel.getActivityImageListH5()
        }
        viewModel.activityImageList.observe(this) {
            it.firstOrNull { it.activityId == activityId }?.let {
                setup(it)
            }
        }
        viewModel.activityApply.observe(this) {
            tvDeposit.text = TextUtil.formatMoney(0)
            tvReward.text = TextUtil.formatMoney(0)
            linApply.isEnabled = false
            linApply.setBackgroundResource(R.drawable.bg_gray_radius_8)
            PromotionSuccessDialog.newInstance().show(supportFragmentManager,null)
        }
    }

    fun setup(activityData: ActivityImageList) {
        if (activityData.contentImage.isNullOrEmpty()){
            binding.ivBanner.gone()
        }else{
            binding.ivBanner.show()
            binding.ivBanner.load(sConfigData?.resServerHost+activityData.contentImage,R.drawable.img_banner01)
        }
        binding.tvSubTitle.text = activityData.subTitleText
        val startTime = TimeUtil.timeFormat(activityData.startTime, TimeUtil.EN_DATE_FORMAT, locale = Locale.ENGLISH)
        val endTime = TimeUtil.timeFormat(activityData.endTime, TimeUtil.EN_DATE_FORMAT, locale = Locale.ENGLISH)
        binding.tvTime.text = "$startTime ${getString(R.string.J645)} $endTime"
        if (activityData.activityId.isNullOrEmpty()) {
            binding.linActivity.gone()
        } else {
            binding.linActivity.show()
            binding.tvDeposit.text = TextUtil.formatMoney(activityData.amount)
            if (activityData.activityType == 1) {
                tvReward.text = getString(R.string.H019)
            } else {
                tvReward.text = getString(R.string.deposits)
            }
            tvReward.text = TextUtil.formatMoney(activityData.reward)
            if (activityData.reward == 0) {
                linApply.isEnabled = false
                linApply.setBackgroundResource(R.drawable.bg_gray_radius_8)
            } else {
                linApply.isEnabled = true
                linApply.setBackgroundResource(R.drawable.bg_blue_radius_8)
                linApply.setOnClickListener {
                    viewModel.activityApply(activityData.activityId)
                }
            }
        }
        binding.okWebView.setBackgroundColor(ContextCompat.getColor(this,R.color.color_F9FAFD))
        binding.okWebView.loadDataWithBaseURL(null,(activityData.contentText?:"").formatHTML(), "text/html", "utf-8",null)
    }
}