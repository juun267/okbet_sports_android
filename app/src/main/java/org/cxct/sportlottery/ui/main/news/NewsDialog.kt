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

    private val mRvTabManager by lazy { LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) }
    private val mNewsTabAdapter by lazy { NewsTabAdapter(context, mMessageList) }
    private val mRvContentManager by lazy { LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) }
    private val mNewsContentAdapter by lazy { NewsContentAdapter() }

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
        img_arrow_left.visibility =
            if (mNewsTabAdapter.mDataList.size > 1) View.VISIBLE else View.INVISIBLE
        img_arrow_right.visibility =
            if (mNewsTabAdapter.mDataList.size > 1) View.VISIBLE else View.INVISIBLE
    }

}