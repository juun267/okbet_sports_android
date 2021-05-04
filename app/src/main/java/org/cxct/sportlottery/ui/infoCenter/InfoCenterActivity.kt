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
import kotlinx.android.synthetic.main.fragment_feedback_record_list.*
import kotlinx.android.synthetic.main.fragment_sport_bet_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity

class InfoCenterActivity : BaseOddButtonActivity<InfoCenterViewModel>(InfoCenterViewModel::class) {

    companion object {
        const val KEY_READ_PAGE = "key-read-page"
        const val BEEN_READ = 0
        const val YET_READ = 1
    }

    private val mDefaultShowPage by lazy { intent.getIntExtra(KEY_READ_PAGE, BEEN_READ) }

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            iv_scroll_to_top.apply {
                when (recyclerView.canScrollVertically(-1)) {
                    true -> {
                        visibility = View.VISIBLE
                        animate().alpha(1f).setDuration(1000).setListener(null)
                            when (btn_read_letters.isChecked) {
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
                if (btn_unread_letters.isSelected) {
                    viewModel.setDataRead(data.id.toString())
                }
            }
        })
    }

    private var mNowLoading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_info_center)
        initToolbar()
        initLiveData()
        initRecyclerView()
        initData()
        initButton()
    }

    private fun initToolbar() {
        btn_toolbar_back.setOnClickListener {
            finish()
        }
    }

    private fun initButton() {

        iv_scroll_to_top.setOnClickListener {
            rv_data.smoothScrollToPosition(0)
        }

        btn_read_letters.setOnClickListener {
            adapter.data = mutableListOf()//清空資料
            viewModel.getMsgCount(InfoCenterViewModel.DataType.UNREAD)//未讀資料比數
            viewModel.getUserMsgList(dataType = InfoCenterViewModel.DataType.READ)//已讀
        }
        btn_unread_letters.setOnClickListener {
            adapter.data = mutableListOf()//清空資料
            viewModel.getMsgCount(InfoCenterViewModel.DataType.READ)//已讀資料筆數
            viewModel.getUserMsgList(dataType = InfoCenterViewModel.DataType.UNREAD)//未讀
        }

        //default show page
        if (mDefaultShowPage == BEEN_READ) btn_read_letters.performClick()
        else btn_unread_letters.performClick()
    }

    private fun initData() {
        viewModel.getUserMsgList(dataType = InfoCenterViewModel.DataType.READ)//已讀
        viewModel.getMsgCount(InfoCenterViewModel.DataType.UNREAD)//未讀資料筆數
    }

    private fun initLiveData() {
        //已讀訊息清單
        viewModel.userReadMsgList.observe(this@InfoCenterActivity, Observer {
            val userMsgList = it ?: return@Observer
            if (adapter.data.isNotEmpty()) {
                adapter.addData(userMsgList)//上拉加載
            } else {
                if ((it.size == 0 || it.isNullOrEmpty()) && !mNowLoading) {
                    image_no_message.visibility = View.VISIBLE
                } else {
                    image_no_message.visibility = View.GONE
                    adapter.data = userMsgList//重新載入
                }
            }
            viewModel.getResult()
        })
        //已讀總筆數
        viewModel.totalReadMsgCount.observe(this@InfoCenterActivity, Observer {
            btn_read_letters.text = String.format(resources.getString(R.string.inbox), it)
        })
        //未讀訊息清單
        viewModel.userUnreadMsgList.observe(this@InfoCenterActivity, Observer {
            val userMsgList = it ?: return@Observer
            if (adapter.data.isNotEmpty()) {
                adapter.addData(userMsgList)//上拉加載
            } else {
                if ((it.size == 0 || it.isNullOrEmpty()) && !mNowLoading) {
                    image_no_message.visibility = View.VISIBLE
                } else {
                    image_no_message.visibility = View.GONE
                    adapter.data = userMsgList//重新載入
                }
            }
            viewModel.getResult()
        })
        //未讀總筆數
        viewModel.totalUnreadMsgCount.observe(this@InfoCenterActivity, {
            btn_unread_letters.text = String.format(resources.getString(R.string.unread_letters), it)
        })
        //Loading
        viewModel.isLoading.observe(this, {
            if (it) {
                mNowLoading = it
                loading()
            } else {
                mNowLoading = it
                hideLoading()
            }
        })
    }

    private fun initRecyclerView() {
        rv_data.layoutManager = LinearLayoutManager(this@InfoCenterActivity, LinearLayoutManager.VERTICAL, false)
        rv_data.adapter = adapter
        rv_data.addOnScrollListener(recyclerViewOnScrollListener)
    }
}