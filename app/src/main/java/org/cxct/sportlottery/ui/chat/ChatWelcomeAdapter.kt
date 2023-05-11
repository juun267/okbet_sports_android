package org.cxct.sportlottery.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ItemChatWelcomeBinding
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatUserResult
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.DisplayUtil.dp

/**
 * @author kevin
 * @create 2023/3/17
 * @description
 */
class ChatWelcomeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType {
        ITEM, NULL
    }

    private val nullParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 32.dp)

    var dataList = mutableListOf<ChatUserResult>()

    var activity: MainTabActivity? = null

    fun insertItem() {
        notifyItemInserted(dataList.size)
    }

    override fun getItemCount(): Int = dataList.size + 2 //添加兩個空白項目用於滾出介面

    override fun getItemViewType(position: Int): Int {
        return if (position < dataList.size) {
            ItemType.ITEM.ordinal
        } else {
            ItemType.NULL.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.NULL.ordinal -> NullViewHolder(
                View(parent.context).apply {
                    layoutParams = nullParams
                }
            )
            else -> UserViewHolder(
                ItemChatWelcomeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> {
                holder.bind(dataList[position])
            }
        }
    }

    inner class UserViewHolder(val binding: ItemChatWelcomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
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