package org.cxct.sportlottery.ui.profileCenter.vip.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.UserVipType.REWARD_TYPE_BIRTHDAY
import org.cxct.sportlottery.common.enums.UserVipType.REWARD_TYPE_PACKET
import org.cxct.sportlottery.common.enums.UserVipType.REWARD_TYPE_PROMOTE
import org.cxct.sportlottery.common.enums.UserVipType.REWARD_TYPE_WEEKLY
import org.cxct.sportlottery.common.enums.UserVipType.setLevelTagIcon
import org.cxct.sportlottery.common.extentions.getColor
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ViewUserVipBinding
import org.cxct.sportlottery.net.user.data.RewardInfo
import org.cxct.sportlottery.net.user.data.UserVip
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterFragment
import org.cxct.sportlottery.ui.profileCenter.vip.VipViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import splitties.systemservices.layoutInflater

class UserVipView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    val binding = ViewUserVipBinding.inflate(layoutInflater,this)
    lateinit var viewModel: VipViewModel

    init {
        binding.vipProgressView.apply {
            setTintColor(R.color.color_FFB828, R.color.color_eed39f)
            val progressTextView = getProgressTextView()
            progressTextView.layoutParams.height = 30.dp
            progressTextView.layoutParams.height = 38.dp
            progressTextView.setTextColor(Color.WHITE)
            progressTextView.gravity = Gravity.CENTER
        }
        setProgress(0)
    }

    fun setup(fragment: ProfileCenterFragment,viewModel: VipViewModel) {
        viewModel.userVipEvent.observe(fragment){
            updateUI(it)
        }
        viewModel.getUserVip()
    }
    private fun updateUI(userVip: UserVip)=binding.run{
        ivCurrentLevel.setLevelTagIcon(userVip.levelCode)
        tvCurrentLevel.text = userVip.levelName
        val nextLevelIndex = userVip.rewardInfo.indexOfFirst { it.levelCode == userVip.levelCode}+1
        val nextRewardInfo = userVip.rewardInfo.getOrNull(nextLevelIndex)
        if (nextLevelIndex>=userVip.rewardInfo.size){
            ivNextLevel.gone()
            tvNextLevel.text = resources.getString(R.string.P439)
        }else{
            ivNextLevel.visible()
            ivNextLevel.setLevelTagIcon(nextRewardInfo?.levelCode)
            tvNextLevel.text = nextRewardInfo?.levelName
        }

        setProgress((userVip.exp*100/userVip.upgradeExp).toInt())
        val currentRewardInfo = userVip.rewardInfo.firstOrNull { it.levelCode == userVip.levelCode }
        currentRewardInfo?.let {
            ivLockPromote.setBenefitEnable(it, REWARD_TYPE_PROMOTE)
            ivLockBirthday.setBenefitEnable(it, REWARD_TYPE_BIRTHDAY)
            ivLockWeekly.setBenefitEnable(it, REWARD_TYPE_WEEKLY)
            ivLockPacket.setBenefitEnable(it, REWARD_TYPE_PACKET)
        }
    }

    private fun setProgress(progress: Int) {
        if (progress < 90) {
            val drawableProgress = context.getDrawable(R.drawable.bg_vip_progress_left)!!.mutate()
            drawableProgress.setTint(getColor(R.color.color_ff871f))
            binding.vipProgressView.setProgress2(progress, 5.dp, drawableProgress)
        } else {
            val drawableProgress = context.getDrawable(R.drawable.bg_vip_progress_right)!!.mutate()
            drawableProgress.setTint(getColor(R.color.color_ff871f))
            binding.vipProgressView.setProgress2(progress, 24.dp, drawableProgress)
        }
    }

    private fun ImageView.setBenefitEnable(rewardInfo: RewardInfo,rewardType: Int){
        val benefitDetail = rewardInfo.rewardDetail.firstOrNull{ it.rewardType==rewardType}
        isVisible = if (benefitDetail==null){
            false
        }else{
            benefitDetail.enable&&benefitDetail.status==7
        }
    }

}