package org.cxct.sportlottery.ui.promotion

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemPromotionListBinding
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.sConfigData

class PromotionAdapter: BindingAdapter<ActivityImageList, ItemPromotionListBinding>() {

    override fun onBinding(position: Int, binding: ItemPromotionListBinding, item: ActivityImageList) {
         binding.apply {
             ivImage.load(sConfigData?.resServerHost+item.titleImage)
             tvContent.text = item.titleText
         }
    }
}