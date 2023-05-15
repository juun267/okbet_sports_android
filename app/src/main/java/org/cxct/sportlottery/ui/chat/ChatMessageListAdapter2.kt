package org.cxct.sportlottery.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_redenvelope_fail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.*
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.*
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.chat.adapter.vh.*
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.MixFontTextView

/**
 * @author kevin
 * @create 2023/3/15
 * @description
 */
@SuppressLint("SetTextI18n")
class ChatMessageListAdapter2(private val onPhotoClick: (String) -> Unit,
                              private val onUserAvatarClick: (tagUserPair: Pair<String, String>) -> Unit,
                              private val onRedEnvelopeClick: (String, Int) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isAdmin = false //身份是否為管理員
    var dataList = mutableListOf<ChatReceiveContent<*>>()

    enum class ItemType {
        MESSAGE_ADMIN, MESSAGE_USER, MESSAGE_ME, TIME, SYSTEM, RED_ENVELOPE, WIN_RED_ENVELOPE, DATE_TIP, FLOATING_DATE_TIP
    }

    //  0-立刻发红包(系统红包)
    //  1 每日红包（暂无）
    //  2 随机红包 random
    //  3 定向红包 assign
    //  4 小号红包
    //  5 口令红包 password
    enum class RedEnvelopeType(val type: Int) {
        RANDOM(2), ASSIGN(3), PASSWORD(5)
    }


    fun insertItem() {
        notifyItemInserted(dataList.size)
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
//        notifyItemRangeChanged(position, dataList.size - position)
    }

    fun removeRangeItem(start: Int, count: Int) {
        val change = dataList.size - start
        notifyItemRangeRemoved(start, count)
//        notifyItemRangeChanged(start, change)
    }

    override fun getItemViewType(position: Int): Int {
        val itemData = dataList[position]
        if (itemData.isCustomMessage) {
            return if (itemData.content is String) { ItemType.DATE_TIP.ordinal } else { -1 }
        }

        return when (dataList[position].type) {
            ChatMsgReceiveType.CHAT_MSG,
            ChatMsgReceiveType.CHAT_SEND_PIC,
            ChatMsgReceiveType.CHAT_SEND_PIC_AND_TEXT -> {
                when (dataList[position].isMySelf) {
                    true -> ItemType.MESSAGE_ME.ordinal
                    else -> ItemType.MESSAGE_USER.ordinal
                }
            }

            ChatMsgReceiveType.CHAT_USER_PROMPT -> {
                ItemType.SYSTEM.ordinal
            }

            ChatMsgReceiveType.CHAT_MSG_RED_ENVELOPE,
            ChatMsgReceiveType.CHAT_SEND_RED_ENVELOPE,
            ChatMsgReceiveType.CHAT_SEND_PERSONAL_RED_ENVELOPE-> {
                ItemType.RED_ENVELOPE.ordinal
            }

            ChatMsgReceiveType.CHAT_WIN_RED_ENVELOPE_ROOM_NOTIFY,
            ChatMsgReceiveType.CHAT_WIN_RED_ENVELOPE_RAIN_NOTIFY-> {
                ItemType.WIN_RED_ENVELOPE.ordinal
            }
            else -> -1
        }
    }

    override fun getItemCount(): Int = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.MESSAGE_USER.ordinal -> UserVH(parent, onPhotoClick, onUserAvatarClick)
            ItemType.MESSAGE_ME.ordinal -> MeVH(parent, onPhotoClick)
            ItemType.RED_ENVELOPE.ordinal -> MessageRedEnvelopeVH(parent, { isAdmin }, onRedEnvelopeClick)
            ItemType.WIN_RED_ENVELOPE.ordinal -> WinRedEnvelopeVH(parent)
            ItemType.SYSTEM.ordinal -> SystemVH(parent)
            ItemType.DATE_TIP.ordinal -> DateVH(parent)
            //ItemDecoration onDrawOver使用
            ItemType.FLOATING_DATE_TIP.ordinal -> FloatingDateVH(parent)

            else -> NullViewHolder(FrameLayout(parent.context))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is UserVH -> {
            holder.bind(dataList[position])
        }
        is MeVH -> {
            holder.bind(dataList[position])
        }
        is MessageRedEnvelopeVH -> {
            holder.bind(dataList[position])
        }
        is WinRedEnvelopeVH -> {
            holder.bind(dataList[position])
        }
        is SystemVH -> {
            holder.bind(dataList[position])
        }
        is DateVH -> {
            holder.bind(dataList[position])
        }
        //ItemDecoration onDrawOver使用
        is FloatingDateVH -> {
            holder.bind(dataList[position])
        }

        else -> {  }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        }
    }


    //暫時
    inner class NullViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    companion object {

        //除了管理員，其他人一般訊息要濾掉 網址(.com, .net, etc.) 變成笑臉圖案
        private val urlFilterRegex =
            Regex("((http|ftp|https|Http|Ftp|Https|www)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?")
        private val netFilterRegex = Regex("(.com|.net|.org|.cc|.vip|.me|.app|.live|.info|.io|.club)")
        private val smileEmoticon = "\uD83D\uDE04"
        private val resServerHost: String = sConfigData?.resServerHost.orEmpty()

        /**
         * 處理圖片和文字訊息以及排版
         */
        fun setupPictureAndTextMessage(
            data: ChatMessageResult,
            imageView: AppCompatImageView,
            textView: MixFontTextView,
            messageBorder: LinearLayout,
            onPhotoClick: (String) -> Unit
        ) {
            val content = data.content
            if (data.type != ChatMsgReceiveType.CHAT_SEND_PIC
                || data.type != ChatMsgReceiveType.CHAT_SEND_PIC_AND_TEXT) {

                imageView.isVisible = false
                textView.isVisible = true
                textView.post { textView.gravity = if (textView.lineCount > 1) Gravity.START else Gravity.CENTER }
                messageBorder.post { messageBorder.gravity = Gravity.CENTER }
                return
            }

            imageView.isVisible = true
            //content:[img:{imagePath}]{message}
            // ex:
            //    content:[img:/p/20230409/cx_sports/1/img/469/jpg/1681007650426.jpg]你好
            val imgUrl: String
            val textMsg: String
            val contentString = content.orEmpty()
            val startKey = "[img:"
            val endKey = "]"
            if (contentString.startsWith(startKey) && contentString.contains(endKey)) {
                val imgContent = contentString.split(endKey)[0]
                imgUrl = imgContent.replace(startKey, "")
                textMsg = contentString.replace(imgContent + endKey, "")
            } else {
                imgUrl = contentString
                textMsg = ""
            }

            textView.mixFontText = textMsg
            textView.isVisible = textMsg.isNotEmpty()
            textView.post { textView.gravity = Gravity.START } //圖文訊息的情況下要Gravity.START
            messageBorder.post { messageBorder.gravity = Gravity.START }
            val url = if (imgUrl.startsWith("http")) imgUrl else "$resServerHost/$imgUrl"
            imageView.load(url, R.drawable.ic_image_load)
            imageView.setOnClickListener {onPhotoClick(url) }
        }

        /**
         * 檢查是否需要顯示tag (sample: "[@:nickName] " => "@nickName " (@nickName改藍字+底線) )
         */
        fun checkTag(
            stringBuilder: SpannableStringBuilder,
            context: Context,
            message: String?,
        ): SpannableStringBuilder {

            val startKey = "[@:"
            val endKey = "] "
            val content = message.orEmpty()
            if (!content.contains(startKey) || !content.contains(endKey)) { //"[@:nickName] "
                stringBuilder.append(content)
                return stringBuilder
            }

            val words = content.split(endKey)
            val space = " "
            for (word in words) {
                if (word.contains(startKey)) {
                    if (word.contains(space)) { //是否包含space
                        val checkWords = word.split(space)
                        for (checkWord in checkWords) {
                            if (checkWord.contains(startKey)) {
                                val targetWord = checkWord.replace(startKey, "@")
                                setupTextColorAndUnderline(targetWord, context, stringBuilder)
                            } else {
                                stringBuilder.append(checkWord)
                            }
                            stringBuilder.append(space) //用(space = " ")split，需補空格
                        }
                    } else {
                        val targetWord = word.replace(startKey, "@")
                        setupTextColorAndUnderline(targetWord, context, stringBuilder)
                    }
                } else {
                    stringBuilder.append(word)
                }
                stringBuilder.append(space) //用(endKey = "] ")split，需補空格
            }

            return stringBuilder
        }

        private fun setupTextColorAndUnderline(
            target: String,
            context: Context,
            stringBuilder: SpannableStringBuilder,
        ) {
            val tagName = SpannableString(target)
            tagName.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_025BE8)),
                0,
                tagName.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            tagName.setSpan(UnderlineSpan(),
                0,
                tagName.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            stringBuilder.append(tagName) //@nickName 改文字顏色+底線
        }

        /**
         * 除了管理員，其他人一般訊息要濾掉 網址(.com, .net, etc.) 變成笑臉圖案
         * (会员角色 0游客、1会员、2管理员、3訪客)
         */
        fun checkContent(content: String?, userType: String?): String {
             if (userType == "2") {
                 return content.orEmpty()
            }
            if (content?.matches(urlFilterRegex) == true) {
                return content.replace(netFilterRegex, smileEmoticon)
            }
            return content.orEmpty()
        }
    }

}
