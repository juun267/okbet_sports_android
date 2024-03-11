package org.cxct.sportlottery.ui.chat

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
import org.cxct.sportlottery.util.MetricsUtil

/**
 * 聊天室搶紅包列表彈窗
 */

class RedEnvelopeListDialog(
    private val mData: MutableList<UnPacketRow>,
    var mListener: Listener?,
) : BaseDialog<BaseViewModel, DialogRedEnvelopeListBinding>() {

    init {
        setStyle(R.style.CustomDialogStyle)
    }
    private val mRVAdapter = RVAdapter()

    override fun onInitView() {
        initView()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
        binding.rvList.scrollToPosition(0)
    }

    override fun show(manager: FragmentManager) {
        super.show(manager)
        binding.rvList.scrollToPosition(0)
    }

    private fun initView() {
        initRecyclerView()
        setOnClickListeners(binding.ivClose,binding.btnConfirm){
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
                mListener?.onDialogCallback(mRVAdapter.getItem(position))
            }
        }
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }


    fun setPackets(data: MutableList<UnPacketRow>) {
        mData.clear()
        mData.addAll(data)
        mRVAdapter.setList(mData)
        mRVAdapter.setOnItemChildClickListener { adapter, view, position ->
            mListener?.onDialogCallback(mRVAdapter.getItem(position))
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

        override fun onBinding(
            position: Int,
            binding: ItemChatRedEnvelopeBinding,
            item: UnPacketRow,
        ) {
            binding.tvNumber.text = (position + 1).toString()
            addChildClickViewIds(binding.btnOpen.id)
        }

    }

    interface Listener {
        fun onDialogCallback(selected: UnPacketRow)
    }


}