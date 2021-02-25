package org.cxct.sportlottery.ui.vip

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.stx.xhb.androidx.transformers.Transformer
import kotlinx.android.synthetic.main.activity_vip.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.user.info.UserInfoData
import org.cxct.sportlottery.ui.base.BaseNoticeActivity
import org.cxct.sportlottery.util.TextUtil

class VipActivity : BaseNoticeActivity<VipViewModel>(VipViewModel::class) {

    private val levelBubbleList by lazy { listOf<TextView>(bubble_level_one, bubble_level_two, bubble_level_three, bubble_level_four, bubble_level_five, bubble_level_six) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vip)

        initView()
        initObserve()
    }

    private fun initObserve() {
        viewModel.apply {
            //獲取用戶資料
            userInfoResult.observe(this@VipActivity, Observer {
                it.userInfoData?.let { data -> userInfoUpdateView(data) }
            })
            userLevelGrowthResult.observe(this@VipActivity, Observer {
                setupBannerData()
            })
        }
    }

    private fun initView() {
        viewModel.getUserLevelGrowth()
        pb_user_level.max = levelBubbleList.size

        banner_vip_level.setPageTransformer(Transformer.Default)
    }

    private fun userInfoUpdateView(userInfo: UserInfoData) {
        setupViewByUserInfo(userInfo)
        updateUserLevel(userInfo.userLevelId)
        updateUserGrowthBar(userInfo)
        updateNextLevelTips(userInfo)
    }

    private fun updateNextLevelTips(userInfo: UserInfoData) {
        getNextLevel(userInfo.userLevelId)?.let { nextLevel ->
            tv_next_level_tips.text = "${getString(nextLevel.levelRequirement.level)}  ${nextLevel.levelRequirement.levelName}"
        }
    }

    private fun updateUserGrowthBar(userInfo: UserInfoData) {
        if (verifyMaxLevel(userInfo.userLevelId)) {
            pb_user_growth.apply {
                max = 1
                progress = 1
            }
            return
        }
        val growthRequirement = getUpgradeGrowthRequirement(userInfo.userLevelId)
        val userGrowth = userInfo.growth?.toInt() ?: 0
        val nextLevelRequirement = (growthRequirement - userGrowth).let { if (it < 0) 0 else it }
        pb_user_growth.apply {
            max = growthRequirement
            progress = userGrowth
        }
        tv_requirement_amount.text = TextUtil.format(nextLevelRequirement.toDouble())
    }

    private fun verifyMaxLevel(levelId: Int): Boolean {
        return Level.values().last() === Level.values().find { level -> level.levelRequirement.levelId == levelId }
    }

    private fun getUpgradeGrowthRequirement(levelId: Int): Int {
        return getNextLevel(levelId)?.levelRequirement?.growthRequirement ?: 0
    }

    private fun getNextLevel(levelId: Int): Level? {
        return Level.values().find { level -> level.levelRequirement.levelId == levelId + 1 }
    }

    private fun setupViewByUserInfo(userInfo: UserInfoData) {
        userInfo.let { user ->
            tv_greet.text = user.nickName
            tv_user_amount.text = user.growth.toString()
        }
    }

    private fun updateUserLevel(levelId: Int) {
        Level.values().find { it.levelRequirement.levelId == levelId }?.apply {
            tv_vip_name.text = levelRequirement.levelName
            iv_vip.setImageDrawable(ContextCompat.getDrawable(this@VipActivity, levelRequirement.levelIcon))
            updateLevelBar(ordinal)
        }
    }

    private fun updateLevelBar(levelIndex: Int) {
        //0: VIP1, 1: VIP2 ...
        val level = levelIndex + 1
        pb_user_level.progress = level
        runOnUiThread {
            levelBubbleList.forEachIndexed { index, textView ->
                textView.isSelected = index == levelIndex
                textView.requestLayout()
            }
        }
    }

    private fun setupBannerData() {
        banner_vip_level.apply {
            setBannerData(R.layout.item_banner_member_level, setupBannerLevelRequirement())
            loadImage { banner, model, view, position ->
                val tvLevel: TextView? = view?.findViewById(R.id.tv_level)
                val ivLevel: ImageView? = view?.findViewById(R.id.iv_level_icon)
                //TODO Dean : 階級會員名稱、所需成長值
                val tvLevelName: TextView? = view?.findViewById(R.id.tv_level_name)
                val tvGrowthRequirement: TextView? = view?.findViewById(R.id.tv_growth_requirement)
                val cardInfo = (model as BannerLevelCard).xBannerUrl
                tvLevel?.text = getString(cardInfo.level)
                ivLevel?.setImageDrawable(ContextCompat.getDrawable(this@VipActivity, cardInfo.levelIcon))
                tvLevelName?.text = cardInfo.levelName
                tvGrowthRequirement?.text = getGrowthRequirementTips(cardInfo.growthRequirement)
            }
        }
    }

    private fun getGrowthRequirementTips(requirement: Int?): String {
        return when {
            requirement == null -> ""
            (requirement <= 1) -> {
                getString(R.string.no_value)
            }
            else -> "$requirement ${getString(R.string.level_requirement_unit)}"
        }
    }

    /**
     * 設置會員層級資料至BannerItem
     */
    private fun setupBannerLevelRequirement(): List<BannerLevelCard> {
        return mutableListOf<BannerLevelCard>().apply {
            Level.values().forEach {
                this.add(BannerLevelCard(it.levelRequirement))
            }
        }
    }
}