package org.cxct.sportlottery.ui.chat

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.SparseArray
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatMessageResult
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.ChatRoomMsg
import org.cxct.sportlottery.view.MixFontTextView

/**
 * @author kevin
 * @create 2023/3/15
 * @description
 */
class ChatMessageListAdapter3(val onPhotoClick: (String) -> Unit,
                              val onUserAvatarClick: (tagUserPair: Pair<String, String>) -> Unit,
                              val onRedEnvelopeClick: (String, Int) -> Unit)
    : BaseQuickAdapter<ChatRoomMsg<*, BaseViewHolder>, BaseViewHolder>(0) {

    var isAdmin = false //身份是否為管理員
    set(value) {
        if (value != isAdmin) {
            this.isAdmin = value
            notifyDataSetChanged()
        }
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

    fun dataCount() = data.size

    fun removeItems(position: Int, count: Int) {
        if (dataCount() == 0 || position < 0 || position + count > dataCount()) {
            return
        }

        val deleteList = data.subList(position, position + count)
        data.removeAll(deleteList)
        notifyItemRangeRemoved(position, count)
    }

    private val itemTypesMap = SparseArray<ChatRoomMsg<*, BaseViewHolder>>()

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        itemTypesMap.put(item.itemType, item)
        return item.itemType
    }

    override fun convert(holder: BaseViewHolder, item: ChatRoomMsg<*, BaseViewHolder>) {
        item.bindVH(this, holder)
    }

    override fun convert(holder: BaseViewHolder, item: ChatRoomMsg<*, BaseViewHolder>, payloads: List<Any>) {
        item.bindVH(this, holder, payloads)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return itemTypesMap[viewType].createViewHolder(this, parent)
    }

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
            && data.type != ChatMsgReceiveType.CHAT_SEND_PIC_AND_TEXT) {

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


    fun setChatList(chatMessageList: MutableList<ChatRoomMsg<*, *>>) {
        setList(chatMessageList as MutableList<ChatRoomMsg<*, BaseViewHolder>>)
    }

    fun onNewMsg(msg: ChatRoomMsg<*, *>) {
        addData(msg as ChatRoomMsg<*, BaseViewHolder>)
    }

    fun removeMsg(msgId: String) {
        var position = -1
        data.forEachIndexed { index, chatRoomMsg ->
            if (chatRoomMsg.content is ChatMessageResult && chatRoomMsg.content.messageId == msgId) {
                position = index
                return@forEachIndexed
            }
        }

        if (position != -1) {
            removeAt(position)
        }
    }

}
