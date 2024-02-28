package org.cxct.sportlottery.ui.chat

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import org.cxct.sportlottery.databinding.ViewChatRedEnvelopeBinding
import org.cxct.sportlottery.net.chat.data.UnPacketRow
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.AppManager


class ChatRedEnvelopeView @JvmOverloads constructor(
    context: Context?,
    attribute: AttributeSet? = null,
    defStyle: Int = 0,
) : RelativeLayout(context, attribute, defStyle), View.OnClickListener,
    RedEnvelopeListDialog.Listener {

    private var mDialog: RedEnvelopeListDialog? = null
    private var mPacket: MutableList<UnPacketRow> = arrayListOf()
    private var mCanOpen = false
    val binding by lazy { ViewChatRedEnvelopeBinding.inflate(LayoutInflater.from(context), this, false) }
    init {
        addView(binding.root)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initViews()
        checkState()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            binding.rootLayout.id -> {
                if (mCanOpen) loadWithPackets()
            }
        }
    }

    fun initViews() {
//        mDialog = RedEnvelopeListDialog(context)
//        mDialog?.setListener(this)
//        rootLayout.setOnClickListener(this)
    }

    fun loadWithPackets() {
        mDialog?.setPackets(mPacket)
        (AppManager.currentActivity() as? BaseActivity<*,*>)?.supportFragmentManager?.let {
            mDialog?.show(it)
        }
    }

    fun setPackets() {
        checkStateByConfig()
        mDialog?.setPublicGroupstate()
        getUnPacketIds()
    }

    fun refresh() {
        getUnPacketIds()
    }

    fun checkState() {
//        if(mPacket.isEmpty()) { this.visibility = View.GONE } else {  this.visibility = View.VISIBLE }
    }

    /**
     * 取得公頻群組可領取的紅包列表
     */
    private fun getUnPacketIds() {
        mCanOpen = false
//        val config = ChatManager.getChatConfigOutput()
//        val input = ChatGroupUnpacketIdsInput()
//        input.setToken(config.token)
        //TODo Bill 取得公頻群組可領取的紅包列表
//        mChatApi.getUnPacketIds(input, object : BaseWebApi.ResultListener {
//            override fun onResult(response: String?) {
//                mPacket = getPacketEntityList(response)
//                ChatManager.getChatConfigOutput().packetList = mPacket
//                checkState()
//                mDialog?.reload(mPacket)
//                mCanOpen = true
//            }
//
//            override fun onError(error: ErrorOutput?) {
//            }
//        })
    }

    private fun checkStateByConfig() {
//        mPacket = ChatManager.getChatConfigOutput().packetList ?: mutableListOf()
        if (mPacket.isEmpty()) {
            this.visibility = View.GONE
        } else {
            this.visibility = View.VISIBLE
        }
    }

    override fun onDialogCallback(selected: UnPacketRow) {
        refresh()
    }

}