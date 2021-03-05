package org.cxct.sportlottery.ui.home.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_event_msg.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.message.Row
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.main.MainViewModel

class NewsDialog(activity: FragmentActivity?, messageListResult: List<Row>?) :
    BaseDialog<MainViewModel>(MainViewModel::class) {

    init {
        setStyle(R.style.CustomDialogStyle)
    }

    private var mActivity = activity

    var msgData = messageListResult

    var mNewsViewPagerAdapter = NewsViewPagerAdapter(mActivity)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_event_msg, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTabRecyclerView()
        initViewPager()
        initButton()
    }

    private fun initButton() {
        img_close.let {
            it.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun initViewPager() {
        mNewsViewPagerAdapter.data = getTypeMsg(1) as MutableList<Row>
        vp_msg.adapter = mNewsViewPagerAdapter
    }

    private fun initTabRecyclerView() {
        view?.post(Runnable() {
            rv_tab.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)//LinearLayout
            rv_tab.adapter = NewsAdapter(img_top.width / 3, NewsAdapter.ItemClickListener {
                mNewsViewPagerAdapter = NewsViewPagerAdapter(mActivity)
                mNewsViewPagerAdapter.data = getTypeMsg(it) as MutableList<Row>
                vp_msg.adapter = mNewsViewPagerAdapter
            })
            rv_tab.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    when ((recyclerView.getChildAt(0).layoutParams as RecyclerView.LayoutParams).viewAdapterPosition) {
                        0 -> {
                            img_arrow_right.visibility = View.VISIBLE
                            img_arrow_left.visibility = View.INVISIBLE
                        }
                        3 -> {
                            img_arrow_right.visibility = View.INVISIBLE
                            img_arrow_left.visibility = View.VISIBLE
                        }
                        else -> {
                            img_arrow_left.visibility = View.VISIBLE
                            img_arrow_right.visibility = View.VISIBLE
                        }
                    }
                }
            })
            val mPagerSnapHelper = PagerSnapHelper()
            mPagerSnapHelper.attachToRecyclerView(rv_tab)
        })
    }

    private fun getTypeMsg(index: Int): List<Row>? {
        val filterData = msgData?.filter {
            it.msgType.toInt() == index
        }
        return filterData ?: mutableListOf()
    }
}