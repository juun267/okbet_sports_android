package org.cxct.sportlottery.ui.chat

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.view_chat_red_envelope.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.net.chat.data.UnPacketRow


class ChatRedEnvelopeView @JvmOverloads constructor(
    context: Context?,
    attribute: AttributeSet? = null,
    defStyle: Int = 0,
) : RelativeLayout(context, attribute, defStyle), View.OnClickListener,
    RedEnvelopeListDialog.Listener {

    private var mDialog: RedEnvelopeListDialog? = null
    private var mPacket: MutableList<UnPacketRow> = arrayListOf()
    private var mCanOpen = false

    init {
        addView(LayoutInflater.from(context).inflate(R.layout.view_chat_red_envelope, this, false))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initViews()
        checkState()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            rootLayout.id -> {
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
        mDialog?.show()
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