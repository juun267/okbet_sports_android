package org.cxct.sportlottery.ui.main.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING
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
        if (mNewsTabAdapter.mDataList.size > 1) {
            btn_arrow_left.setOnClickListener {

                val currentPosition =
                    mNewsTabAdapter.mDataList.indexOf(mNewsTabAdapter.mDataList.find { it.isSelect })

                when (currentPosition) {
                    0 -> {
                        rv_tab.scrollToPosition(mNewsTabAdapter.itemCount - 1)
                        mNewsTabAdapter.setSelect(mNewsTabAdapter.itemCount - 1)
                    }
                    else -> {
                        rv_tab.scrollToPosition(currentPosition - 1)
                        mNewsTabAdapter.setSelect(currentPosition - 1)
                    }
                }

                val nextPosition = when (currentPosition) {
                    0 -> mNewsTabAdapter.itemCount - 1
                    else -> currentPosition - 1
                }
                if (tabPosition != nextPosition) {
                    tabPosition = nextPosition
                    val selectMsgType = mNewsTabAdapter.mDataList[nextPosition].msgType
                    val dataList = mMessageList?.filter { it.msgType == selectMsgType }
                    resetRvContentManager(dataList)
                    mNewsContentAdapter.setData(dataList)
                }
            }

            btn_arrow_right.setOnClickListener {
                val currentPosition =
                    mNewsTabAdapter.mDataList.indexOf(mNewsTabAdapter.mDataList.find { it.isSelect })

                when (currentPosition) {
                    mNewsTabAdapter.itemCount - 1 -> {
                        rv_tab.scrollToPosition(0)
                        mNewsTabAdapter.setSelect(0)
                    }
                    else -> {
                        rv_tab.scrollToPosition(currentPosition + 1)
                        mNewsTabAdapter.setSelect(currentPosition + 1)
                    }
                }

                val nextPosition = when (currentPosition) {
                    mNewsTabAdapter.itemCount - 1 -> 0
                    else -> currentPosition + 1
                }
                if (tabPosition != nextPosition) {
                    tabPosition = nextPosition
                    val selectMsgType = mNewsTabAdapter.mDataList[nextPosition].msgType
                    val dataList = mMessageList?.filter { it.msgType == selectMsgType }
                    resetRvContentManager(dataList)
                    mNewsContentAdapter.setData(dataList)
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
            PagerSnapHelper().attachToRecyclerView(rv_tab)
            rv_tab.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                var scrollRight = true //<0 往左  //>0 往右
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    scrollRight = dx > 0
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == SCROLL_STATE_SETTLING) {
                        val childIndex = if (scrollRight) 1 else 0

                        val currentPosition =
                            (recyclerView.getChildAt(childIndex).layoutParams as RecyclerView.LayoutParams).bindingAdapterPosition

                        if (tabPosition != currentPosition) {
                            tabPosition = currentPosition
                            val selectMsgType = mNewsTabAdapter.mDataList[currentPosition].msgType
                            val dataList = mMessageList?.filter { it.msgType == selectMsgType }
                            resetRvContentManager(dataList)
                            mNewsContentAdapter.setData(dataList)
                        }
                    }
                }
            })

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