package org.cxct.sportlottery.ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.*
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.coroutines.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentChatBinding
import org.cxct.sportlottery.network.chat.getUnPacket.Row
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.repository.ChatRepository
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.login.afterTextChanged
import org.cxct.sportlottery.ui.profileCenter.profile.GlideEngine
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.layoutmanager.SocketLinearManager
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

/**
 * @author kevin
 * @create 2022/3/15
 * @description init 失敗重新執行 getSign 最多嘗試三次
 * @app_destination 聊天室 - 大廳群聊
 */
class ChatFragment : BaseSocketFragment<ChatViewModel>(ChatViewModel::class), View.OnClickListener {

    private lateinit var binding: FragmentChatBinding

    private var isShowScrollBottom = false //列表正在底部附近才會自動滾至下一則訊息

    private var jobScroll: Job? = null

    private val chatMessageListAdapter by lazy {
        ChatMessageListAdapter()
    }

    private val chatWelcomeAdapter by lazy {
        ChatWelcomeAdapter()
    }

    private var redPacketDialog: RedPacketDialog? = null
    private var redEnvelopeListDialog: RedEnvelopeListDialog? = null
    private var sendPictureMsgDialog: SendPictureMsgDialog? = null

    //訊息列表用日期提示ItemDecoration
    private val headerItemDecoration by lazy {
        ChatDateHeaderItemDecoration(binding.rvChatMessage) { itemPosition ->
            if (itemPosition >= 0 && itemPosition < (binding.rvChatMessage.adapter?.itemCount
                    ?: 0)
            ) {
                val itemData = chatMessageListAdapter.dataList[itemPosition]
                itemData.isCustomMessage && itemData.type == ChatMsgCustomType.DATE_TIP
            } else false
        }
    }
    private var hideDateFloatingTipsJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserve()
        viewModel.getHistoryMessageList()
    }

    override fun onPause() {
        super.onPause()
        handleChatRoomIsReadyEvent(false)
    }

    private fun initView() {
        binding.vChatAction.binding.ivUploadImage.setOnClickListener(this)
        binding.vChatAction.binding.ivSend.setOnClickListener(this)
        binding.ivDownBtn.setOnClickListener(this)
        chatWelcomeAdapter.activity = activity as ChatActivity?
        binding.rvWelcome.apply {
            layoutManager = SmoothLinearLayoutManager(context)
            addItemDecoration(
                SpaceItemDecoration(
                    requireContext(),
                    R.dimen.recyclerview_chat_welcome_item_dec_spec
                )
            )
            adapter = chatWelcomeAdapter
        }
        binding.rvChatMessage.apply {
            layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = chatMessageListAdapter
        }
        binding.rvChatMessage.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //region 處理聊天訊息中日期提示隱藏及顯示. 實際上懸浮的日期提示為RecyclerView的ItemDecoration所繪製的, 故需要在頂部顯示該日期時隱藏列表中對應的Item, 離開頂部時才顯示.
                //可視範圍的第一個Item 若為日期提示則隱藏
                val layoutManager = (recyclerView.layoutManager as LinearLayoutManager)
                isShowScrollBottom =
                    layoutManager.findLastCompletelyVisibleItemPosition() < chatMessageListAdapter.dataList.size - 3
                binding.ivDownBtn.isVisible = isShowScrollBottom

                val firstPosition = layoutManager.findFirstVisibleItemPosition()
                val itemData = chatMessageListAdapter.dataList.getOrNull(firstPosition)
                val firstView = layoutManager.findViewByPosition(firstPosition)
                if ((itemData?.isCustomMessage == true) && (itemData.type == ChatMsgCustomType.DATE_TIP)) {
                    firstView?.visibility = View.GONE
                }

                //可視範圍的第二個Item 若為日期提示則顯示
                val secondPosition = firstPosition + 1
                val secondItemData = chatMessageListAdapter.dataList.getOrNull(secondPosition)
                val secondView = layoutManager.findViewByPosition(secondPosition)
                if ((secondItemData?.isCustomMessage == true) && (secondItemData.type == ChatMsgCustomType.DATE_TIP)) {
                    secondView?.visibility = View.VISIBLE
                }
                //endregion
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        //滾動靜止一秒鐘後隱藏懸浮日期提示
                        hideDateFloatingTipsJob = CoroutineScope(Dispatchers.IO).launch {
                            delay(1000)
                            launch(Dispatchers.Main) {
                                val layoutManager =
                                    (recyclerView.layoutManager as LinearLayoutManager)
                                val firstPosition = layoutManager.findFirstVisibleItemPosition()
                                val firstItemData =
                                    chatMessageListAdapter.dataList.getOrNull(firstPosition)

                                //判斷是否為客製化MessageType
                                //判斷是否為日期提示且日期內容
                                //判斷可視範圍內第一項內容文字是否與標題內容文字相符
                                //皆相符的話不須隱藏Header
                                if (!((firstItemData?.isCustomMessage == true)
                                            && (firstItemData.type == ChatMsgCustomType.DATE_TIP)
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
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        //一滾動就顯示懸浮日期提示
                        if (!viewModel.isFirstInit) {
                            headerItemDecoration.setHeaderVisibility(true)
                            hideDateFloatingTipsJob?.cancel() //取消一秒後隱藏懸浮日期提示的工作
                        }
                    }
                }
            }
        })
        OverScrollDecoratorHelper.setUpOverScroll(
            binding.rvChatMessage,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL
        )

        //設定發送按鈕是否為可點擊的狀態
        binding.vChatAction.apply {
            binding.etInput.afterTextChanged {
                setSendStatus(it.isNotEmpty() && viewModel.checkIsSpeak())
            }
        }
    }

    /**
     * 加入"@nickName"訊息，並判斷前後是否需要補空格
     */
    private fun addTagMessage(tagUser: String) {
        binding.vChatAction.binding.etInput.apply {
            if (!isEnabled) return@apply
            val input = text.toString().ifEmpty { "" }
            val space = " "
            var plusTag = when {
                input.isEmpty() -> input.plus(tagUser)
                else -> {
                    if (input.endsWith(space)) {
                        input.plus(tagUser)
                    } else {
                        input.plus(space).plus(tagUser) //當tag的前面沒空格時，前面補空格
                    }
                }
            }
            plusTag = plusTag.plus(space) //點tag，後面補空格
            setText(plusTag)
            text?.let { setSelection(it.length) }
        }
    }

    private fun initEvent() {
        chatMessageListAdapter.itemContentClickListener =
            object : ChatMessageListAdapter.ItemContentClickListener {
                override fun onRedEnvelopeClick(packetId: String, packetType: Int) {
                    createRedPacketDialog(packetId, packetType, false) //非管理原才會顯示打開按鈕，才可點擊
                }

                override fun onPhotoClick(url: String) {
                    viewModel.showPhoto(url)
                }

                override fun onUserAvatarClick(tagUserPair: Pair<String, String>) { //Pair<"[@:userId]", "@nickName">
                    viewModel.tagPairList.add(tagUserPair)
                    addTagMessage(tagUserPair.second) //加入"@nickName"訊息
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chatEvent.collect { chatEvent ->
                when (chatEvent) {
                    is ChatEvent.ChatRoomIsReady -> { //已訂閱roomId且已更新歷史訊息
                        initEvent() //房間就緒後才能操作列表內容
                        viewModel.apply {
                            //region 選擇照片會離開聊天室頁面，返回後需等重新訂閱，聊天室就緒後才能發送圖片訊息
                            if (!tempChatImgUrl.isNullOrEmpty() && chatEvent.isReady) {
                                tempChatImgUrl?.let {
                                    delay(200)
                                    sendPictureMsgDialog = SendPictureMsgDialog.newInstance(it,
                                        object : SendPictureMsgDialog.SendMsgListener {
                                            override fun onSend(
                                                msg: String,
                                                chatType: ChatType,
                                            ) {
                                                chatSendMessage(
                                                    LiveMsgEntity().apply {
                                                        content = msg
                                                        type = chatType.code.toString()
                                                    }
                                                )
//                                                Timber.e("chatSendMessage msg:$msg, type:${chatType.code.toString()}")
                                                tempChatImgUrl = null
                                            }
                                        })
                                    sendPictureMsgDialog?.showAllowingStateLoss(
                                        childFragmentManager, "SendPictureMsgDialog"
                                    )
                                }
                            }
                            //endregion
                        }
                    }
                    is ChatEvent.UpdateList -> {
                        chatMessageListAdapter.dataList = chatEvent.chatMessageList

                        if (viewModel.isFirstInit) {
                            binding.rvChatMessage.removeItemDecoration(headerItemDecoration)
                            binding.rvChatMessage.addItemDecoration(headerItemDecoration)
                        }
                    }

                    is ChatEvent.RemoveMsg -> {
                        chatMessageListAdapter.removeItem(chatEvent.position)
                    }

                    is ChatEvent.UserEnter -> {

                    }

                    is ChatEvent.UserLeave -> {

                    }

                    is ChatEvent.SubscribeRoom -> {
                        Timber.i("[Chat] 訂閱 roomId -> ${chatEvent.roomId}")
                        subscribeChatRoom(chatEvent.roomId.toString()) //需要修改socket token
                    }

                    is ChatEvent.UnSubscribeRoom -> {
                        Timber.i("[Chat] 解除訂閱 roomId -> ${chatEvent.roomId}")
                        unSubscribeChatRoom(chatEvent.roomId.toString()) //需要修改socket token
                    }

                    is ChatEvent.SubscribeChatUser -> {
                        Timber.i("[Chat] 訂閱 userId -> ${chatEvent.userId}")
                        subscribeChatUser(chatEvent.userId.toString())
                    }

                    is ChatEvent.UnSubscribeChatUser -> {
                        Timber.i("[Chat] 解除訂閱 userId -> ${chatEvent.userId}")
                        unSubscribeChatUser(chatEvent.userId.toString())
                    }

                    is ChatEvent.InitFail -> {
                        showErrorPromptDialog(
                            title = getString(R.string.error),
                            message = chatEvent.message
                        ) {
                            activity?.onBackPressed()
                        }
                    }

                    is ChatEvent.ActionInputSendStatusAndMaxLength -> {
                        binding.vChatAction.setInputMaxLength(chatEvent.maxLength)
                        binding.vChatAction.setInputStatus(chatEvent.isEnable)
                        binding.vChatAction.apply {
                            setInputMaxLength(chatEvent.maxLength)
                            setInputStatus(chatEvent.isEnable)
                            setSendStatus(chatEvent.isEnable && binding.etInput.text.toString()
                                .isNotEmpty())
                        }
                    }

                    is ChatEvent.ActionUploadImageStatus -> {
                        binding.vChatAction.setUploadImageStatus(chatEvent.isEnable)
                    }

                    is ChatEvent.NoMatchRoom -> {

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
                        showToast(getString(R.string.chat_you_banned))
                    }

                    is ChatEvent.UnSilence -> {
                        showToast(getString(R.string.chat_you_can_chatting))
                    }

                    is ChatEvent.KickOut -> {
                        showPromptDialog(
                            getString(R.string.chat_chat_room_lobby),
                            getString(R.string.chat_you_kicked_out)
                        ) {
                            dismissDiaolg()
                            activity?.onBackPressed()
                        }
                    }

                    is ChatEvent.UserSystemPrompt -> {
                        insertItem()
                    }

                    is ChatEvent.NotifyChange -> {
                        chatMessageListAdapter.notifyDataSetChanged()
                    }

                    is ChatEvent.ScrollToBottom -> {
                        binding.rvChatMessage.apply {
                            post {
                                scrollToPosition(chatMessageListAdapter.itemCount - 1) //滑動到底部不需動畫效果
                                binding.rvChatMessage.isVisible = true
                            }
                        }
                    }

                    is ChatEvent.RedEnvelopeMsg -> {
                        insertItem()
                    }

                    is ChatEvent.OpenLuckyBag -> {
//                        redPacketDialog?.show()
                    }

                    is ChatEvent.GetLuckyBagResult -> {
                        if (chatEvent.luckyBagResult.success) {
                            redPacketDialog?.showRedPacketOpenDialog(
                                chatEvent.luckyBagResult.t?.toDouble() ?: 0.0
                            )
                            //TODO Bill 更新聊天室列表的紅包狀態
                        } else {
                            when (chatEvent.luckyBagResult.code) {
                                ChatViewModel.ChatErrorCode.NOT_ENOUGH_BET_AND_RECH_MONEY, ChatViewModel.ChatErrorCode.GOT_ALREADY -> {
                                    showPromptDialog(
                                        getString(
                                            R.string.chat_hint
                                        ),
                                        chatEvent.luckyBagResult.msg
                                    ) {
                                        dismissDiaolg()
                                    }
                                }
                                ChatViewModel.ChatErrorCode.PW_ERROR -> {
                                    redPacketDialog?.showPWErrorHint()
                                }
                                ChatViewModel.ChatErrorCode.NET_ERROR -> {
                                    showPromptDialog(
                                        getString(R.string.chat_hint),
                                        chatEvent.luckyBagResult.msg
                                    ) {
                                        dismissDiaolg()
                                    }
                                }
                                else -> {
                                    showPromptDialog(
                                        getString(
                                            R.string.chat_hint
                                        ),
                                        chatEvent.luckyBagResult.msg
                                    ) {
                                        dismissDiaolg()
                                    }
                                }
                            }
                            Timber.v("Bill===>開完紅包${chatEvent.luckyBagResult}")
                        }
                    }

                    is ChatEvent.UpdateUserEnterList -> {
                        chatWelcomeAdapter.dataList = chatEvent.userEnterList
                    }

                    is ChatEvent.InsertUserEnter -> {
                        chatWelcomeAdapter.insertItem()
                        if (jobScroll?.isActive == true) jobScroll?.cancel()
                        jobScroll = lifecycleScope.launch {
                            delay(2000)
                            binding.rvWelcome.smoothScrollToPosition(chatWelcomeAdapter.itemCount - 2)
                            delay(2000)
                            binding.rvWelcome.smoothScrollToPosition(chatWelcomeAdapter.itemCount - 1)
                        }
                    }

                    is ChatEvent.IsAdminType -> {
                        chatMessageListAdapter.isAdmin = chatEvent.isAdmin
                    }

                    //獲取未領取紅包列表
                    is ChatEvent.GetUnPacket -> {
                        redEnvelopeListDialog = context?.let { context ->
                            ChatRepository.unPacketList
                                ?.toMutableList()?.let { getUnPacketList ->
                                    RedEnvelopeListDialog(
                                        context,
                                        getUnPacketList,
                                        object : RedEnvelopeListDialog.Listener { //開紅包
                                            override fun onDialogCallback(selected: Row) {
                                                createRedPacketDialog(
                                                    selected.id.toString(),
                                                    selected.packetType,
                                                    chatEvent.isAdmin
                                                )
                                            }
                                        }
                                    )
                                }
                        }

                        chatRedEnpView.setOnClickListener {
                            redEnvelopeListDialog?.show()
                        }
                    }

                    //判斷顯示懸浮紅包
                    is ChatEvent.ChatRedEnpViewStatus -> {
                        chatRedEnpView.isVisible = chatEvent.isShow
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
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.removeRangeEvent.collect { chatEvent ->
                when (chatEvent) {
                    is ChatEvent.UpdateList -> {
                        chatMessageListAdapter.dataList = chatEvent.chatMessageList
                    }

                    is ChatEvent.RemoveRangeMessageItem -> {
                        chatMessageListAdapter.removeRangeItem(0, chatEvent.count)
                    }
                }
            }
        }

        //上傳圖片server的result
        viewModel.editIconUrlResult.observe(viewLifecycleOwner) {
            val iconUrlResult = it.getContentIfNotHandled()
            if (iconUrlResult?.success == true) {
                viewModel.tempChatImgUrl = iconUrlResult.t
            } else
                iconUrlResult?.msg?.let { msg ->
                    showErrorPromptDialog(getString(R.string.error), msg) {}
                }
        }
    }

    private fun insertItem(isMe: Boolean = false) {
        chatMessageListAdapter.insertItem()
        if (isMe) {
            chatListScrollToBottom(isSmooth = false) //發送自己的訊息後，需快速至聊天室底部
        } else {
            if (!isShowScrollBottom) {
                chatListScrollToBottom(isSmooth = true)
            }
        }
    }

    private fun showToast(msg: String) {
        binding.vChatToast.tvToast.text = msg
        activity?.let {
            binding.vChatToast.bvBlock.setupWith(it.window?.decorView?.rootView as ViewGroup)
                .setFrameClearDrawable(it.window?.decorView?.background)
                .setBlurRadius(8f)
        }
        binding.vChatToast.root.isVisible = true
        Handler(Looper.getMainLooper()).postDelayed(
            { binding.vChatToast.root.isVisible = false },
            2000
        )
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

    override fun onClick(v: View?) {
        when (v) {
            binding.vChatAction.binding.ivUploadImage -> {
                pickPhoto()
                hideKeyboard()
            }
            binding.vChatAction.binding.ivSend -> {
                val input = binding.vChatAction.binding.etInput.text
                if (input.isNullOrEmpty()) return
                if (mIsEnabled) {
                    avoidFastDoubleClick(delayMills = 1500) //避免短時間重複送出訊息
                    val sendMessage = setupSendMessage(input.toString())
                    chatSendMessage(
                        LiveMsgEntity().apply {
                            content = sendMessage
                            type = ChatType.CHAT_SEND_TEXT_MSG.code.toString()
                        }
                    )
                    binding.vChatAction.binding.etInput.setText("")
                    viewModel.tagPairList.clear()
                    hideKeyboard()
                } else {
                    context?.let {
                        showToast(getString(R.string.chat_speaking_too_often))
                    }
                }
            }
            binding.ivDownBtn -> {
                chatListScrollToBottom(isSmooth = true)
            }
        }
    }

    //選擇相片
    private fun pickPhoto() {
        PictureSelector.create(activity)
            .openGallery(PictureMimeType.ofImage())
            .imageEngine(GlideEngine.createGlideEngine())
            .setLanguage(getLanguage()) // 设置语言，默认中文
            .isCamera(false) // 是否显示拍照按钮 true or false
            .selectionMode(PictureConfig.SINGLE) // 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
            .isEnableCrop(false) // 是否裁剪 true or false
            .isCompress(true) // 是否压缩 true or false
            .rotateEnabled(false) // 裁剪是否可旋转图片 true or false
            .circleDimmedLayer(false) // 是否圆形裁剪 true or false
            .showCropFrame(false) // 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
            .showCropGrid(false) // 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or false
            .withAspectRatio(16, 9) // int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
            .minimumCompressSize(100) // 小于100kb的图片不压缩
            .forResult(selectMediaListener)
    }

    private fun getLanguage(): Int {
        return when (LanguageManager.getSelectLanguage(activity)) {
            LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> LanguageConfig.CHINESE
            LanguageManager.Language.EN -> LanguageConfig.ENGLISH
            LanguageManager.Language.VI -> LanguageConfig.VIETNAM
            else -> LanguageConfig.ENGLISH // 套件無支援
        }
    }

    private val selectMediaListener = object : OnResultCallbackListener<LocalMedia> {
        override fun onResult(result: MutableList<LocalMedia>?) {
            LocalUtils.setLocalLanguage(
                requireContext(),
                LanguageManager.getSetLanguageLocale(requireContext())
            )
            try {
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
                if (file.exists())
                    uploadImg(file)
                else
                    throw FileNotFoundException()
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToastInCenter(
                    requireContext(),
                    getString(R.string.error_reading_file)
                )
            }
        }

        override fun onCancel() {
            LocalUtils.setLocalLanguage(
                requireContext(),
                LanguageManager.getSetLanguageLocale(requireContext())
            )
            Timber.i("PictureSelector Cancel")
        }
    }

    //處理聊天室是否就緒的event
    private fun handleChatRoomIsReadyEvent(isReady: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.emitChatRoomIsReady(isReady)
        }
    }

    private fun uploadImg(file: File) {
        val userId = viewModel.loginRepository.userId.toString()
        val uploadImgRequest =
            UploadImgRequest(userId, file, UploadImgRequest.PlatformCodeType.AVATAR)
        viewModel.uploadImage(uploadImgRequest)
    }

    override fun onDestroy() {
        jobScroll?.cancel()
        super.onDestroy()
    }

    private fun chatListScrollToBottom(isSmooth: Boolean) {
        binding.rvChatMessage.apply {
            post {
                val position = chatMessageListAdapter.itemCount - 1
                if (isSmooth) smoothScrollToPosition(position)
                else scrollToPosition(position)
            }
        }
    }

    fun createRedPacketDialog(packetId: String, packetType: Int, isAdmin: Boolean) {
        dismissDiaolg()
        redPacketDialog = context?.let {
            RedPacketDialog(
                it,
                RedPacketDialog.PacketListener(
                    onClickListener = { luckyBagRequest ->
                        viewModel.getLuckyBag(luckyBagRequest)
                        hideKeyboard()
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
        if (!isAdmin) redPacketDialog?.show() //非管理員才顯示紅包彈窗
    }

    private fun dismissDiaolg() {
        if (redEnvelopeListDialog?.isShowing == true) redEnvelopeListDialog?.dismiss()
        if (redPacketDialog?.isShowing == true) redPacketDialog?.dismiss()
    }
}
