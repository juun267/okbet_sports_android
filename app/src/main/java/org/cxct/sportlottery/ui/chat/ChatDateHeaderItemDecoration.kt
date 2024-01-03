package org.cxct.sportlottery.ui.chat

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.HeaderItemDecoration

/**
 * 此ItemDecoration僅提供給R.layout.item_chat_date做使用
 * @see R.layout.item_chat_date
 */
class ChatDateHeaderItemDecoration(
    parent: RecyclerView,
    shouldFadeOutHeader: Boolean = false,
    private val isHeader: (itemPosition: Int) -> Boolean,
) : HeaderItemDecoration(parent, shouldFadeOutHeader, isHeader) {
    override fun getHeaderViewForItem(itemPosition: Int, parent: RecyclerView): View? {
        if (parent.adapter == null) {
            return null
        }
        val headerPosition = getHeaderPositionForItem(itemPosition)
        if (headerPosition == RecyclerView.NO_POSITION) return null
        val headerType = when (val itemViewType = parent.adapter?.getItemViewType(headerPosition)) {
            null -> null
            else -> itemViewType
        } ?: return null
        // if match reuse viewHolder
        if (currentHeader?.first == headerPosition && currentHeader?.second?.itemViewType == headerType) {
            return currentHeader?.second?.itemView
        }

        val headerHolder = parent.adapter?.createViewHolder(parent, headerType)

        if (headerHolder != null) {
            parent.adapter?.onBindViewHolder(headerHolder, headerPosition)
            fixLayoutSize(parent, headerHolder.itemView)
            // save for next draw
            currentHeader = headerPosition to headerHolder
        }
        return headerHolder?.itemView
    }
}