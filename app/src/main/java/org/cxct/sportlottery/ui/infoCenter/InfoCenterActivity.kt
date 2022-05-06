package org.cxct.sportlottery.ui.infoCenter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_info_center.*
import kotlinx.android.synthetic.main.activity_info_center.iv_scroll_to_top
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.repository.MsgType
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.util.setTitleLetterSpacing

class InfoCenterActivity : BaseSocketActivity<InfoCenterViewModel>(InfoCenterViewModel::class) {

    companion object {
        const val KEY_READ_PAGE = "key-read-page"
        const val BEEN_READ = 0
        const val YET_READ = 1
    }

    private val mDefaultShowPage by lazy { intent.getIntExtra(KEY_READ_PAGE, BEEN_READ) }

    private var currentPage = BEEN_READ //當前頁籤

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            iv_scroll_to_top.apply {
                //置頂按鈕
                when (recyclerView.canScrollVertically(-1)) {
                    true -> {
                        visibility = View.VISIBLE
                        animate().alpha(1f).setDuration(1000).setListener(null)
                            when (custom_tab_layout.selectedTabPosition == 0) {
                                true -> viewModel.getUserMsgList(false, adapter.data.size, InfoCenterViewModel.DataType.READ)
                                false -> viewModel.getUserMsgList(false, adapter.data.size, InfoCenterViewModel.DataType.UNREAD)
                            }
                    }
                    false -> {
                        animate().alpha(0f).setDuration(1000)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    visibility = View.GONE
                                }
                            })
                    }
                }
            }
        }
    }

    val adapter by lazy {
        InfoCenterAdapter(InfoCenterAdapter.ItemClickListener {
            it.let { data ->
                val detailDialog = InfoCenterDetailDialog(data)
                detailDialog.show(supportFragmentManager, "")

                //未讀的資料打開要變成已讀
                if (currentPage == YET_READ) {
                    viewModel.setDataRead(data.id.toString())
                }
            }
        })
    }

    private var mNowLoading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.getMsgCount(MsgType.NOTICE_UNREAD)//未讀資料比數
        viewModel.getMsgCount(MsgType.NOTICE_READED)//已讀資料筆數
        setContentView(R.layout.activity_info_center)
        initToolbar()
        initLiveData()
        initRecyclerView()
        initButton()
        initSelectTab()
    }

    private fun initToolbar() {
        tv_toolbar_title.setTitleLetterSpacing()
        tv_toolbar_title.text = getString(R.string.news_center)
        btn_toolbar_back.setOnClickListener {
            finish()
        }
    }

    private fun initButton() {

        iv_scroll_to_top.setOnClickListener {
            rv_data.smoothScrollToPosition(0)
        }

        custom_tab_layout.setCustomTabSelectedListener { position ->
            when(position) {
                0 -> {
                    selectReadTab()
                }
                1 -> {
                    selectUnReadTab()
                }
            }
        }
    }

    private fun selectReadTab() {
        adapter.data = mutableListOf()//清空資料
        viewModel.getMsgCount(MsgType.NOTICE_UNREAD)//未讀資料比數
        viewModel.getUserMsgList(dataType = InfoCenterViewModel.DataType.READ)//已讀
        currentPage = BEEN_READ
        iv_scroll_to_top.visibility = View.INVISIBLE
    }

    private fun selectUnReadTab() {
        adapter.data = mutableListOf()//清空資料
        viewModel.getMsgCount(MsgType.NOTICE_READED)//已讀資料筆數
        viewModel.getUserMsgList(dataType = InfoCenterViewModel.DataType.UNREAD)//未讀
        currentPage = YET_READ
        iv_scroll_to_top.visibility = View.INVISIBLE
    }

    private fun initSelectTab() {
        when (mDefaultShowPage) {
            BEEN_READ -> {
                if (custom_tab_layout.selectedTabPosition == BEEN_READ) {
                    selectReadTab()
                } else {
                    custom_tab_layout.selectTab(BEEN_READ)
                }
            }
            YET_READ -> {
                if (custom_tab_layout.selectedTabPosition == YET_READ) {
                    selectUnReadTab()
                } else {
                    custom_tab_layout.selectTab(YET_READ)
                }
            }
        }
    }

    private fun initLiveData() {
        //已讀訊息清單
        viewModel.userReadMsgList.observe(this@InfoCenterActivity, Observer {
            val userMsgList = it ?: return@Observer
            if (currentPage == BEEN_READ) {
                if (adapter.data.isNotEmpty()) {
                    adapter.addData(userMsgList)//上拉加載
                } else {
                    if (it.isNullOrEmpty()) {
                        image_no_message.visibility = View.VISIBLE
                    } else {
                        image_no_message.visibility = View.GONE
                        adapter.data = userMsgList as MutableList<InfoCenterData>//重新載入
                    }
                }
            }
            viewModel.getResult()
        })
        //已讀總筆數
        viewModel.totalReadMsgCount.observe(this@InfoCenterActivity) {
            custom_tab_layout.firstTabText = String.format(resources.getString(R.string.inbox), it)
        }
        //未讀訊息清單
        viewModel.userUnreadMsgList.observe(this@InfoCenterActivity, Observer {
            val userMsgList = it ?: return@Observer
            if (currentPage == YET_READ) {
                when(userMsgList.isNullOrEmpty()){
                    true ->{
                        image_no_message.visibility = View.VISIBLE
                        adapter.data = mutableListOf()
                    }
                    false ->{
                        image_no_message.visibility = View.GONE
                        adapter.data = userMsgList as MutableList<InfoCenterData> //重新載入
                    }
                }
            }
            viewModel.getResult()
        })
        //未讀總筆數
        viewModel.totalUnreadMsgCount.observe(this@InfoCenterActivity) {
            custom_tab_layout.secondTabText =
                String.format(resources.getString(R.string.unread_letters), it)
        }
        //Loading
        viewModel.isLoading.observe(this) {
            if (it) {
                mNowLoading = it
                loading()
            } else {
                mNowLoading = it
                hideLoading()
            }
        }
    }

    private fun initRecyclerView() {
        rv_data.layoutManager = LinearLayoutManager(this@InfoCenterActivity, LinearLayoutManager.VERTICAL, false)
        rv_data.adapter = adapter
        rv_data.addOnScrollListener(recyclerViewOnScrollListener)
    }
}