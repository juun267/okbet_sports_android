package org.cxct.sportlottery.ui.chat

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemChatWelcomeBinding
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatUserResult
import org.cxct.sportlottery.util.DisplayUtil.dp


/**
 * @author kevin
 * @create 2023/3/17
 * @description
 */
class ChatWelcomeAdapter(val lifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM = 101
    private val NULL = 99

    private val nullParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 32.dp)
    private var jobScroll: Job? = null

    var dataList = mutableListOf<ChatUserResult>()

    var activity: ChatActivity? = null

    private fun schuleRemove() {
        if (jobScroll?.isActive == true) jobScroll?.cancel()
        jobScroll = lifecycleOwner.lifecycleScope.launch {
            delay(1500)
            while (dataList.isNotEmpty()) {
                dataList.removeAt(0)
                notifyItemRemoved(0)
                delay(1500)
            }

            jobScroll = null
        }
    }

    fun stop() {
        jobScroll?.cancel()
        jobScroll = null
    }

    fun userEnter(item: ChatUserResult) {
        if (dataList.isEmpty() || dataList.last().userId != item.userId) {
            dataList.add(item)
            notifyItemInserted(dataList.size)
            if (dataList.size > 5) {
                dataList.removeAt(0)
                notifyItemRemoved(0)
            }
            schuleRemove()
        }
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

        fun bind(data: ChatUserResult) = binding.run {

            val context = root.context
            val nameTag = "[${data.nickName}]"
            var welcomeString = context.getString(R.string.N991, nameTag)
            val index = welcomeString.indexOf(nameTag)
            welcomeString = welcomeString.replace(nameTag, "${data.nickName}")
            val builder = SpannableStringBuilder(welcomeString)
            builder.setSpan(
                ForegroundColorSpan(context.getColor(R.color.color_FFF27C)),
                index,
                index + nameTag.length - 2,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )

            tvName.text = builder
            activity?.let {
                bvBlock.setupWith(it.window?.decorView?.rootView as ViewGroup)
                    .setFrameClearDrawable(it.window?.decorView?.background)
                    .setBlurRadius(4f)
            }
        }
    }

    inner class NullViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}