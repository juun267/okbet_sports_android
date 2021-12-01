package org.cxct.sportlottery.ui.vip

import com.stx.xhb.androidx.entity.BaseBannerInfo

class BannerLevelCard(private val levelRequirement: LevelRequirement) : BaseBannerInfo {
    override fun getXBannerUrl(): LevelRequirement {
        return levelRequirement
    }

    override fun getXBannerTitle(): String {
        return ""
    }
}