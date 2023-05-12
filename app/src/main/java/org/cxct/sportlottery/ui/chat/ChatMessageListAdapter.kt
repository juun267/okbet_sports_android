package org.cxct.sportlottery.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.dialog_redenvelope_fail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hide
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.*
import org.cxct.sportlottery.network.chat.socketResponse.chatMessage.*
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.widget.MixFontTextView

/**
 * @author kevin
 * @create 2023/3/15
 * @description
 */
@SuppressLint("SetTextI18n")
class ChatMessageListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isAdmin = false //身份是否為管理員
    var dataList = mutableListOf<ChatReceiveContent<*>>()
    var itemContentClickListener: ItemContentClickListener? = null
    private val resServerHost: String = sConfigData?.resServerHost.orEmpty()

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

    //除了管理員，其他人一般訊息要濾掉 網址(.com, .net, etc.) 變成笑臉圖案
    private val urlFilterRegex =
        Regex("((http|ftp|https|Http|Ftp|Https|www)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?")
    private val netFilterRegex = Regex("(.com|.net|.org|.cc|.vip|.me|.app|.live|.info|.io|.club)")
    private val smileEmoticon = "\uD83D\uDE04"

    fun insertItem() {
        notifyItemInserted(dataList.size)
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, dataList.size - position)
    }

    fun removeRangeItem(start: Int, count: Int) {
        val change = dataList.size - start
        notifyItemRangeRemoved(start, count)
        notifyItemRangeChanged(start, change)
    }

    override fun getItemViewType(position: Int): Int {
        val itemData = dataList[position]
        return when (itemData.isCustomMessage) {
            true -> {
                if (itemData.content is String) {
                    ItemType.DATE_TIP.ordinal
                } else {
                    -1
                }
            }
            else -> {
                when (dataList[position].type) {
                    ChatMsgReceiveType.CHAT_MSG,
                    ChatMsgReceiveType.CHAT_SEND_PIC,
                    ChatMsgReceiveType.CHAT_SEND_PIC_AND_TEXT,
                    -> {
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
                    ChatMsgReceiveType.CHAT_SEND_PERSONAL_RED_ENVELOPE,
                    -> {
                        ItemType.RED_ENVELOPE.ordinal
                    }
                    ChatMsgReceiveType.CHAT_WIN_RED_ENVELOPE_ROOM_NOTIFY,
                    ChatMsgReceiveType.CHAT_WIN_RED_ENVELOPE_RAIN_NOTIFY,
                    -> {
                        ItemType.WIN_RED_ENVELOPE.ordinal
                    }
                    else -> -1
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.MESSAGE_USER.ordinal -> UserViewHolder(
                ItemChatMessageUserBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            ItemType.MESSAGE_ME.ordinal -> MeViewHolder(
                ItemChatMessageMeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            ItemType.RED_ENVELOPE.ordinal -> MessageRedEnvelopeViewHolder(
                ItemChatMessageRedEnvelopeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            ItemType.WIN_RED_ENVELOPE.ordinal -> WinRedEnvelopeViewHolder(
                ItemChatMessageWinRedEnvelopeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            ItemType.SYSTEM.ordinal -> SystemViewHolder(
                ItemChatMessageSystemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            ItemType.DATE_TIP.ordinal -> DateViewHolder(
                ItemChatDateBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            //ItemDecoration onDrawOver使用
            ItemType.FLOATING_DATE_TIP.ordinal -> FloatingDateViewHolder(
                ItemChatDateBlurBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> NullViewHolder(FrameLayout(parent.context))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> {
                holder.bind(dataList[position])
            }
            is MeViewHolder -> {
                holder.bind(dataList[position])
            }
            is MessageRedEnvelopeViewHolder -> {
                holder.bind(dataList[position])
            }
            is WinRedEnvelopeViewHolder -> {
                holder.bind(dataList[position])
            }
            is SystemViewHolder -> {
                holder.bind(dataList[position])
            }
            is DateViewHolder -> {
                holder.bind(dataList[position])
            }
            //ItemDecoration onDrawOver使用
            is FloatingDateViewHolder -> {
                holder.bind(dataList[position])
            }
        }
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

    inner class UserViewHolder(val binding: ItemChatMessageUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ChatReceiveContent<*>) {
            data.getThisContent<ChatMessageResult>()?.apply {
                binding.tvName.text = nickName
                val checkedContent = checkContent(content, userType)
                val stringBuilder = SpannableStringBuilder()
                val checkTag =
                    SpannableString(checkTag(stringBuilder, binding.root.context, checkedContent))
                binding.tvMessage.apply {
                    mixFontText = checkTag
                    setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            getTextColor(userType, false)
                        )
                    )
                }
                //region 處理圖片和文字訊息以及排版
                setupPictureAndTextMessage(
                    this@apply,
                    binding.ivChatImage,
                    binding.tvMessage,
                    binding.messageBorder
                )
                binding.paddingView.isGone =
                    binding.ivChatImage.isVisible && binding.tvMessage.isVisible
                //endregion
                binding.tvTime.text =
                    if (curTime != null) TimeUtil.timeFormat(
                        curTime.toLong(),
                        TimeUtil.HM_FORMAT
                    ) else ""

                if (userType == UserType.ADMIN.code) {
                    binding.ivAvatar.setImageResource(R.drawable.ic_chat_admin)

                    binding.messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_admin_circle)

                    binding.messageBorder.backgroundTintList = null
                } else {
                    Glide.with(binding.root.context)
                        .load(iconUrl)
                        .placeholder(
                            ContextCompat.getDrawable(
                                binding.root.context,
                                R.drawable.ic_person_avatar
                            )
                        )
                        .error(
                            ContextCompat.getDrawable(
                                binding.root.context,
                                R.drawable.ic_person_avatar
                            )
                        )
                        .into(binding.ivAvatar)

                    //0游客、1会员、2管理员(特殊處理)、3訪客
                    binding.messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_user_custom_circle_border)

                    binding.messageBorder.backgroundTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                binding.root.context,
                                getBorderColor(userType, false)
                            )
                        )
                }

                //點擊用戶暱稱
                binding.tvName.setOnClickListener {
                    var userIdString = userId?.toString() ?: ""
                    var nickNameString = nickName.orEmpty()
                    if (userIdString.isNotEmpty() && nickNameString.isNotEmpty()) {
                        userIdString = "[@:$userIdString]" //包裝過的格式 "[@:userId]"
                        nickNameString = "@$nickNameString" //輸入匡要顯示 "@nickName"
                        itemContentClickListener?.onUserAvatarClick(
                            Pair(userIdString, nickNameString)
                        )
                    }
                }
            }
        }
    }

    inner class MeViewHolder(val binding: ItemChatMessageMeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ChatReceiveContent<*>) {
            data.getThisContent<ChatMessageResult>()?.apply {
                val checkedContent = checkContent(content, userType)
                val stringBuilder = SpannableStringBuilder()
                val checkTag =
                    SpannableString(checkTag(stringBuilder, binding.root.context, checkedContent))
                binding.tvMessage.apply {
                    setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            getTextColor(userType, true)
                        )
                    )
                    mixFontText = checkTag
                }
                //region 處理圖片和文字訊息以及排版
                setupPictureAndTextMessage(
                    this@apply,
                    binding.ivChatImage,
                    binding.tvMessage,
                    binding.messageBorder
                )
                binding.paddingView.isGone =
                    binding.ivChatImage.isVisible && binding.tvMessage.isVisible
                //endregion

                binding.tvTime.text =
                    if (curTime != null) TimeUtil.timeFormat(
                        curTime.toLong(),
                        TimeUtil.HM_FORMAT
                    ) else ""

                if (userType == UserType.ADMIN.code) {
                    binding.messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_admin_circle_me)

                    binding.messageBorder.backgroundTintList = null
                } else {
                    //0游客、1会员、2管理员(特殊處理)、3訪客
                    binding.messageBorder.setBackgroundResource(R.drawable.bg_chat_pop_me_custom_circle_border)

                    binding.messageBorder.backgroundTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                binding.root.context,
                                getBorderColor(userType, true)
                            )
                        )
                }
            }
        }
    }

    inner class MessageRedEnvelopeViewHolder(val binding: ItemChatMessageRedEnvelopeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ChatReceiveContent<*>) {
            when (data.type) {
                //1001
                ChatMsgReceiveType.CHAT_SEND_RED_ENVELOPE -> {
                    data.getThisContent<ChatRedEnvelopeResult>()?.apply {
                        when (packetType) {
                            RedEnvelopeType.RANDOM.type -> {
                                binding.tvName.text = nickName
                                binding.tvMessage.mixFontText =
                                    binding.root.context.getString(R.string.chat_room_member) + "\n" +
                                            "[\u0020$nickName\u0020]" + "\n" +
                                            binding.root.context.getString(R.string.chat_send_red_packets)
                                binding.llMessage.apply {
                                    setBackgroundResource(if (isAdmin) R.drawable.bg_chat_pop_red_envelope_fixed else R.drawable.bg_chat_pop_red_envelope_fixed_3_line)
                                    updatePadding(
                                        paddingStart,
                                        if (isAdmin) 13.dp else 16.dp,
                                        paddingEnd,
                                        paddingBottom
                                    )
                                }
                            }
                            else -> {
                                binding.tvName.text =
                                    binding.root.context.getString(R.string.system_red_packet)
                                binding.tvMessage.mixFontText =
                                    binding.root.context.getString(R.string.chat_opportunity)
                                binding.llMessage.apply {
                                    setBackgroundResource(if (isAdmin) R.drawable.bg_chat_pop_red_envelope_fixed_admin else R.drawable.bg_chat_pop_red_envelope_fixed)
                                    updatePadding(
                                        paddingStart,
                                        if (isAdmin) 15.dp else 13.dp,
                                        paddingEnd,
                                        paddingBottom
                                    )
                                }
                            }

                        }
                        binding.tvTime.text = if (data.time != null) TimeUtil.timeFormat(
                            data.time,
                            TimeUtil.HM_FORMAT
                        ) else ""

                        binding.tvRedEnvelope.apply { if (isAdmin) hide() else show() }
                        binding.tvRedEnvelope.setOnClickListener {
                            itemContentClickListener?.onRedEnvelopeClick(id.toString(), packetType)
                        }
                    }
                }

                //2005
                ChatMsgReceiveType.CHAT_SEND_PERSONAL_RED_ENVELOPE -> {
                    data.getThisContent<ChatPersonalRedEnvelopeResult>()?.apply {
                        binding.tvName.text =
                            binding.root.context.getString(R.string.system_red_packet)

                        binding.llMessage.apply {
                            setBackgroundResource(if (isAdmin) R.drawable.bg_chat_pop_red_envelope_fixed_admin else R.drawable.bg_chat_pop_red_envelope_fixed)
                            updatePadding(
                                paddingStart,
                                if (isAdmin) 15.dp else 13.dp,
                                paddingEnd,
                                paddingBottom
                            )
                        }
                        binding.tvRedEnvelope.apply { if (isAdmin) hide() else show() }
                        binding.tvRedEnvelope.setOnClickListener {
                            itemContentClickListener?.onRedEnvelopeClick(
                                this.id.toString(),
                                this.packetType ?: -1
                            )
                        }
                    }
                }

                //2008
                ChatMsgReceiveType.CHAT_MSG_RED_ENVELOPE -> {
                    data.getThisContent<ChatMessageResult>()?.chatRedEnvelopeMessageResult?.apply {
                        when (packetType) {
                            RedEnvelopeType.RANDOM.type -> {
                                binding.tvName.text = nickName
                                binding.tvMessage.mixFontText =
                                    binding.root.context.getString(R.string.chat_room_member) + "\n" +
                                            "[\u0020$nickName\u0020]" + "\n" +
                                            binding.root.context.getString(R.string.chat_send_red_packets)
                                binding.llMessage.apply {
                                    setBackgroundResource(if (isAdmin) R.drawable.bg_chat_pop_red_envelope_fixed else R.drawable.bg_chat_pop_red_envelope_fixed_3_line)
                                    updatePadding(
                                        paddingStart,
                                        if (isAdmin) 13.dp else 16.dp,
                                        paddingEnd,
                                        paddingBottom
                                    )
                                }
                            }
                            else -> {
                                binding.tvName.text =
                                    binding.root.context.getString(R.string.system_red_packet)
                                binding.tvMessage.mixFontText =
                                    binding.root.context.getString(R.string.chat_opportunity)
                                binding.llMessage.apply {
                                    setBackgroundResource(if (isAdmin) R.drawable.bg_chat_pop_red_envelope_fixed_admin else R.drawable.bg_chat_pop_red_envelope_fixed)
                                    updatePadding(
                                        paddingStart,
                                        if (isAdmin) 15.dp else 13.dp,
                                        paddingEnd,
                                        paddingBottom
                                    )
                                }
                            }
                        }
                        binding.tvTime.text = if (data.time != null) TimeUtil.timeFormat(
                            data.time,
                            TimeUtil.HM_FORMAT
                        ) else ""

                        binding.tvRedEnvelope.apply { if (isAdmin) hide() else show() }
                        binding.tvRedEnvelope.setOnClickListener {
                            itemContentClickListener?.onRedEnvelopeClick(
                                id.toString(),
                                packetType ?: -1
                            )
                        }
                    }
                }
            }
        }
    }

    inner class WinRedEnvelopeViewHolder(val binding: ItemChatMessageWinRedEnvelopeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ChatReceiveContent<*>) {
            data.getThisContent<ChatWinRedEnvelopeResult>()?.apply {
                binding.tvName.text = if (!nickName.isNullOrEmpty()) nickName else userName
                binding.tvMoney.text = TextUtil.format(money.toString()) //應正確顯示到小數第二位
                binding.tvCurrency.text = currency
            }
        }
    }

    inner class SystemViewHolder(val binding: ItemChatMessageSystemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ChatReceiveContent<*>) {
            data.getThisContent<ChatMessageResult>()?.apply {
                binding.tvName.text = binding.root.context.getString(R.string.system_notify)
                binding.tvMessage.mixFontText = content
                binding.tvTime.text =
                    if (curTime != null) TimeUtil.timeFormat(
                        curTime.toLong(),
                        TimeUtil.HM_FORMAT
                    ) else ""

                if (!bgColor.isNullOrEmpty()) {
                    binding.tvMessage.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor(bgColor))
                }
                if (!textColor.isNullOrEmpty())
                    binding.tvMessage.setTextColor(Color.parseColor(textColor))
            }
        }
    }

    inner class DateViewHolder(val binding: ItemChatDateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ChatReceiveContent<*>) {
            data.getThisContent<String>().apply {
                binding.tvDate.text = this@apply
            }
        }
    }

    //ItemDecoration onDrawOver使用
    inner class FloatingDateViewHolder(val binding: ItemChatDateBlurBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ChatReceiveContent<*>) {
            data.getThisContent<String>().apply {
                binding.tvDate.text = this@apply
            }
        }

        fun update(data: ChatReceiveContent<*>) {
            data.getThisContent<String>().apply {
                binding.tvDate.text = this@apply
            }
        }
    }

    //暫時
    inner class NullViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface ItemContentClickListener {
        fun onRedEnvelopeClick(packetId: String, packetType: Int)
        fun onPhotoClick(url: String)
        fun onUserAvatarClick(tagUserPair: Pair<String, String>)
    }

    /**
     * 處理圖片和文字訊息以及排版
     */
    private fun setupPictureAndTextMessage(
        data: ChatMessageResult,
        imageView: AppCompatImageView,
        textView: MixFontTextView,
        messageBorder: LinearLayout,
    ) {
        val content = data.content
        when (data.type) {
            ChatMsgReceiveType.CHAT_SEND_PIC,
            ChatMsgReceiveType.CHAT_SEND_PIC_AND_TEXT,
            -> {
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
//                Timber.e("imgUrl: $imgUrl, textMsg: $textMsg")
                textView.apply {
                    mixFontText = textMsg
                    isVisible = textMsg.isNotEmpty()
                    post {
                        gravity = Gravity.START //圖文訊息的情況下要Gravity.START
                    }
                }
                messageBorder.apply {
                    post {
                        gravity = Gravity.START
                    }
                }

                val url = if (imgUrl.startsWith("http")) imgUrl else "$resServerHost/$imgUrl"
                Glide.with(imageView.context)
                    .load(url)
                    .placeholder(
                        ContextCompat.getDrawable(
                            imageView.context,
                            R.drawable.ic_image_load
                        )
                    )
                    .error(ContextCompat.getDrawable(imageView.context, R.drawable.ic_image_load))
                    .into(imageView)

                imageView.setOnClickListener {
                    itemContentClickListener?.onPhotoClick(url)
                }
            }
            else -> {
                imageView.isVisible = false
                textView.apply {
                    isVisible = true
                    post {
                        gravity = if (lineCount > 1) Gravity.START else Gravity.CENTER
                    }
                }
                messageBorder.apply {
                    post {
                        gravity = Gravity.CENTER
                    }
                }
            }
        }
    }

    /**
     * 檢查是否需要顯示tag (sample: "[@:nickName] " => "@nickName " (@nickName改藍字+底線) )
     */
    private fun checkTag(
        stringBuilder: SpannableStringBuilder,
        context: Context,
        message: String?,
    ): SpannableStringBuilder {
        val startKey = "[@:"
        val endKey = "] "
        val content = message.orEmpty()
        if (content.contains(startKey) && content.contains(endKey)) { //"[@:nickName] "
            val words = content.split(endKey)
//            Timber.e("words: $words")
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
        } else {
            stringBuilder.append(content)
        }
        return stringBuilder
    }

    private fun setupTextColorAndUnderline(
        target: String,
        context: Context,
        stringBuilder: SpannableStringBuilder,
    ) {
        val tagName = SpannableString(target)
        tagName.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(context, R.color.color_025BE8)
            ), 0, tagName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tagName.setSpan(
            UnderlineSpan(),
            0, tagName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        stringBuilder.append(tagName) //@nickName 改文字顏色+底線
    }

    /**
     * 除了管理員，其他人一般訊息要濾掉 網址(.com, .net, etc.) 變成笑臉圖案
     * (会员角色 0游客、1会员、2管理员、3訪客)
     */
    private fun checkContent(content: String?, userType: String?): String {
        return if (userType == "2") {
            content.orEmpty()
        } else {
            if (content?.matches(urlFilterRegex) == true) {
                content.replace(netFilterRegex, smileEmoticon)
            } else {
                content.orEmpty()
            }
        }
    }

    data class UserType private constructor(
        val code: String,
        val borderColor: Int,
        val fillColor: Int,
        val textColor: Int,
    ) {
        companion object {
            val GUEST = UserType(
            "0",
            R.color.color_chat_message_border_guest,
                R.color.color_chat_message_fill_guest,
                R.color.color_chat_message_text_guest
            )

            val MEMBER = UserType(
                "1",
                R.color.color_chat_message_border_member,
                R.color.color_chat_message_fill_member,
                R.color.color_333333
            )

            val MEMBER_ME = UserType(
                "1",
                R.color.color_025BE8,
                R.color.color_chat_message_fill_member,
                R.color.color_FFFFFF
            )

            val ADMIN = UserType(
                "2",
                R.color.color_chat_message_border_admin,
                R.color.color_chat_message_fill_admin,
                R.color.color_chat_message_text_admin
            )

            val VISITOR = UserType(
                "3",
            R.color.color_chat_message_border_guest,
            R.color.color_chat_message_fill_guest,
            R.color.color_chat_message_text_guest
            )
        }
    }

    fun getBorderColor(userType: String?, isMe: Boolean = false): Int =
        when (userType) {
            UserType.GUEST.code -> UserType.GUEST.borderColor
            UserType.MEMBER.code -> if (isMe) UserType.MEMBER_ME.borderColor else UserType.MEMBER.borderColor
            UserType.VISITOR.code -> UserType.VISITOR.borderColor
            else -> 0
        }

    fun getFillColor(userType: String?): Int =
        when (userType) {
            UserType.GUEST.code -> UserType.GUEST.fillColor
            UserType.MEMBER.code -> UserType.MEMBER.fillColor
            UserType.VISITOR.code -> UserType.VISITOR.fillColor
            else -> 0
        }

    fun getTextColor(userType: String?, isMe: Boolean = false): Int =
        when (userType) {
            UserType.GUEST.code -> UserType.GUEST.textColor
            UserType.MEMBER.code -> if (isMe) UserType.MEMBER_ME.textColor else UserType.MEMBER.textColor
            UserType.ADMIN.code -> UserType.ADMIN.textColor
            UserType.VISITOR.code -> UserType.VISITOR.textColor
            else -> 0
        }

}
