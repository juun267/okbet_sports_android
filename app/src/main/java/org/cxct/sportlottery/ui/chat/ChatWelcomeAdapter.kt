package org.cxct.sportlottery.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ItemChatWelcomeBinding
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatUserResult
import org.cxct.sportlottery.util.DisplayUtil.dp

/**
 * @author kevin
 * @create 2023/3/17
 * @description
 */
class ChatWelcomeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM = 101
    private val NULL = 99

    private val nullParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 32.dp)

    var dataList = mutableListOf<ChatUserResult>()

    var activity: ChatActivity? = null

    fun insertItem() {
        notifyItemInserted(dataList.size)
    }

    override fun getItemCount(): Int = dataList.size + 2 //添加兩個空白項目用於滾出介面

    override fun getItemViewType(position: Int): Int {
        return if (position < dataList.size) ITEM else NULL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == NULL) {
            return NullViewHolder(View(parent.context).apply { layoutParams = nullParams })
        }

        return UserViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserViewHolder) {
            holder.bind(dataList[position])
        }
    }

    inner class UserViewHolder(parent: ViewGroup, val binding: ItemChatWelcomeBinding = ItemChatWelcomeBinding.inflate(
                                   LayoutInflater.from(parent.context), parent, false))
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ChatUserResult) {
            binding.tvName.text = data.nickName
            activity?.let {
                binding.bvBlock.setupWith(it.window?.decorView?.rootView as ViewGroup)
                    .setFrameClearDrawable(it.window?.decorView?.background)
                    .setBlurRadius(4f)
            }
        }
    }

    inner class NullViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}