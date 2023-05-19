package org.cxct.sportlottery.ui.chat.adapter

import android.view.ViewGroup
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.ui.chat.ChatMessageListAdapter3

interface IChatVH<VH: BaseViewHolder> {
    fun createViewHolder(adapter: ChatMessageListAdapter3, parent: ViewGroup): VH
    fun bindVH(adapter: ChatMessageListAdapter3, holder: VH)
    fun bindVH(adapter: ChatMessageListAdapter3, holder: VH, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            bindVH(adapter, holder)
        }
    }
}