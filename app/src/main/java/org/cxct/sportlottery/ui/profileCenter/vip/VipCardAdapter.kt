package org.cxct.sportlottery.ui.profileCenter.vip

import android.view.ViewGroup.MarginLayoutParams
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.hide
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.ItemVipCardBinding
import org.cxct.sportlottery.util.DisplayUtil.dp

class VipCardAdapter: BindingAdapter<String, ItemVipCardBinding>() {
    override fun onBinding(position: Int, binding: ItemVipCardBinding, item: String) {
        if (position % 3 === 0) {
            binding.tvCurrent.show()
            (binding.card.layoutParams as MarginLayoutParams).leftMargin = 12.dp
        } else {
            binding.tvCurrent.hide()
            (binding.card.layoutParams as MarginLayoutParams).leftMargin = 10.dp
        }

    }
}