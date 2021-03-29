package org.cxct.sportlottery.ui.infoCenter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_info_center.*
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

        private fun scrollToTopControl(firstVisibleItemPosition: Int) {
            iv_scroll_to_top.apply {
                if (firstVisibleItemPosition > 0) {
                    if (alpha == 0f) {
                        alpha = 0f
                        visibility = View.VISIBLE
                        animate().alpha(1f).setDuration(300).setListener(null)
                    }
                } else {
                    if (alpha == 1f) {
                        alpha = 1f
                        animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                visibility = View.GONE
                            }
                        })
                    }
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            recyclerView.layoutManager?.let {
                val firstVisibleItemPosition: Int = (it as LinearLayoutManager).findFirstVisibleItemPosition()
                when (btn_read_letters.isSelected) {
                    true -> viewModel.getUserMsgList(false, adapter.itemCount, InfoCenterViewModel.DataType.READ)
                    false -> viewModel.getUserMsgList(false, adapter.itemCount, InfoCenterViewModel.DataType.UNREAD)
                }
                scrollToTopControl(firstVisibleItemPosition)
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