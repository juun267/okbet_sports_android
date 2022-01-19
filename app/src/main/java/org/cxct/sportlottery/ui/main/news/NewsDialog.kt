package org.cxct.sportlottery.ui.main.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bekawestberg.loopinglayout.library.LoopingLayoutManager
import kotlinx.android.synthetic.main.dialog_event_msg_v5.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.message.Row
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.main.MainViewModel

class NewsDialog(private val mMessageList: List<Row>?) : BaseDialog<MainViewModel>(MainViewModel::class) {

    private val mRvTabManager by lazy { context?.let { LoopingLayoutManager(it, LinearLayoutManager.HORIZONTAL, false) } }
    private val mNewsTabAdapter by lazy { NewsTabAdapter(context, mMessageList) }
    private val mRvContentManager by lazy { LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) }
    private val mNewsContentAdapter by lazy { NewsContentAdapter() }

    var tabPosition = 0

    init {
        setStyle(R.style.CustomDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_event_msg_v5, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCloseBtn()
        initRecyclerView()
        setArrowBtn()
    }

    private fun setArrowBtn() {
        if(mNewsTabAdapter.mDataList.size > 1){
            btn_arrow_left.setOnClickListener {
                val currentPosition =
                    (rv_tab.getChildAt(0).layoutParams as RecyclerView.LayoutParams).bindingAdapterPosition

                when(currentPosition){
                    0 -> rv_tab.smoothScrollToPosition(mNewsTabAdapter.itemCount - 1 )
                    else -> rv_tab.smoothScrollToPosition(currentPosition - 1)
                }
            }

            btn_arrow_right.setOnClickListener {
                val currentPosition =
                    (rv_tab.getChildAt(1).layoutParams as RecyclerView.LayoutParams).bindingAdapterPosition

                when(currentPosition){
                    mNewsTabAdapter.itemCount - 1  -> rv_tab.smoothScrollToPosition(0 )
                    else -> rv_tab.smoothScrollToPosition(currentPosition + 1)
                }
            }
        }

        img_arrow_left.visibility =
            if (mNewsTabAdapter.mDataList.size > 1) View.VISIBLE else View.INVISIBLE
        img_arrow_right.visibility =
            if (mNewsTabAdapter.mDataList.size > 1) View.VISIBLE else View.INVISIBLE
    }

    private fun setupCloseBtn() {
        btn_close.setOnClickListener { dismiss() }
    }

    private fun initRecyclerView() {
        //Tab RecycleView

        if (mNewsTabAdapter.mDataList.size > 1) {
            rv_tab.layoutManager = mRvTabManager
            rv_tab.adapter = mNewsTabAdapter

            rv_tab.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    val currentPosition =
                        (recyclerView.getChildAt(0).layoutParams as RecyclerView.LayoutParams).bindingAdapterPosition

                    if (tabPosition != currentPosition) {
                        tabPosition = currentPosition
                        val selectMsgType = mNewsTabAdapter.mDataList[currentPosition].msgType
                        val dataList = mMessageList?.filter { it.msgType == selectMsgType }
                        resetRvContentManager(dataList)
                        mNewsContentAdapter.setData(dataList)
                    }
                }
            })
            PagerSnapHelper().attachToRecyclerView(rv_tab)
        } else {
            rv_tab.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            rv_tab.adapter = mNewsTabAdapter
        }

        mNewsTabAdapter.setOnSelectItemListener(object :
            OnSelectItemListener<NewsTabAdapter.TabEntity> {
            override fun onClick(select: NewsTabAdapter.TabEntity) {
                val dataList = mMessageList?.filter { it.msgType == select.msgType }
                resetRvContentManager(dataList)
                mNewsContentAdapter.setData(dataList)
            }
        })

        //Content RecycleView
        rv_content.layoutManager = mRvContentManager
        rv_content.adapter = mNewsContentAdapter

        //RecyclerView Indicator
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(rv_content)
        indicator_view.attachToRecyclerView(rv_content,pagerSnapHelper)
        mNewsContentAdapter.registerAdapterDataObserver(indicator_view.adapterDataObserver)

        //default show first
        rv_content.post {
            mNewsTabAdapter.selectItem(0)
        }
    }

    fun resetRvContentManager(dataList: List<Row>?) {
        context?.let {
            rv_content.layoutManager = if (dataList?.size ?: 0 > 1) LoopingLayoutManager(
                it,
                LinearLayoutManager.HORIZONTAL,
                false
            ) else LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

}