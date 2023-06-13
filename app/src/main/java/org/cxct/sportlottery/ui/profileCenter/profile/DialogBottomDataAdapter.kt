package org.cxct.sportlottery.ui.profileCenter.profile

import android.content.Context
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R

class DialogBottomDataAdapter(context: Context) :
    BaseQuickAdapter<DialogBottomDataEntity, BaseViewHolder>(R.layout.item_dialog_bottom_select) {
    override fun convert(holder: BaseViewHolder, item: DialogBottomDataEntity) {
        holder.setText(R.id.tvDialogBtmItem, item.name)
        holder.setVisible(R.id.ivDialogBtmItem, item.flag)
        holder.setBackgroundColor(
            R.id.tvDialogBtmItem,
            context.resources.getColor(if (item.flag) R.color.color_E8EFFD else R.color.white)
        )

    }
}