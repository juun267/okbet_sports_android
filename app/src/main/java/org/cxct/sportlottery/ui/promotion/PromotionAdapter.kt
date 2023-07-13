package org.cxct.sportlottery.ui.promotion

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemPromotionListBinding
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.sConfigData

class PromotionAdapter: BindingAdapter<ActivityImageList, ItemPromotionListBinding>() {

    override fun onBinding(position: Int, binding: ItemPromotionListBinding, item: ActivityImageList) {
         binding.apply {
             ivImage.load(sConfigData?.resServerHost+item.indexImage, R.drawable.img_banner01,R.drawable.img_banner01)
             tvContent.text = item.titleText
         }
    }
}