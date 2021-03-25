package org.cxct.sportlottery.ui.main.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_event_msg.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.message.Row
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.main.MainViewModel

class NewsDialog(private val mMessageList: List<Row>?) : BaseDialog<MainViewModel>(MainViewModel::class) {

    private val mRvTabManager by lazy { LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) }
    private val mNewsTabAdapter by lazy { NewsTabAdapter(context) }
    private val mRvContentManager by lazy { LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) }
    private val mNewsContentAdapter by lazy {NewsContentAdapter() }

    init {
        setStyle(R.style.CustomDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_event_msg, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCloseBtn()
        initRecyclerView()
    }

    private fun setupCloseBtn() {
        btn_close.setOnClickListener { dismiss() }
    }

    private fun initRecyclerView() {
        //Tab RecycleView
        rv_tab.layoutManager = mRvTabManager
        rv_tab.adapter = mNewsTabAdapter
        mNewsTabAdapter.setOnSelectItemListener(object : OnSelectItemListener<NewsTabAdapter.TabEntity> {
            override fun onClick(select: NewsTabAdapter.TabEntity) {
                val dataList = mMessageList?.filter { it.msgType == select.msgType }
                mNewsContentAdapter.setData(dataList)
                switchRvContentArrow()
            }
        })
        rv_tab.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    switchRvTabArrow()
                }
           }
        })
        PagerSnapHelper().attachToRecyclerView(rv_tab)

        //Content RecycleView
        rv_content.layoutManager = mRvContentManager
        rv_content.adapter = mNewsContentAdapter
        rv_content.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    switchRvContentArrow()
                }
            }
        })
        PagerSnapHelper().attachToRecyclerView(rv_content)

        //default show first
        rv_content.post {
            mNewsTabAdapter.selectItem(0)
            switchRvTabArrow()
            switchRvContentArrow()
        }
    }

    private fun switchRvTabArrow() {
        val firstItemPosition = mRvTabManager.findFirstVisibleItemPosition()
        val lastItemPosition = mRvTabManager.findLastVisibleItemPosition()
        val lastIndex = mRvTabManager.itemCount - 1

        img_arrow_left.visibility = if (firstItemPosition == 0) View.INVISIBLE else View.VISIBLE
        img_arrow_right.visibility = if (lastItemPosition == lastIndex) View.INVISIBLE else View.VISIBLE
    }

    private fun switchRvContentArrow() {
        val visiblePosition = mRvContentManager.findFirstVisibleItemPosition()
        val lastIndex = mRvContentManager.itemCount - 1

        when {
            mRvContentManager.itemCount <= 1 -> {
                content_arrow_left.visibility = View.INVISIBLE
                content_arrow_right.visibility = View.INVISIBLE
            }
            visiblePosition == 0 -> {
                content_arrow_left.visibility = View.INVISIBLE
                content_arrow_right.visibility = View.VISIBLE
            }
            visiblePosition == lastIndex -> {
                content_arrow_left.visibility = View.VISIBLE
                content_arrow_right.visibility = View.INVISIBLE
            }
            else -> {
                content_arrow_left.visibility = View.VISIBLE
                content_arrow_right.visibility = View.VISIBLE
            }
        }
    }

}