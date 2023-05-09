package org.cxct.sportlottery.ui2.common.bean

import com.stx.xhb.androidx.entity.BaseBannerInfo

data class XBannerImage(val title: String, val imgUrl: String, val jumpUrl: String? = null): BaseBannerInfo {
    override fun getXBannerUrl() = imgUrl

    override fun getXBannerTitle() = title
}