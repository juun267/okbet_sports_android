package org.cxct.sportlottery.ui.chat

import android.app.Activity
import android.graphics.Color
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.ArithUtil

class ChatMessageAdapter(val activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TAG = "ChatMessageAdapter"
    }

    private val mChatApiUrl: String = "getAppConfig().chatHost" ?: ""
    private var mDataList = mutableListOf<ChatMessage>() //跟注資料清單
    private var mChatConfigOutput: ChatConfigOutput? = null
    private var mOnChatListener: OnChatListener? = null

    //語音播放
    private var isPlayAudioId = -1 //isPlayMicId
    private var isPlayMediaPlayer = false
    var isPlayRecorder = false

    private var mCountDownTimer: CountDownTimer? = null //countDownMap -> mCountDownTimer
    private var mPlayerCurrentTime = -1
    private var mMediaPlayer: MediaPlayer? = null

    fun setOnChatListener(onChatListener: OnChatListener) {
        mOnChatListener = onChatListener
    }

    //20200616 紀錄問題：除了管理員，其他人一般訊息要濾掉 網址 變成笑臉圖案
    // ((http|ftp|https|Http|Ftp|Https)://)(([a-zA-Z0-9\._-]+\.[a-zA-Z]{2,6})|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\&%_\./-~-]*)?
    private val urlFilterRegex =
        Regex("((http|ftp|https|Http|Ftp|Https|www)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?")
    private val netFilterRegex = Regex("(.com|.net|.org|.cc|.vip|.me|.app|.live|.info|.io|.club)")
    private val smileEmoticon = "\uD83D\uDE04"

    private enum class ViewType {
        CHAT_RV_MSG_L, CHAT_RV_MSG_R, CHAT_RV_WARNING, CHAT_RV_BET_FOLLOW_L,
        CHAT_RV_BET_FOLLOW_R, CHAT_RV_RED, CHAT_RV_RE_GET, CHAT_RV_BET_LOTTERY, UNKNOWN_TYPE, CHAT_ROBOT_BET, CHAT_BET_RECORD_L, CHAT_BET_RECORD_R,
        CHAT_RV_PLAN_PUSH_L
    }

    override fun getItemViewType(position: Int): Int {
        val data = mDataList[position]
        return when (data.chatType) {
            1 -> { //訊息、圖片、語音
                if (data.fk == mChatConfigOutput?.fk) //資料的 fk 等於自己的 fk，代表是自己發的訊息
                    ViewType.CHAT_RV_MSG_R.ordinal
                else
                    ViewType.CHAT_RV_MSG_L.ordinal
            }

            4, //禁言
            5, //解禁言
            7, 8, 301, //聊天時段 7: 系統給個人的訊息
            -> {
                ViewType.CHAT_RV_WARNING.ordinal
            }

            6 -> { //跟注
                if (data.fk == mChatConfigOutput?.fk) //資料的 fk 等於自己的 fk，代表是自己發的跟注
                    ViewType.CHAT_RV_BET_FOLLOW_R.ordinal
                else
                    ViewType.CHAT_RV_BET_FOLLOW_L.ordinal
            }

            12 -> { //計畫消息
                ViewType.CHAT_RV_PLAN_PUSH_L.ordinal
            }

            10 -> { //紅包
                ViewType.CHAT_RV_RED.ordinal
            }

            11 -> { //搶紅包結果
                ViewType.CHAT_RV_RE_GET.ordinal
            }

            14 -> { //跟注開獎結果 //讚 + 打賞
                ViewType.CHAT_RV_BET_LOTTERY.ordinal
            }
            19 -> { //投注機器人
                ViewType.CHAT_ROBOT_BET.ordinal
            }
            20 -> { //投注記錄
                if (data.fk == mChatConfigOutput?.fk) //資料的 fk 等於自己的 fk，代表是自己發的訊息
                    ViewType.CHAT_BET_RECORD_R.ordinal
                else
                    ViewType.CHAT_BET_RECORD_L.ordinal
            }
            else -> {
                Log.e(TAG, "未歸類的 chatType: ${data.chatType}")
                ViewType.UNKNOWN_TYPE.ordinal
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.CHAT_RV_MSG_L.ordinal -> MsgHolder(
                LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.chat_rv_msg_l, viewGroup, false)
            )
            ViewType.CHAT_RV_MSG_R.ordinal -> MsgHolder(
                LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.chat_rv_msg_r, viewGroup, false)
            )
            ViewType.CHAT_RV_WARNING.ordinal -> SystemMsgHolder(
                LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.chat_rv_system_msg, viewGroup, false)
            )
            ViewType.CHAT_RV_BET_FOLLOW_L.ordinal -> BetFollowHolder(
                LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.chat_rv_bet_follow_l, viewGroup, false)
            )
            ViewType.CHAT_RV_BET_FOLLOW_R.ordinal -> BetFollowHolder(
                LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.chat_rv_bet_follow_r, viewGroup, false)
            )
            ViewType.CHAT_BET_RECORD_L.ordinal -> RobotBetRecordHolder(
                LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.chat_rv_bet_record_l, viewGroup, false)
            )
            ViewType.CHAT_BET_RECORD_R.ordinal -> RobotBetRecordHolder(
                LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.chat_rv_bet_record_r, viewGroup, false)
            )
            ViewType.CHAT_RV_RED.ordinal -> RedPacketHolder(
                LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.chat_rv_red, viewGroup, false)
            )
            ViewType.CHAT_RV_RE_GET.ordinal -> RedGetMsgHolder(
                LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.chat_rv_system_red_get_msg, viewGroup, false)
            )
//            ViewType.CHAT_RV_PLAN_PUSH_L.ordinal -> ChatPlanPushHolder(viewGroup)
            else -> UnKnownMsgHolder(
                LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.chat_rv_unknown_msg, viewGroup, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        try {
            val data = mDataList[position]
            when (viewHolder) {
                // ============= 一般訊息 =============
                is MsgHolder -> {

                    setHeader(viewHolder.ivHead, data)
                    setNickName(viewHolder.tvName, data)
                    setLevelPic(viewHolder.ivLevel, data)
                    setTime(viewHolder.tvTime, data.curTime)

                    setContent(viewHolder, data) //判斷 文字、語音、圖片 訊息vgContentLayout
                    setBubbleSpeechColor(viewHolder.tvContent, viewHolder.vgContentLayout, data)

                    setChatButton(viewHolder.tvChat, data)

                }
                // ============= 系統設定消息 (禁言 & 解禁言 & 聊天時段) =============
                is SystemMsgHolder -> {
                    if (getItemViewType(position) == ViewType.CHAT_RV_WARNING.ordinal) {
                        viewHolder.iconWarning.visibility = View.VISIBLE
                        viewHolder.tvContent.setTextColor(Color.parseColor("#FF6600"))
                    } else {
                        viewHolder.iconWarning.visibility = View.GONE
                        viewHolder.tvContent.setTextColor(
                            ContextCompat.getColor(
                                activity,
                                R.color.skinTextLightForWhite
                            )
                        )
                    }

                    when (data.chatType) {
                        ChatMessageType.SPEAKING_BANNED.type -> viewHolder.tvContent.text =
                            viewHolder.tvContent.context.getString(R.string.chat)
                        ChatMessageType.CANCEL_SPEAKING_BANNED.type -> viewHolder.tvContent.text =
                            viewHolder.tvContent.context.getString(R.string.unbanned_already)
                        ChatMessageType.CHAT_TIME.type -> {
                            when (data.content) {
                                "1" -> viewHolder.tvContent.text =
                                    viewHolder.tvContent.context.getString(R.string.chat_open_chat)
                                "0" -> viewHolder.tvContent.text =
                                    viewHolder.tvContent.context.getString(R.string.chat_close_chat)
                                else -> viewHolder.tvContent.text = "${
                                    viewHolder.tvContent.context.getString(R.string.chat_system_msg)
                                }: ${data.content}"
                            }
                        }
                        ChatMessageType.SYSTEM_PRIVATE_MSG.type -> {
                            viewHolder.tvContent.text =
                                viewHolder.tvContent.context.getString(R.string.chat_above_history_msg)
                            viewHolder.iconWarning.visibility = View.GONE
                            viewHolder.tvContent.setTextColor(
                                ContextCompat.getColor(
                                    activity,
                                    R.color.skinTextLightForWhite
                                )
                            )
                        }

                        else -> viewHolder.tvContent.text = Html.fromHtml(data.content ?: "")
                    }

                    viewHolder.btnSystem.setOnClickListener {
                        // 2023/2/6 33188 未登錄/游客/登錄會員，進入「聊天室」頁面，點擊聊天室上顯示的「歷史消息」訊息文字，不需開啟「設置」頁面
//                        if (data.chatType == ChatMessageType.SYSTEM_PRIVATE_MSG.type) mOnChatListener?.onSystemMsg()
                    }

                }
                // ============= 跟注消息 =============
//                is BetFollowHolder -> {
//                    setHeader(viewHolder.ivHead, data)
//                    setNickName(viewHolder.tvName, data)
//                    setLevelPic(viewHolder.ivLevel, data)
//                    setTime(viewHolder.tvTime, data.curTime)
//
//                    setBubbleSpeechColor(viewHolder.tvContent, viewHolder.vgContentLayout, data)
////                    viewHolder.btnBetFollow.setBackgroundColor()
////                    viewHolder.tvContent.text = getBetFollowContent(data.content ?: "") //內容訊息
//                    setBetFollowContent(viewHolder, data.content)
//                    setFollowBetTextColor(viewHolder, data)
//                    viewHolder.btnBetFollow.isEnabled = !data.isFollowed && LoginManager.isLogin()
//                    if (!isLogin()) viewHolder.btnBetFollow.visibility = View.GONE
//                    viewHolder.btnBetFollow.setOnClickListener { v ->
//                        Utils.avoidFastDoubleClick(v)
//                        try {
//                            mOnChatListener?.onBetFollow(data, viewHolder.btnBetFollow)
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//                    if(AppConfigManager.isFeaturesHiding()) { viewHolder.btnBetFollow.invisible() }
//                }
                // ============= 跟注結果消息 =============
//                is BetLotteryHolder -> { //chatType: 14
//                    try {
//                        setHeader(viewHolder.ivHead, data)
//                        setNickName(viewHolder.tvName, data)
//                        setLevelPic(viewHolder.ivLevel, data)
//                        setTime(viewHolder.tvTime, data.curTime)
//
//                        setBubbleSpeechColor(viewHolder.tvContent, viewHolder.vgContentLayout, data)
//
//                        val webSocketChatGoodAndGiveInput = Gson().fromJson(
//                                data.content
//                                        ?: "", WebSocketChatGoodAndGiveInput::class.java
//                        )
////                    viewHolder.tvContent.text = getBetLotteryContent(webSocketChatGoodAndGiveInput)
//
//                        viewHolder.tvLotteryName.text = webSocketChatGoodAndGiveInput.gameName
//                        viewHolder.tvTurnNUm.text = webSocketChatGoodAndGiveInput.turnNum
//                        viewHolder.tvBetMoney.text =
//                                ArithUtil.toMoneyFormat(webSocketChatGoodAndGiveInput.totalMoney)
//                        viewHolder.tvWinnableMoney.text =
//                                ArithUtil.toMoneyFormat(webSocketChatGoodAndGiveInput.rewardRebate)
//
//                        //按讚
//                        viewHolder.btnGood.isEnabled = data.likeCount == null //判斷是否按過讚
//                        viewHolder.btnGive.isEnabled = !data.isRewarded
//
//                        val likeCount =
//                                if (data.likeCount == null) webSocketChatGoodAndGiveInput.likeCount else data.likeCount
//                        viewHolder.btnGood.text = "($likeCount)"
//
//                        viewHolder.btnGood.setOnClickListener {
//                            mOnChatListener?.onLike(data, viewHolder.btnGood)
//                        }
//
//                        //打賞
//                        viewHolder.btnGive.setOnClickListener {
//                            if (isPlayRecorder)
//                                return@setOnClickListener
//                            mOnChatListener?.onReward(data, viewHolder.btnGive)
//                        }
//
//                        setLotteryBetTextColor(viewHolder, data)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//                // ============= 計畫推送 =============
//                is ChatPlanPushHolder -> {
//                    viewHolder.setData(data)
//                    mDataList[position].isOutOfDate = viewHolder.setBetFollowButtonState(data.isOutOfDate)
//                    viewHolder.setBetFollowListener(object : ChatPlanPushHolder.ButtonListener {
//                        override fun onBetFollow(chatMessage: ChatMessage, btnBetFollow: Button, projectId: String) {
//                            mOnChatListener?.onGetFollowPlanProjectMoney(chatMessage, btnBetFollow, projectId)
//                        }
//                    })
//                }
                // ============= 紅包消息 =============
                is RedPacketHolder -> { //chatType: 10
                    setTime(viewHolder.tvTime, data.curTime)

                    viewHolder.btnOpen.setOnClickListener {
                        mOnChatListener?.onRedOpen(data)
                    }
                }

                is RedGetMsgHolder -> {
                    setRedGetMessage(viewHolder.tvContent, data)
                }

                else -> {
                    Log.e(TAG, "unknown chatType = ${data.chatType}")
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setChatButton(tvChat: TextView, data: ChatMessage) {
        if (mChatConfigOutput?.role?.type != "2") {//非客服身份
            if (data.role?.type == "2") {//對象是客服
                tvChat.visibility = View.VISIBLE
                tvChat.setOnClickListener {
                    /**
                     * mChatConfigOutput?.role?.type 為登入者 ， data.role?.type 為點擊者
                     * 登入者為 1或0 (會員or遊客 都可以到私聊) ，點到的是管理者是2，才會跳至下一頁
                     * 客服點客服要彈跳視窗訊息 (客服不可以與客服聊天)
                     * **/
                    Log.e("tvChatOnClick", "data = " + Gson().toJson(data))
//                    if (data.role?.type == "2" && mChatConfigOutput?.role?.type == "1" || mChatConfigOutput?.role?.type == "0") {
//                        EventBus.getDefault().post(
//                                ChatPrivateUIEvent(
//                                        ChatPrivateUIEvent.Type.OPEN_PRIVATE_CHAT_ROOM,
//                                        data.chatUserId, data.nickName
//                                )
//                        )
//                    } else if (data.role?.type == "2" && mChatConfigOutput?.role?.type == "2") {
//                        EventBus.getDefault().post(
//                                ChatPrivateUIEvent(
//                                        VietnamModule.context
//                                                .getString(R.string.chat_service_can_not_private_to_service), ""
//                                )
//                        )
//                    }
                }
            } else {
                tvChat.visibility = View.GONE
            }
        } else {
            tvChat.visibility = View.GONE
        }
    }

    private fun filterUrlMessage(data: ChatMessage): String {
        return if (data.content?.matches(urlFilterRegex) == true) {
            data.content?.replace(netFilterRegex, smileEmoticon) ?: ""
        } else {
            data.content ?: ""
        }
    }

    private fun setContent(viewHolder: MsgHolder, data: ChatMessage) {
        //20200616 紀錄問題：除了管理員，其他人一般訊息要濾掉 網址 變成笑臉圖案
        val msgContent = if (data.role?.type == "2" || data.chatType == 12 || data.chatType == 7) {
            data.content ?: ""
        } else {
            filterUrlMessage(data)
        }

//        when {
//            //--------語音--------
//            msgContent.startsWith("[audio:") -> {
//                viewHolder.btnAudio.visibility = View.VISIBLE
//                viewHolder.ivImage.visibility = View.GONE
//                viewHolder.tvContent.visibility = View.GONE
//
//                val webSocketVoiceOutput = WebSocketVoiceOutput((data.content ?: ""))
//
//                //判斷是否是播放中的語音
//                if (isPlayAudioId == data.id && mPlayerCurrentTime >= 0) {
//                    viewHolder.tvAudioTime.text = mPlayerCurrentTime.toString()
//                    Glide.with(activity).asGif().load(R.drawable.audio).into(viewHolder.ivAudio)
//                } else {
//                    viewHolder.tvAudioTime.text = webSocketVoiceOutput.length.toString()
//                    Glide.with(activity).load(R.drawable.ic_voice_2).into(viewHolder.ivAudio)
//                }
//
//                viewHolder.btnAudio.setOnClickListener(View.OnClickListener {
//                    if (isPlayRecorder) return@OnClickListener//若當前在錄音中，禁止撥放音檔
//
//                    //播放判斷
//                    if (isPlayMediaPlayer && isPlayAudioId == data.id) { //停止 //點擊當前正在撥放中的音檔
//                        stopPlayAudio()
//
//                    } else { //播放 //點擊其他一律播放
//                        playAudio(viewHolder.adapterPosition, webSocketVoiceOutput, data)
//                    }
//                    notifyDataSetChanged()
//                })
//            }
//
//            //--------圖片--------
//            msgContent.startsWith("[img:") -> {
//                viewHolder.btnAudio.visibility = View.GONE
//                viewHolder.ivImage.visibility = View.VISIBLE
//                viewHolder.ivImage.setImageResource(R.drawable.ic_image_load)
//
//                //文字 部分
//                if (msgContent.endsWith("]")) {
//                    viewHolder.tvContent.visibility = View.GONE
//                } else {
//                    viewHolder.tvContent.visibility = View.VISIBLE
//                    val contentMsg = msgContent.substring(
//                            (data.content
//                                    ?: "").indexOf("]") + 1
//                    )
//                    viewHolder.tvContent.text = contentMsg
//                }
//
//                //圖片 部分
//                val requestOptions = RequestOptions()
//                        .fitCenter()
//                        .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
////                        .override(ScreenUtils.getScreenWidth(activity)) //這會讓所有圖片大小和螢幕一樣寬
//                        .placeholder(R.drawable.ic_image_load)
//                        .error(R.drawable.ic_image_error)
//
////                val msgImageTarget = MsgImageTarget(viewHolder.ivImage)
//
//                val contentImg = msgContent.substring(
//                        (data.content
//                                ?: "").indexOf("[img:") + 5, msgContent.indexOf("]")
//                )
//
//                val url = if (contentImg.startsWith("http"))
//                    contentImg
//                else
//                    "$mChatApiUrl/$contentImg"
//
//                Glide.with(activity)
//                        .asBitmap()
//                        .load(url)
//                        .apply(requestOptions)
//                        .into(object : SimpleTarget<Bitmap>(
//                                ScreenUtils.getScreenWidth(activity),
//                                Target.SIZE_ORIGINAL
//                        ) {
//                            override fun onLoadFailed(errorDrawable: Drawable?) {
//                                viewHolder.ivImage.setImageResource(R.drawable.error)
//                            }
//
//                            override fun onResourceReady(
//                                    bitmap: Bitmap,
//                                    transition: Transition<in Bitmap>?
//                            ) {
//                                if (bitmap.width * 3 < ScreenUtils.getScreenWidth(activity)) {
//                                    viewHolder.ivImage.layoutParams.width = (bitmap.width * 3)
//                                }
//                                if (bitmap.height * 3 < ScreenUtils.getScreenWidth(activity)) {
//                                    viewHolder.ivImage.layoutParams.height = (bitmap.height * 3)
//                                }
//
//                                viewHolder.ivImage.maxHeight = ScreenUtils.getScreenHeight(activity)
//                                viewHolder.ivImage.maxWidth = ScreenUtils.getScreenWidth(activity)
//
//                                viewHolder.ivImage.setImageBitmap(bitmap)
//                            }
//                        })
//
//                viewHolder.ivImage.setOnClickListener {
//                    Utils.avoidFastDoubleClick(viewHolder.ivImage)
//                    if (!isPlayRecorder) {
//                        ChatPhotoDialog(url, activity).show()
//                    }
//                }
//            }
//
//            //--------文字--------
//            else -> {
//                viewHolder.btnAudio.visibility = View.GONE
//                viewHolder.ivImage.visibility = View.GONE
//                viewHolder.tvContent.visibility = View.VISIBLE
//
//                if (data.chatType == 12) {
//                    viewHolder.ivLevel.visibility = View.GONE //計畫消息 不顯示 腳色圖示
//                    viewHolder.tvContent.text = Html.fromHtml(msgContent)
//                } else {
//                    viewHolder.ivLevel.visibility = View.VISIBLE
//                    viewHolder.tvContent.text = msgContent
//                }
//            }
//        }
    }

    //打賞內容排版
//    private fun getBetLotteryContent(webSocketChatGoodAndGiveInput: WebSocketChatGoodAndGiveInput): String {
//        val returnValue = StringBuilder()
//        val nextLine = "\n"
//
//        /*returnValue.append(context.getString(R.string.chat_game_name))
//                .append(webSocketChatGoodAndGiveInput.gameName).append(nextLine)*/ //遊戲
//        /*returnValue.append(context.getString(R.string.chat_turnnum))
//                .append(webSocketChatGoodAndGiveInput.turnNum).append(nextLine)*/ //期號
//        /*returnValue.append(context.getString(R.string.chat_follow_money))
//                .append(webSocketChatGoodAndGiveInput.fllowMoney.toInt()).append(nextLine)*/ //跟注
//        /*returnValue.append(context.getString(R.string.chat_win_money))
//                .append(webSocketChatGoodAndGiveInput.winMoney)*/ //盈亏
//
//        return returnValue.toString()
//    }

    //跟注內容排版
    /*private fun getBetFollowContent(content: String): String {
        val returnValue = StringBuilder()
        val nextLine = "\n"
        try {
            val webSocketChatFallowInput = Gson().fromJson(content, WebSocketChatFallowInput::class.java)

            returnValue.append(MyApplication.getRes().getString(R.string.chat_game_name)).append(getPlayDataGameNameByGameId(webSocketChatFallowInput.gameId + "")).append(nextLine) //遊戲
            returnValue.append(MyApplication.getRes().getString(R.string.chat_turnnum)).append(webSocketChatFallowInput.turnNum).append(nextLine) //期號
            returnValue.append(MyApplication.getRes().getString(R.string.chat_content)).append(nextLine) //內容

            webSocketChatFallowInput.betList?.forEach { item ->
                val name = GameConfigManager.getPlayCateName(gameDataOutput.playCateMap[item.playCateId.toString()]!!, " ")

                if (item.betInfo.isNullOrEmpty()) {
                    returnValue.append(name)
                            .append(item.playName)
                            .append(MyApplication.getRes().getString(R.string.chat_money)).append(item.money.toInt()).append(nextLine)
                } else {
                    returnValue.append(name)
                            .append(item.playName)
                            .append("【").append(item.betInfo).append("】")
                            .append(MyApplication.getRes().getString(R.string.chat_money)).append(item.money.toInt()).append(nextLine)
                }
            }

            returnValue.deleteCharAt(returnValue.length - 1) //刪除最後一個 \n

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return returnValue.toString()
    }*/

    fun setData(newDataList: MutableList<ChatMessage>?, chatConfigOutput: ChatConfigOutput?) {
        mDataList = newDataList ?: mutableListOf()
        mChatConfigOutput = chatConfigOutput
        notifyDataSetChanged()
    }

    fun cleanData() {
        mDataList.clear()
        notifyDataSetChanged()
    }

    private fun setTextViewColor(textView: TextView, data: ChatMessage) {
        try {
            val textColor = data.role?.textColor
            if (!textColor.isNullOrEmpty())
                textView.setTextColor(ColorUtil.parseColor(textColor))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setBubbleSpeechColor(vgContentLayout: View, data: ChatMessage) {
        try {
            val colorBottom = data.role?.bgColor?.substring(0, 7)
            val colorTop = data.role?.bgColor?.substring(8, 15)

            vgContentLayout.background = BubbleSpeechUtil.getBubbleSpeech(
                vgContentLayout.context,
                colorTop.toString(),
                colorBottom.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setBubbleSpeechColor(textView: TextView, vgContentLayout: View, data: ChatMessage) {
        try {
            textView.setTextColor(ColorUtil.parseColor(data.role?.textColor))//文字顏色

            val colorBottom = data.role?.bgColor?.substring(0, 7)
            val colorTop = data.role?.bgColor?.substring(8, 15)

            vgContentLayout.background = BubbleSpeechUtil.getBubbleSpeech(
                vgContentLayout.context,
                colorTop.toString(),
                colorBottom.toString()
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //設定取得紅包的訊息
    private fun setRedGetMessage(tvContent: TextView, data: ChatMessage) {
        //將浮點數移除
        fun String.floatToInt(): Int {
            return this.toFloat().toInt()
        }

        var redGetText: String = ""
        try {
            val currentMoney: String = data.content.toString()
            val nickName = data.nickName.toString()
            val nickNamtText = "<font color='#ff0000'>${nickName}</font>"
            // VNFEED-514 搶紅包 - 希望可以隱藏搶紅包的金額
            if (currentMoney == "*") {
                redGetText =
                    activity.resources.getString(R.string.chat_message_system_red_get_secret,
                        nickNamtText)
            } else {
                val currentMoneyText =
                    "<font color='#ff0000'>${ArithUtil.toMoneyFormat(currentMoney.toDouble())}</font>"
                redGetText = activity.resources.getString(R.string.chat_message_system_red_get,
                    nickNamtText,
                    currentMoneyText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        tvContent.text = HtmlCompat.fromHtml(redGetText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        tvContent.text = Html.fromHtml(redGetText)
    }

    private fun setTime(textView: TextView, curTime: String?) {
        textView.text = curTime?.substring(11, 19)
    }

    private fun setLevelPic(imageView: ImageView, data: ChatMessage) {
        try {
            if (data.chatType != ChatMessageType.PLAN_ADMIN_MSG.type) {
                imageView.visibility = View.VISIBLE

                val requestOptions = RequestOptions()
                    .fitCenter()
//                        .placeholder(R.drawable.chat_icon_testuser)
                    .error(R.drawable.chat_icon_testuser)

                if (data.levelPic != null)
                    Glide.with(imageView.context)
                        .load(data.levelPic)
                        .apply(requestOptions)
                        .into(imageView)
                else
                    imageView.visibility = View.GONE

            } else {
                imageView.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setNickName(textView: TextView, data: ChatMessage) {
//        textView.ellipsize = TextUtils.TruncateAt.END
        textView.text = data.nickName

        if (!data.role?.nickTextColor.isNullOrEmpty())
            textView.setTextColor(ColorUtil.parseColor(data.role?.nickTextColor))

        textView.setOnClickListener {
            if (data.chatType != 10)
                mOnChatListener?.onSelectName(data.nickName ?: "")
        }
    }

    private fun setHeader(imageView: ImageView, data: ChatMessage) {
        when (data.role?.type) {
            "2" -> Glide.with(imageView.context).load(R.drawable.chat_head_u2).into(imageView)
            else -> {
                UserDataManager.loadIntoUserAvatarIcon(imageView, data.iconUrl)
            }
        }
        imageView.setOnClickListener {
            Log.e("點頭像", "nickName = " + data.nickName)
            if (data.chatType != 10)
                mOnChatListener?.onSelectName(data.nickName ?: "")
        }
    }

    private fun stopPlayAudio() {
        if (mMediaPlayer != null) {
            mCountDownTimer?.cancel()
            isPlayMediaPlayer = false
            mMediaPlayer?.stop()
            mMediaPlayer?.release()
            mMediaPlayer = null

            mPlayerCurrentTime = -1
            isPlayAudioId = -1
            isPlayMediaPlayer = false
        }
    }

    fun releaseMediaPlay() {
        if (mMediaPlayer != null) {
            stopPlayAudio()
            mMediaPlayer?.release()
            mMediaPlayer = null
        }
    }


    //item layout
    class MsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivHead: ImageView = itemView.findViewById(R.id.iv_head)
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val ivLevel: ImageView = itemView.findViewById(R.id.iv_level)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val vgContentLayout: View = itemView.findViewById(R.id.vg_content_layout)

        val btnAudio: View = itemView.findViewById(R.id.btn_audio)
        val ivAudio: ImageView = itemView.findViewById(R.id.iv_audio)
        val tvAudioTime: TextView = itemView.findViewById(R.id.tv_audio_time)
        val ivImage: ImageView = itemView.findViewById(R.id.iv_image)
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)

        val tvChat: TextView = itemView.findViewById(R.id.txv_chat)
    }

    class SystemMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnSystem: View = itemView.findViewById(R.id.btn_system)
        val iconWarning: View = itemView.findViewById(R.id.icon_warning)
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
    }

    class BetFollowHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivHead: ImageView = itemView.findViewById(R.id.iv_head)
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val ivLevel: ImageView = itemView.findViewById(R.id.iv_level)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val vgContentLayout: View = itemView.findViewById(R.id.vg_content_layout)
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)

        val tvLotteryName: TextView = itemView.findViewById(R.id.tv_lottery_name)
        val tvBetFollow: TextView = itemView.findViewById(R.id.tvBetFollow)

        //        val tvGameName: TextView = itemView.findViewById(R.id.tvGameName)
        val rvBet: RecyclerView = itemView.findViewById(R.id.betRecyclerView)

        //        val tvBetContent: TextView = itemView.findViewById(R.id.tvBetContent)
        val tvChosen: TextView = itemView.findViewById(R.id.tvChosen)
        val tvMoney: TextView = itemView.findViewById(R.id.tvMoney)

        //        val tvGameNameTitle: TextView = itemView.findViewById(R.id.tvGameNameTitle)
//        val tvBetContentTitle: TextView = itemView.findViewById(R.id.tvBetContentTitle)
        val tvChosenTitle: TextView = itemView.findViewById(R.id.tvChosenTitle)
        val tvMoneyTitle: TextView = itemView.findViewById(R.id.tvMoneyTitle)

        val btnBetFollow: Button = itemView.findViewById(R.id.btn_bet_follow)
    }

    class UnKnownMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Todo: 臨時解決方式，因為其他訊息不該顯示，理論上要過濾
    }

    class RobotBetRecordHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivHead: ImageView = itemView.findViewById(R.id.iv_head)
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        var tvTitle: TextView = itemView.findViewById(R.id.tv_content_title)
        val tvIssue: TextView = itemView.findViewById(R.id.tv_issue_title)
        val tvTotal: TextView = itemView.findViewById(R.id.tv_total)
        val tvTotalMoney: TextView = itemView.findViewById(R.id.tv_total_money)
        val vgContentLayout: View = itemView.findViewById(R.id.vg_content_layout)

        var tvTime: TextView = itemView.findViewById(R.id.tv_time)
        var itemContainer: LinearLayout = itemView.findViewById(R.id.item_container)
    }

    class RedPacketHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_buyer_name)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        val btnOpen: TextView = itemView.findViewById(R.id.btn_open)
    }

    //取得紅包通知
    class RedGetMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
    }
}

