package org.cxct.sportlottery.ui.profileCenter.vip.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import org.cxct.sportlottery.common.enums.UserVipType.REWARD_TYPE_BIRTHDAY
import org.cxct.sportlottery.common.enums.UserVipType.REWARD_TYPE_PACKET
import org.cxct.sportlottery.common.enums.UserVipType.REWARD_TYPE_PROMOTE
import org.cxct.sportlottery.common.enums.UserVipType.REWARD_TYPE_WEEKLY
import org.cxct.sportlottery.common.enums.UserVipType.setLevelTagIcon
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ViewUserVipBinding
import org.cxct.sportlottery.net.user.data.RewardInfo
import org.cxct.sportlottery.net.user.data.UserVip
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterFragment
import org.cxct.sportlottery.ui.profileCenter.vip.VipViewModel
import splitties.systemservices.layoutInflater

class UserVipView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val binding = ViewUserVipBinding.inflate(layoutInflater,this,true)
    lateinit var viewModel: VipViewModel
    init {
        orientation = VERTICAL
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
            tvNextLevel.text = "MAX"
        }else{
            ivNextLevel.visible()
            ivNextLevel.setLevelTagIcon(nextRewardInfo?.levelCode)
            tvNextLevel.text = nextRewardInfo?.levelName
        }
        binding.vipProgressView.setProgress((userVip.exp*100/userVip.upgradeExp).toInt())
        val currentRewardInfo = userVip.rewardInfo.firstOrNull { it.levelCode == userVip.levelCode }
        currentRewardInfo?.let {
            ivLockPromote.setBenefitEnable(it, REWARD_TYPE_PROMOTE)
            ivLockBirthday.setBenefitEnable(it, REWARD_TYPE_BIRTHDAY)
            ivLockWeekly.setBenefitEnable(it, REWARD_TYPE_WEEKLY)
            ivLockPacket.setBenefitEnable(it, REWARD_TYPE_PACKET)
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