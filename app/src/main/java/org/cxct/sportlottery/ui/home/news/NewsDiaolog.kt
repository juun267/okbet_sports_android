package org.cxct.sportlottery.ui.home.news

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import kotlinx.android.synthetic.main.dialog_event_msg.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.message.Row
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.main.MainViewModel

class NewsDiaolog(activity: Activity, messageListResult: List<Row>?) :
    BaseDialog<MainViewModel>(MainViewModel::class) {

    init {
        setStyle(R.style.CustomDialogStyle)
    }

    var mActivity = activity

    var msgData = messageListResult

    var mNewsViewPagerAdapter = NewsViewPagerAdapter(mActivity)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_event_msg, container, false).apply {
            var a = this.width
            var b = a
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTabRecyclerView()
        initViewPager()
        initButton()

    }

    private fun initButton() {
        img_close.setOnClickListener {
            dismiss()
        }
    }

    private fun initViewPager() {
        mNewsViewPagerAdapter.data = getTypeMsg(1) as MutableList<Row>
        vp_msg.adapter = mNewsViewPagerAdapter
    }

    private fun initTabRecyclerView() {
        rv_tab.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)//LinearLayout
        rv_tab.adapter = NewsAdapter(context, NewsAdapter.ItemClickListener {
            mNewsViewPagerAdapter = NewsViewPagerAdapter(mActivity)
            mNewsViewPagerAdapter.data = getTypeMsg(it) as MutableList<Row>
            vp_msg.adapter = mNewsViewPagerAdapter
        })
        var mPagerSnapHelper = PagerSnapHelper()
        mPagerSnapHelper.attachToRecyclerView(rv_tab)
    }

    private fun getTypeMsg(index: Int): List<Row>? {
        var filterData = msgData?.filter {
            it.msgType.toInt() == index
        }
        return filterData ?: mutableListOf()
    }
}