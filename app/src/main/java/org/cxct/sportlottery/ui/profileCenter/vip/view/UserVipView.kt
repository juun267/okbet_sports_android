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
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.UserVipType.REWARD_TYPE_BIRTHDAY
import org.cxct.sportlottery.common.enums.UserVipType.REWARD_TYPE_PACKET
import org.cxct.sportlottery.common.enums.UserVipType.REWARD_TYPE_PROMOTE
import org.cxct.sportlottery.common.enums.UserVipType.REWARD_TYPE_WEEKLY
import org.cxct.sportlottery.common.enums.UserVipType.setLevelIcon
import org.cxct.sportlottery.common.enums.UserVipType.setLevelTagIcon
import org.cxct.sportlottery.common.extentions.getColor
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ViewUserVipBinding
import org.cxct.sportlottery.net.user.data.RewardInfo
import org.cxct.sportlottery.net.user.data.UserVip
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterFragment
import org.cxct.sportlottery.ui.profileCenter.vip.UserVipRewardAdapter
import org.cxct.sportlottery.ui.profileCenter.vip.VipViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import splitties.systemservices.layoutInflater

class UserVipView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    val binding = ViewUserVipBinding.inflate(layoutInflater,this)
    lateinit var viewModel: VipViewModel
    private val rewardAdapter by lazy { UserVipRewardAdapter() }

    init {
        binding.vipProgressView.setYellowStyle()
        setProgress(0.0)
    }

    fun setup(fragment: ProfileCenterFragment,viewModel: VipViewModel) {
        viewModel.userVipEvent.observe(fragment){
            it?.let { it1 -> updateUI(it1) }
        }
    }
    private fun updateUI(userVip: UserVip)=binding.run{
        ivCurrentLevel.setLevelIcon(userVip.levelCode)
        tvCurrentLevel.text = userVip.levelName
        val nextLevelIndex = userVip.rewardInfo.indexOfFirst { it.levelCode == userVip.levelCode}+1
        val nextRewardInfo = userVip.rewardInfo.getOrNull(nextLevelIndex)
        if (nextLevelIndex>=userVip.rewardInfo.size){
            ivNextLevel.gone()
            tvNextLevel.text = "Max"
        }else{
            ivNextLevel.visible()
            ivNextLevel.setLevelIcon(nextRewardInfo?.levelCode)
            tvNextLevel.text = nextRewardInfo?.levelName
        }
        setProgress((userVip.getExpPercent()))
        val currentRewardInfo = userVip.rewardInfo.firstOrNull { it.levelCode == userVip.levelCode }
        val items = currentRewardInfo?.rewardDetail?.sortedByDescending { it.enable }?: listOf()
        if (rvReward.adapter==null){
            rvReward.setLinearLayoutManager(RecyclerView.HORIZONTAL)
            rvReward.adapter = rewardAdapter
        }
        rewardAdapter.setList(items)
    }

    private fun setProgress(progress: Double) {
        binding.vipProgressView.setProgress(progress)
    }

}