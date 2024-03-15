package org.cxct.sportlottery.ui.chat

import android.os.Bundle
import android.os.Parcelable
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.DialogRedEnvelopeListBinding
import org.cxct.sportlottery.databinding.ItemChatRedEnvelopeBinding
import org.cxct.sportlottery.net.chat.data.UnPacketRow
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.MetricsUtil

/**
 * 聊天室搶紅包列表彈窗
 */

class RedEnvelopeListDialog : BaseDialog<BaseViewModel, DialogRedEnvelopeListBinding>() {

    companion object{
        fun newInstance(chatEvent: ChatEvent.GetUnPacket,mData: MutableList<UnPacketRow>)= RedEnvelopeListDialog().apply {
            arguments = Bundle().apply {
                putParcelable("chatEvent",chatEvent)
                putParcelableArrayList("mData", arrayListOf<UnPacketRow>().apply {addAll(mData)})
            }
        }
    }
    init {
        setStyle(R.style.FullScreen)
    }
    private val chatEvent by lazy { requireArguments().getParcelable<ChatEvent.GetUnPacket>("chatEvent")!! }
    private val mData by lazy { requireArguments().getParcelableArrayList<UnPacketRow>("mData")!! }
    private val mRVAdapter = RVAdapter()

    override fun onInitView() {
        initView()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
        if (isAdded)
        binding.rvList.scrollToPosition(0)
    }

    override fun show(manager: FragmentManager) {
        super.show(manager)
        if (isAdded)
        binding.rvList.scrollToPosition(0)
    }

    private fun initView() {
        initRecyclerView()
        setOnClickListeners(binding.root,binding.ivClose,binding.btnConfirm){
            dismiss()
        }
    }

    private fun initRecyclerView() {
        binding.rvList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(
                RecycleViewDivider(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    MetricsUtil.convertDpToPixel(5f, context).toInt(),
                    ContextCompat.getColor(context, R.color.transparent)
                )
            )
            adapter = mRVAdapter
            mRVAdapter.setList(mData)
            mRVAdapter.setOnItemChildClickListener { adapter, view, position ->
                (requireParentFragment() as? ChatFragment)?.onDialogCallback(chatEvent,mRVAdapter.getItem(position))
            }
        }
    }


    fun setPackets(data: MutableList<UnPacketRow>) {
        mData.clear()
        mData.addAll(data)
        mRVAdapter.setList(mData)
        mRVAdapter.setOnItemChildClickListener { adapter, view, position ->
            (requireParentFragment() as? ChatFragment)?.onDialogCallback(chatEvent,mRVAdapter.getItem(position))
        }
    }

    fun reload(data: MutableList<UnPacketRow>) {
        mData.clear()
        mData.addAll(data)
        mRVAdapter.setList(mData)
    }

    fun setPrivateGroupstate() {
//        mChatFrom = RedPacketDialog.ChatFrom.CHAT_GROUP
    }

    fun setPublicGroupstate() {
//        mChatFrom = RedPacketDialog.ChatFrom.CHAT
    }

    /**
     * 開啟紅包dialog
     */
    private fun showRedPacketDialog(pocket: UnPacketRow) {
//        val config = ChatManager.getChatConfigOutput()

//        mRedPacketDialog = context.let { RedPacketDialog(it) }
//        mRedPacketDialog?.setData("config.token", pocket, mChatFrom)
//        mRedPacketDialog?.setOnCompleteListener(object : RedPacketDialog.OnCompleteListener {
//            override fun onComplete() {
//                // 在紅包列表裡，移除掉領取的紅包 id
//                mListener?.onDialogCallback(pocket)
//                //mRedPacketDialog?.dismiss()
//            }
//
//            override fun onCancel() {}
//        })
//        mRedPacketDialog?.show()
    }

    class RVAdapter:BindingAdapter<UnPacketRow,ItemChatRedEnvelopeBinding>() {

        init {
            addChildClickViewIds(R.id.btnOpen)
        }
        override fun onBinding(
            position: Int,
            binding: ItemChatRedEnvelopeBinding,
            item: UnPacketRow,
        ) {
            binding.tvNumber.text = (position + 1).toString()
        }

    }

    interface Listener {
        fun onDialogCallback(selected: UnPacketRow)
    }


}