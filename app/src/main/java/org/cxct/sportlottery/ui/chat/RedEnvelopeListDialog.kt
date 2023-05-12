package org.cxct.sportlottery.ui.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_red_envelope_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.chat.getUnPacket.UnPacketRow
import org.cxct.sportlottery.util.MetricsUtil

/**
 * 聊天室搶紅包列表彈窗
 */
//TODO Bill 之後重構
class RedEnvelopeListDialog(
    context: Context,
    private val mData: MutableList<UnPacketRow>,
    var mListener: Listener?,
) : AlertDialog(context), View.OnClickListener {

    private val mRVAdapter = RVAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_red_envelope_list)
        this.window?.setBackgroundDrawableResource(android.R.color.transparent)

        initView()
    }

    override fun show() {
        super.show()
        rvList.scrollToPosition(0)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            ivClose.id -> {
                dismiss()
            }
            btnConfirm.id -> {
                dismiss()
            }
        }
    }

    private fun initView() {
        initRecyclerView()
        ivClose.setOnClickListener(this)
        btnConfirm.setOnClickListener(this)
    }

    private fun initRecyclerView() {
        rvList.apply {
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
            mRVAdapter.setDatas { mData }
            mRVAdapter.setItemCallback(object : RVAdapter.ItemCallback {
                override fun onSelect(selected: UnPacketRow) {
                    mListener?.onDialogCallback(selected)
                }
            })
        }
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

//    fun loadList(groupId: String) {
//        val config = ChatManager.getChatConfigOutput()
//        getGroupUnPacketIds(config.token, groupId)
//    }

    fun setPackets(data: MutableList<UnPacketRow>) {
        mData.clear()
        mData.addAll(data)
        mRVAdapter.setDatas { mData }
        mRVAdapter.setItemCallback(object : RVAdapter.ItemCallback {
            override fun onSelect(selected: UnPacketRow) {
                mListener?.onDialogCallback(selected)
            }
        })
    }

    fun reload(data: MutableList<UnPacketRow>) {
        mData.clear()
        mData.addAll(data)
        mRVAdapter.setDatas { mData }
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

    class RVAdapter(private val mData: ArrayList<UnPacketRow> = ArrayList()) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var itemCallback: ItemCallback? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ChatRedEnvelopeViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_red_envelope, parent, false)
            )
        }

        override fun getItemCount(): Int = mData.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is ChatRedEnvelopeViewHolder -> {
                    holder.tvNumber.text = (position + 1).toString()
                }
            }
        }

        interface ItemCallback {
            fun onSelect(selected: UnPacketRow)
        }

        inner class ChatRedEnvelopeViewHolder(view: View) : RecyclerView.ViewHolder(view),
            View.OnClickListener {
            var tvNumber: TextView = itemView.findViewById<TextView>(R.id.tvNumber)
            var btnOpen: Button = itemView.findViewById<Button>(R.id.btnOpen)

            init {
                btnOpen.setOnClickListener(this)
            }

            override fun onClick(view: View?) {
                when (view?.id) {
                    btnOpen.id -> {
                        itemCallback?.onSelect(mData[adapterPosition])
                    }
                }
            }
        }

        fun setItemCallback(callback: ItemCallback?) {
            itemCallback = callback
        }

        fun setDatas(datablock: () -> MutableList<UnPacketRow>) {
            mData.clear()
            mData.addAll(datablock())
            notifyDataSetChanged()
        }

        fun addDatas(datablock: () -> MutableList<UnPacketRow>) {
            val newDatas = datablock()
            mData.addAll(newDatas)
            notifyItemRangeChanged((mData.size - newDatas.size - 1), mData.size - 1)
        }

        fun addData(item: UnPacketRow) {
            mData.add(item)
            notifyItemChanged(mData.size - 1)
        }
    }

    interface Listener {
        fun onDialogCallback(selected: UnPacketRow)
    }
}