package org.cxct.sportlottery.ui.chat


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentChatBinding
import org.cxct.sportlottery.net.chat.data.UnPacketRow
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.repository.ChatRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.chat.adapter.ChatMessageListAdapter3
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.selectpicture.PictureSelectorUtils
import org.cxct.sportlottery.view.afterTextChanged
import org.cxct.sportlottery.view.insertEmoji
import org.cxct.sportlottery.view.isVisible
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import timber.log.Timber
import java.io.File
import java.util.ArrayList

/**
 * @author kevin
 * @create 2022/3/15
 * @description init 失敗重新執行 getSign 最多嘗試三次
 * @app_destination 聊天室 - 大廳群聊
 */
class ChatFragment : BaseSocketFragment<ChatViewModel, FragmentChatBinding>(),
    View.OnClickListener {

    private var isShowScrollBottom = false //列表正在底部附近才會自動滾至下一則訊息

    private val chatMessageListAdapter by lazy {
        ChatMessageListAdapter3(
            onPhotoClick = { viewModel.showPhoto(it) },
            onUserAvatarClick = {
                viewModel.tagPairList.add(it)
                addTagMessage(it.second) //加入"@nickName"訊息
            },
            onRedEnvelopeClick = { packetId, packetType ->
                createRedPacketDialog(packetId, packetType, false) //非管理原才會顯示打開按鈕，才可點擊
            }
        )
    }

    private val chatWelcomeAdapter by lazy { ChatWelcomeAdapter(viewLifecycleOwner) }
    private var redPacketDialog: RedPacketDialog? = null
    private var redEnvelopeListDialog: RedEnvelopeListDialog? = null

    //訊息列表用日期提示ItemDecoration
    private val headerItemDecoration by lazy {
        ChatDateHeaderItemDecoration(binding.rvChatMessage) { itemPosition ->
            if (itemPosition >= 0 && itemPosition < chatMessageListAdapter.dataCount()) {
                chatMessageListAdapter.isDateItem(chatMessageListAdapter.getItem(itemPosition))
            } else {
                false
            }
        }
    }

    override fun onInitView(view: View) {
        initView()
        setWindowSoftInput(
            float = binding.vChatAction,
            setPadding = false,
            editText = binding.vChatAction.etInput,
            onChanged = {
//                LogUtil.d("SoftInput visibility = ${hasSoftInput()}")
            }
        )
    }

    private val loadingHolder by lazy { Gloading.wrapView(binding.rvChatMessage) }

    override fun onBindViewStatus(view: View) {
        initObserve()
        chatWelcomeAdapter.activity = activity as ChatActivity?
        loadingHolder.showLoading()
    }

    override fun onPause() {
        super.onPause()
        handleChatRoomIsReadyEvent(false)
    }

    private fun initView() {
        binding.vChatAction.ivUploadImage.setOnClickListener(this)
        binding.vChatAction.ivSend.setOnClickListener(this)
        binding.ivDownBtn.setOnClickListener(this)
        binding.rvWelcome.layoutManager = SmoothLinearLayoutManager(context)
        binding.rvWelcome.setLinearLayoutManager()
        binding.rvWelcome.addItemDecoration(
            SpaceItemDecoration(
                requireContext(),
                R.dimen.recyclerview_chat_welcome_item_dec_spec
            )
        )
        binding.rvWelcome.adapter = chatWelcomeAdapter
        binding.rvChatMessage.setLinearLayoutManager().stackFromEnd = true
        binding.rvChatMessage.adapter = chatMessageListAdapter
        chatMessageListAdapter.setEmptyView(getChatEmptyView(binding.root.context))

        OverScrollDecoratorHelper.setUpOverScroll(
            binding.rvChatMessage,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL
        )

        //設定發送按鈕是否為可點擊的狀態
        binding.vChatAction.etInput.afterTextChanged {
            binding.vChatAction.setSendStatus(it.trim().isNotEmpty() && viewModel.checkIsSpeak())
        }
    }

    private fun getChatEmptyView(context: Context): View {
        val rootView = LinearLayout(context)
        rootView.gravity = Gravity.CENTER
        rootView.setPadding(0, 0, 0, 90.dp)
        val textView = AppCompatTextView(context)
        rootView.addView(textView, LinearLayout.LayoutParams(256.dp, 199.dp))
        textView.setBackgroundResource(R.drawable.img_chat_emptymessage)
        textView.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        textView.textSize = 16f
        textView.setTextColor(context.getColor(R.color.color_A7B2C4))
        textView.setText(R.string.no_data)
        return rootView
    }

    private fun setChatMsgScrollListener(isReady: Boolean) {
        if (!isReady || binding.rvChatMessage.tag != null) {
            return
        }
        binding.rvChatMessage.tag = true
        binding.rvChatMessage.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                //region 處理聊天訊息中日期提示隱藏及顯示. 實際上懸浮的日期提示為RecyclerView的ItemDecoration所繪製的, 故需要在頂部顯示該日期時隱藏列表中對應的Item, 離開頂部時才顯示.
                //可視範圍的第一個Item 若為日期提示則隱藏

                val layoutManager = (recyclerView.layoutManager as LinearLayoutManager)
                val firstPosition = layoutManager.findFirstVisibleItemPosition()
                val itemData = chatMessageListAdapter.getItemOrNull(firstPosition)
                val firstView = layoutManager.findViewByPosition(firstPosition)
                if (itemData != null && chatMessageListAdapter.isDateItem(itemData)) {
                    firstView?.visibility = View.GONE
                }

                //可視範圍的第二個Item 若為日期提示則顯示
                val secondPosition = firstPosition + 1
                val secondItemData = chatMessageListAdapter.getItemOrNull(secondPosition)
                val secondView = layoutManager.findViewByPosition(secondPosition)
                if (secondItemData != null && chatMessageListAdapter.isDateItem(secondItemData)) {
                    secondView?.visibility = View.VISIBLE
                }
                //endregion
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    smothToBottom = false
                    //一滾動就顯示懸浮日期提示
                    if (!viewModel.isFirstInit) {
                        headerItemDecoration.setHeaderVisibility(true)
                        clearDateFloatingTipsRunnable()
                    }
                    return
                }

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //滾動靜止一秒鐘後隱藏懸浮日期提示
                    postDateFloatingTipsRunnable()
                    postDownBtnRunable()
                }
            }
        })

    }

    private fun setDownBtnEnable(enable: Boolean) = binding.ivDownBtn.run {
        if (tag != null && isShowScrollBottom == enable) {
            return@run
        }

        isShowScrollBottom = enable
        tag = enable
        val value = if (enable) 1f else 0f
        animate()
            .scaleX(value)
            .scaleY(value)
            .alpha(value)
            .setDuration(200)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.ivDownBtn.tag = null
                }
            })
            .start()
    }

    private val changeDownBtnRunable by lazy {
        java.lang.Runnable {
            val lm = (binding.rvChatMessage.layoutManager as LinearLayoutManager)
            val last = lm.findLastCompletelyVisibleItemPosition()
            val count = chatMessageListAdapter.dataCount()
            if (last == -1) {
                setDownBtnEnable(false)
            } else {
                setDownBtnEnable(last < count - 2)
            }
        }
    }

    private inline fun clearDownBtnRunable() {
        binding.root.removeCallbacks(changeDownBtnRunable)
    }

    private fun postDownBtnRunable() {
        clearDownBtnRunable()
        binding.root.postDelayed(changeDownBtnRunable, 150)
        if (smothToBottom) { // 如果是手动点击滚动到底部，有可能滚动到底部不彻底则再触发一次强制滚动到底部
            chatListScrollToBottom(false)
        }
    }

    private val hideDateFloatingTipsRunnable by lazy {
        java.lang.Runnable {
            val layoutManager = (binding.rvChatMessage.layoutManager as LinearLayoutManager)
            val firstPosition = layoutManager.findFirstVisibleItemPosition()
            val firstItemData =
                chatMessageListAdapter.getItemOrNull(firstPosition) ?: return@Runnable

            //判斷是否為客製化MessageType
            //判斷是否為日期提示且日期內容
            //判斷可視範圍內第一項內容文字是否與標題內容文字相符
            //皆相符的話不須隱藏Header
            if (!(chatMessageListAdapter.isDateItem(firstItemData)
                        && (headerItemDecoration.getHeaderItemView()
                    ?.findViewById<TextView>(R.id.tvDate)?.text?.toString() == firstItemData.content))
            ) {

                headerItemDecoration.setHeaderVisibility(
                    isVisible = false,
                    refreshRecyclerView = true
                )
            }
        }
    }

    private inline fun clearDateFloatingTipsRunnable() {
//        binding.root.removeCallbacks(hideDateFcoloatingTipsRunnable)
    }

    private fun postDateFloatingTipsRunnable() {
//        clearDateFloatingTipsRunnable()
//        binding.root.postDelayed(hideDateFloatingTipsRunnable, 1_000)
    }

    /**
     * 加入"@nickName"訊息，並判斷前後是否需要補空格
     */
    private fun addTagMessage(tagUser: String) = binding.vChatAction.etInput.apply {
        if (!isEnabled) return@apply

        val input = text.toString().ifEmpty { "" }
        val space = " "
        var plusTag = if (input.isEmpty()) {
            input.plus(tagUser)
        } else {
            if (input.endsWith(space)) {
                input.plus(tagUser)
            } else {
                input.plus(space).plus(tagUser) //當tag的前面沒空格時，前面補空格
            }
        }
        plusTag = plusTag.plus(space) //點tag，後面補空格
        setText(plusTag)
        text?.let { setSelection(it.length) }
    }

    private fun onLuckyBagResultEvent(chatEvent: ChatEvent.GetLuckyBagResult) {
        if (chatEvent.luckyBagResult.succeeded()) {            //TODO Bill 更新聊天室列表的紅包狀態
            redPacketDialog?.showRedPacketOpenDialog(chatEvent.luckyBagResult.getData().toDoubleS())

            return
        }

        Timber.v("Bill===>開完紅包${chatEvent.luckyBagResult}")

        if (chatEvent.luckyBagResult.code == ChatViewModel.ChatErrorCode.NOT_ENOUGH_BET_AND_RECH_MONEY
            && chatEvent.luckyBagResult.code == ChatViewModel.ChatErrorCode.GOT_ALREADY
        ) {
            showPromptDialog(getString(R.string.chat_hint), chatEvent.luckyBagResult.msg) {
                dismissDiaolg()
            }
            return
        }

        if (chatEvent.luckyBagResult.code == ChatViewModel.ChatErrorCode.PW_ERROR) {
            redPacketDialog?.showPWErrorHint()
            return
        }

        showPromptDialog(getString(R.string.chat_hint), chatEvent.luckyBagResult.msg) {
            dismissDiaolg()
        }

    }

    private fun onGetUnPacket(chatEvent: ChatEvent.GetUnPacket) {

        redEnvelopeListDialog = null
        val cxt = context ?: return
        val getUnPacketList = ChatRepository.unPacketList?.toMutableList() ?: return

        // 之前的逻辑就是这样如果走不到下面 redEnvelopeListDialog 就会为空
        redEnvelopeListDialog = RedEnvelopeListDialog(
            getUnPacketList,
            object : RedEnvelopeListDialog.Listener { //開紅包
                override fun onDialogCallback(selected: UnPacketRow) {
                    createRedPacketDialog(
                        selected.id.toString(),
                        selected.packetType,
                        chatEvent.isAdmin
                    )
                }
            })
        binding.chatRedEnpView.setOnClickListener { redEnvelopeListDialog?.show(childFragmentManager) }
    }

    private inline fun initChatEventObserver() =
        viewModel.chatEvent.collectWith(lifecycleScope) { chatEvent ->

            when (chatEvent) {
                is ChatEvent.ChatRoomIsReady -> { //已訂閱roomId且已更新歷史訊息
                    setChatMsgScrollListener(chatEvent.isReady)
                    loadingHolder.showLoadSuccess()
                }

                is ChatEvent.UpdateList -> {
                    chatMessageListAdapter.setChatList(chatEvent.chatMessageList)
                    if (viewModel.isFirstInit) {
                        binding.rvChatMessage.removeItemDecoration(headerItemDecoration)
                        binding.rvChatMessage.addItemDecoration(headerItemDecoration)
                    }
                    chatListScrollToBottom(false)
                }

                is ChatEvent.ChatMessage -> {
                    chatMessageListAdapter.onNewMsg(chatEvent.chatMessage)
                }

                is ChatEvent.RemoveMsg -> {
                    chatMessageListAdapter.removeMsg(chatEvent.msgId)
                }

                is ChatEvent.UserEnter -> {

                }

                is ChatEvent.UserLeave -> {

                }

                is ChatEvent.SubscribeRoom -> {
                    Timber.i("[Chat] 訂閱 roomId -> ${chatEvent.roomId}")
                    viewModel.subscribeChatRoom(chatEvent.roomId.toString()) //需要修改socket token
                }

                is ChatEvent.UnSubscribeRoom -> {
                    Timber.i("[Chat] 解除訂閱 roomId -> ${chatEvent.roomId}")
                    viewModel.unSubscribeChatRoom(chatEvent.roomId.toString()) //需要修改socket token
                }

                is ChatEvent.SubscribeChatUser -> {
                    Timber.i("[Chat] 訂閱 userId -> ${chatEvent.userId}")
                    viewModel.subscribeChatUser(chatEvent.userId.toString())
                }

                is ChatEvent.UnSubscribeChatUser -> {
                    Timber.i("[Chat] 解除訂閱 userId -> ${chatEvent.userId}")
                    viewModel.unSubscribeChatUser(chatEvent.userId.toString())
                }

                is ChatEvent.InitFail -> {
                    onError(chatEvent.message)
                }

                is ChatEvent.SendMessageStatusEvent -> {
                    binding.vChatAction.apply {
                        if(chatEvent.sendTextEnabled){
                            //输入框表情icon点击
                            setOnEmojiClick {
                                if (binding.chatEmojiView.isVisible()) {
                                    binding.chatEmojiView.gone()
                                } else {
                                    binding.chatEmojiView.visible()
                                }
                            }
                        }else{
                            //禁言 需要显示emoji但不能点击
                            setOnEmojiSilence()
                        }

                        setInputMaxLength(chatEvent.textMaxLength)
                        setInputStatus(chatEvent.sendTextEnabled)
                        setSendStatus(
                            chatEvent.sendTextEnabled && etInput.text.toString().isNotEmpty()
                        )
                        setUploadImageStatus(chatEvent.uploadImgEnable)
                        if (LoginRepository.isLogined() && !chatEvent.sendTextEnabled && !chatEvent.uploadImgEnable) {
                            showToast(getString(R.string.chat_you_banned))
                        }
                    }

                }


                is ChatEvent.NoMatchRoom -> {
                    onError(getString(R.string.N922))
                }

                is ChatEvent.InsertMessage -> {
                    insertItem(chatEvent.isMe)
                }

                is ChatEvent.InsertPic -> {
                    insertItem(chatEvent.isMe)
                }

                is ChatEvent.RedEnvelope -> {
                    createRedPacketDialog(
                        chatEvent.packetId,
                        chatEvent.packetType,
                        chatEvent.isAdmin
                    )
                    insertItem()
                }

                is ChatEvent.PersonalRedEnvelope -> {
                    createRedPacketDialog(
                        chatEvent.packetId,
                        chatEvent.packetType,
                        chatEvent.isAdmin
                    )
                    insertItem()
                }

                is ChatEvent.Silence -> {
                    if (LoginRepository.isLogined()) {
                        showToast(getString(R.string.chat_you_banned))
                    }
                }

                is ChatEvent.UnSilence -> {
                    showToast(getString(R.string.chat_you_can_chatting))
                }

                is ChatEvent.KickOut -> {
                    showPromptDialog(
                        getString(R.string.LT003),
                        getString(R.string.chat_you_kicked_out)
                    ) {
                        dismissDiaolg()
                        activity?.onBackPressed()
                    }
                }

                is ChatEvent.UserSystemPrompt -> {
                    insertItem()
                }

                is ChatEvent.ScrollToBottom -> {
                    binding.rvChatMessage.post {
                        chatListScrollToBottom(false)
//                        binding.rvChatMessage.scrollToPosition(chatMessageListAdapter.itemCount - 1) //滑動到底部不需動畫效果
                    }
                }

                is ChatEvent.RedEnvelopeMsg -> {
                    insertItem()
                }

                is ChatEvent.OpenLuckyBag -> {
//                 redPacketDialog?.show()
                }

                is ChatEvent.GetLuckyBagResult -> {
                    onLuckyBagResultEvent(chatEvent)
                }

                is ChatEvent.UpdateUserEnterList -> {
                    chatWelcomeAdapter.userEnter(chatEvent.chatUser)
                }

                is ChatEvent.IsAdminType -> {
                    chatMessageListAdapter.isAdmin = chatEvent.isAdmin
                }

                //獲取未領取紅包列表
                is ChatEvent.GetUnPacket -> {
                    onGetUnPacket(chatEvent)
                }

                //判斷顯示懸浮紅包
                is ChatEvent.ChatRedEnpViewStatus -> {
                    binding.chatRedEnpView.isVisible = chatEvent.isShow
                }

                //更新未領取紅包列表
                is ChatEvent.UpdateUnPacketList -> {
                    ChatRepository.unPacketList?.let { redEnvelopeListDialog?.setPackets(it) }
                }

                is ChatEvent.WinRedEnvelope -> {
                    insertItem()
                }

                else -> Unit
            }
        }

    private fun onError(errorMsg: String, block: (() -> Unit)? = null) {
        showErrorPromptDialog(getString(R.string.prompt), errorMsg) {
            if (block != null) {
                block.invoke()
            } else {
                activity?.onBackPressed()
            }
        }
    }

    private fun initObserve() {

        initChatEventObserver()

        //上傳圖片server的result
        viewModel.editIconUrlResult.observe(viewLifecycleOwner) {

            val iconUrlResult = it.getContentIfNotHandled() ?: return@observe
            if (iconUrlResult.success) {
                val chatImgUrl = iconUrlResult.t

                //region 選擇照片會離開聊天室頁面，返回後需等重新訂閱，聊天室就緒後才能發送圖片訊息
                if (chatImgUrl.isNullOrEmpty()) {
                    return@observe
                }
                val sendPictureMsgDialog = SendPictureMsgDialog.newInstance(chatImgUrl,
                    object : SendPictureMsgDialog.SendMsgListener {
                        override fun onSend(msg: String, chatType: ChatType) {
                            val msgEvent = LiveMsgEntity()
                            msgEvent.content = msg
                            msgEvent.type = chatType.code.toString()
                            viewModel.chatSendMessage(msgEvent)
                        }
                    })

                sendPictureMsgDialog.showAllowingStateLoss(
                    childFragmentManager,
                    "SendPictureMsgDialog"
                )
                return@observe
            }

            onError(iconUrlResult.msg) { }
        }

        viewModel.chatStickerEvent.observe(this) {
            binding.chatEmojiView.initColumn(it)
            //表情选中emoji
            binding.chatEmojiView.setOnEmojiSelect {
                binding.vChatAction.etInput.insertEmoji(it)
            }
            //发送图片表情
            binding.chatEmojiView.setOnPictureSelect {
                binding.vChatAction.ivEmoji.performClick()
                viewModel.chatSendPictureMessage(it)
            }
            binding.chatEmojiView.setOnClickListener {
                binding.vChatAction.ivEmoji.performClick()
            }
        }
    }

    private fun insertItem(isMe: Boolean = false) {
        if (isMe) {
            chatListScrollToBottom(isSmooth = false) //發送自己的訊息後，需快速至聊天室底部
        } else if (!isShowScrollBottom) {
            chatListScrollToBottom(isSmooth = true)
        }
    }

    private fun showToast(msg: String) {
        binding.vChatToast.tvToast.text = msg
        activity?.window?.decorView?.let {
            binding.vChatToast.bvBlock.setupWith(it.rootView as ViewGroup)
                .setFrameClearDrawable(it.background)
                .setBlurRadius(8f)
        }
        binding.vChatToast.root.isVisible = true
        binding.root.postDelayed({ binding.vChatToast.root.isVisible = false }, 2000)
    }

    private fun setupSendMessage(inputString: String): String {
        var result = inputString
//        Timber.e("chat result1: $result")
        val pairs = viewModel.tagPairList
        if (pairs.isNotEmpty() && result.contains("@")) {
            for (pair in pairs) {
//                Timber.e("chat pair: $pair")
                result = result.replace(pair.second, pair.first) //Pair<"[@:userId]", "@nickName">
            }
        } else {
            result = inputString
        }
//        Timber.e("chat result2: $result")
        return result
    }

    override fun onClick(v: View) {
        if (v == binding.vChatAction.ivUploadImage) {
            pickPhoto()
            requireActivity().hideSoftKeyboard()
            return
        }

        if (v == binding.vChatAction.ivSend) {
            //隐藏表情板块
            if (binding.vChatAction.expandEmoji) {
                binding.vChatAction.ivEmoji.performClick()
            }

            val input = binding.vChatAction.etInput.text.toString().replace("\n", "")
            if (input.isNullOrEmpty()) return
            if (!v.canDelayClick()) {
                context?.let { showToast(getString(R.string.chat_speaking_too_often)) }
                return
            }
            v.delayTime = 1500
            val sendMessage = setupSendMessage(input.toString())
            val liveMsgEntity = LiveMsgEntity()
            liveMsgEntity.content = sendMessage
            liveMsgEntity.type = ChatType.CHAT_SEND_TEXT_MSG.code.toString()
            viewModel.chatSendMessage(liveMsgEntity)
            binding.vChatAction.etInput.setText("")
            viewModel.tagPairList.clear()
            requireActivity().hideSoftKeyboard()
            return
        }

        if (v == binding.ivDownBtn) {
            chatListScrollToBottom(isSmooth = true)
            return
        }
    }

    //選擇相片
    private fun pickPhoto() {
        activity?.let { PictureSelectorUtils.selectPiture(it, 1, 16, 9, selectMediaListener) }
    }

    private val selectMediaListener = object : OnResultCallbackListener<LocalMedia> {
        override fun onResult(result: ArrayList<LocalMedia>?) {

            // 图片选择结果回调
            // LocalMedia 里面返回三种path
            // 1.media.getPath(); 为原图path
            // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
            // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
            // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的

            val media = result?.firstOrNull() //這裡應當只會有一張圖片
            val path = when {
                media?.isCompressed == true -> media.compressPath
                media?.isCut == true -> media.cutPath
                else -> media?.path
            }

            val file = File(path!!)
            if (file.exists()) {
                uploadImg(file)
                return
            }

            ToastUtil.showToastInCenter(
                requireContext(),
                getString(R.string.error_reading_file)
            )

        }

        override fun onCancel() {
            Timber.i("PictureSelector Cancel")
        }
    }

    //處理聊天室是否就緒的event
    private fun handleChatRoomIsReadyEvent(isReady: Boolean) = launch {
        viewModel.emitChatRoomIsReady(isReady)
    }

    private fun uploadImg(file: File) {
        val userId = LoginRepository.userId.toString()
        val uploadImgRequest =
            UploadImgRequest(userId, file, UploadImgRequest.PlatformCodeType.AVATAR)
        viewModel.uploadImage(uploadImgRequest)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancleJob()
    }

    private fun cancleJob() = runWithCatch {
        clearDateFloatingTipsRunnable()
        clearDownBtnRunable()
        chatWelcomeAdapter.stop()
    }

    private var smothToBottom = false

    private fun chatListScrollToBottom(isSmooth: Boolean) {
        smothToBottom = isSmooth
        binding.rvChatMessage.postDelayed({
            val position = chatMessageListAdapter.itemCount - 1
            if (isSmooth) {
                binding.rvChatMessage.smoothScrollToPosition(position)
            } else {
                val layoutManager = binding.rvChatMessage.layoutManager as LinearLayoutManager
                layoutManager.scrollToPositionWithOffset(position, Integer.MIN_VALUE)
            }
        }, 200)
    }

    fun createRedPacketDialog(packetId: String, packetType: Int, isAdmin: Boolean) {
        dismissDiaolg()
        redPacketDialog = context?.let {
            RedPacketDialog(
                RedPacketDialog.PacketListener(
                    onClickListener = { packetId, watchWord ->
                        viewModel.getLuckyBag(packetId, watchWord)
                        requireActivity().hideSoftKeyboard()
                    },
                    onCancelListener = {
                    },
                    onCompleteListener = { packetId ->
                        //TODO Bill 更新聊天室列表的紅包狀態
                    },
                    goRegisterPageListener = {
                        showErrorPromptDialog(
                            title = getString(R.string.chat_hint),
                            message = getString(R.string.chat_only_after_logging_in),
                        ) {
                            dismissDiaolg()
                            //startActivity(Intent(requireActivity(), RegisterActivity::class.java)) //TODO Bill 不確定PM要不要直接跳轉註冊畫面 先放著
                        }
                    }
                ),
                packetId,
                packetType
            )
        }
        if (!isAdmin) redPacketDialog?.show(childFragmentManager) //非管理員才顯示紅包彈窗
    }

    private fun dismissDiaolg() {
        redEnvelopeListDialog?.dismissAllowingStateLoss()
        redPacketDialog?.dismiss()
    }
}
