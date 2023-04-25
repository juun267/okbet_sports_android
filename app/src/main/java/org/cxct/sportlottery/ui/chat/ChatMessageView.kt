package org.cxct.sportlottery.ui.chat

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_chat_message.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper

class ChatMessageView @JvmOverloads constructor(
    context: Context?,
    attribute: AttributeSet? = null,
    defStyle: Int = 0,
) : RelativeLayout(context, attribute, defStyle), View.OnClickListener {

    private val TAG = ChatMessageView::class.java.simpleName

    private var mCurrentAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null

    init {
        addView(LayoutInflater.from(context).inflate(R.layout.view_chat_message, this, false))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initViews()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            btn_slide_to_bottom.id -> {
                scrollToBottom()
            }
        }
    }

    fun loadChatMessageData(chatMessageAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        mCurrentAdapter = chatMessageAdapter
        initRecyclerView(chatMessageAdapter)
    }

    fun clearChatMessageData() {
        mCurrentAdapter = null
        rvList?.apply {
            adapter = null
        }
    }

    fun refreshChatMessageData() {
        mCurrentAdapter?.notifyDataSetChanged()
    }

    fun scrollToBottom() {
        rvList?.scrollToPosition(0)
    }

    fun isScrollToBottom(): Boolean {
        return btn_slide_to_bottom.visibility == View.GONE
    }

    private fun initViews() {
        btn_slide_to_bottom.setOnClickListener(this)
    }

    private fun initRecyclerView(chatMessageAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        // 基本設置
        rvList?.apply {
            val manager = CustomLinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
            manager.stackFromEnd = true
            //20200817 記錄問題：處理 RecycleView 遇到 IndexOutOfBoundsException: Inconsistency detected. 問題
            layoutManager = manager
            adapter = chatMessageAdapter
        }
        // 添加滑動回彈效果
        OverScrollDecoratorHelper.setUpOverScroll(rvList,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
        // 監聽滑到到底部的事件
        rvList?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // RecyclerView.canScrollVertically(1) 的值表示是否能向上滚动，false表示已经滚动到底部
                // RecyclerView.canScrollVertically(-1) 的值表示是否能向下滚动，false表示已经滚动到顶部
                if (!recyclerView.canScrollVertically(1)) {
                    btn_slide_to_bottom.visibility = View.GONE
                } else
                    btn_slide_to_bottom.visibility = View.VISIBLE
            }
        })
    }
}