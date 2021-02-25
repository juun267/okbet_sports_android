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
        bubble_level_one.isSelected = true
        banner_vip_level.setPageTransformer(Transformer.Default)
    }

    private fun userInfoUpdateView(userInfo: UserInfoData) {
        setupViewByUserInfo(userInfo)
        updateUserLevel(userInfo.userLevelId)
        updateUserGrowthBar(userInfo)
    }

    private fun updateUserGrowthBar(userInfo: UserInfoData) {
        val growthRequirement = getUpgradeGrowthRequirement(userInfo.userLevelId)
        val userGrowth = userInfo.growth?.toInt() ?: 0
        pb_user_growth.apply {
            max = growthRequirement
            progress = userGrowth
        }
        tv_requirement_amount.text = TextUtil.format((growthRequirement - userGrowth).toDouble())
    }

    private fun getUpgradeGrowthRequirement(levelId: Int): Int {
        return Level.values().find { level -> level.levelRequirement.levelId == levelId + 1 }?.levelRequirement?.growthRequirement ?: 0
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