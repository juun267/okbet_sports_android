package org.cxct.sportlottery.ui.promotion

import androidx.recyclerview.widget.DiffUtil
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemPromotionListBinding
import org.cxct.sportlottery.net.user.data.ActivityImageList
import org.cxct.sportlottery.repository.sConfigData

class PromotionAdapter: BindingAdapter<ActivityImageList, ItemPromotionListBinding>() {


    init {
        setDiffCallback(object : DiffUtil.ItemCallback<ActivityImageList>() {
            override fun areItemsTheSame(
                oldItem: ActivityImageList,
                newItem: ActivityImageList
            ) = oldItem === newItem

            override fun areContentsTheSame(
                oldItem: ActivityImageList,
                newItem: ActivityImageList
            ) = newItem.id != null && oldItem.id == newItem.id

        })
    }

    override fun onBinding(position: Int, binding: ItemPromotionListBinding, item: ActivityImageList) = binding.run {
         ivImage.load(sConfigData?.resServerHost+item.titleImage, R.drawable.img_banner01)
         tvContent.text = item.titleText
    }
}