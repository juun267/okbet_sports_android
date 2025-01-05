package org.cxct.sportlottery.ui.profileCenter.pointshop.main

import android.view.View
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemShopTypeItemBinding

class ShopTypeTabAdapter:BindingAdapter<ShopTypeTab,ItemShopTypeItemBinding>() {

    fun updateSelected() {
       notifyDataSetChanged()
    }


    override fun onBinding(position: Int, binding: ItemShopTypeItemBinding, item: ShopTypeTab) {
        binding.tvShopType.apply {
            text =
                if (item.isAllType) context.getString(R.string.B76) else item.typeName
            setTextColor(
                ContextCompat.getColor(
                    context,
                    if (item.isSelected) R.color.color_025BE8 else R.color.color_0D2245
                )
            )
        }

        binding.ivSelectedMark.visibility =
            if (item.isSelected) View.VISIBLE else View.INVISIBLE
    }

}