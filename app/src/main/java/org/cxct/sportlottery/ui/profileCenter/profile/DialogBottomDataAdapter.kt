package org.cxct.sportlottery.ui.profileCenter.profile

import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemDialogBottomSelectBinding

class DialogBottomDataAdapter : BindingAdapter<DialogBottomDataEntity, ItemDialogBottomSelectBinding>() {

    override fun onBinding(
        position: Int,
        binding: ItemDialogBottomSelectBinding,
        item: DialogBottomDataEntity,
    )=binding.run {
        tvDialogBtmItem.text = item.name
        ivDialogBtmItem.isVisible = item.flag
        tvDialogBtmItem.setBackgroundColor(context.resources.getColor(if (item.flag) R.color.color_E8EFFD else R.color.white))
    }
}