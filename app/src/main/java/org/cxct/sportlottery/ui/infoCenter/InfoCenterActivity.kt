package org.cxct.sportlottery.ui.infoCenter

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_info_center.*
import kotlinx.android.synthetic.main.item_footer_no_data.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity

class InfoCenterActivity : BaseOddButtonActivity<InfoCenterViewModel>(InfoCenterViewModel::class) {

    companion object {
        const val KEY_READ_PAGE = "key-read-page"
        const val BEEN_READ = 0
        const val YET_READ = 1
    }

    private val mDefaultShowPage by lazy { intent.getIntExtra(KEY_READ_PAGE, BEEN_READ) }

    var adapter: InfoCenterAdapter? = null
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
        btn_readed_letters.setOnClickListener {
            adapter?.data = mutableListOf()//清空資料
            viewModel.getUserMsgList(true, 0, InfoCenterViewModel.DataType.READED)//已讀
            viewModel.getMsgCount(InfoCenterViewModel.DataType.UNREAD)//未讀資料比數
        }
        btn_unread_letters.setOnClickListener {
            adapter?.data = mutableListOf()//清空資料
            viewModel.getMsgCount(InfoCenterViewModel.DataType.READED)//已讀資料筆數
            viewModel.getUserMsgList(true, 0, InfoCenterViewModel.DataType.UNREAD)//未讀
        }

        //default show page
        if (mDefaultShowPage == BEEN_READ)
            btn_readed_letters.performClick()
        else
            btn_unread_letters.performClick()
    }

    private fun initData() {
        viewModel.getUserMsgList(true, 0, InfoCenterViewModel.DataType.READED)//已讀
        viewModel.getMsgCount(InfoCenterViewModel.DataType.UNREAD)//未讀資料筆數
    }

    private fun initLiveData() {
        //已讀訊息清單
        viewModel.userReadedMsgList.observe(this@InfoCenterActivity, Observer {
            val userMsgList = it ?: return@Observer
            if (adapter?.data?.isNotEmpty() == true) {
                adapter?.addData(userMsgList)//上拉加載
            } else {
                if ((it.size == 0 || it.isNullOrEmpty()) && !mNowLoading) {
                    image_no_message.visibility = View.VISIBLE
                    tv_no_data.visibility = View.GONE
                } else {
                    image_no_message.visibility = View.GONE
                    tv_no_data.visibility = View.VISIBLE
                    adapter?.data = userMsgList//重新載入
                }
            }
        })
        //已讀總筆數
        viewModel.totalReadedMsgCount.observe(this@InfoCenterActivity, Observer {
            btn_readed_letters.text =
                String.format(resources.getString(R.string.inbox), it)
        })
        //未讀訊息清單
        viewModel.userUnreadMsgList.observe(this@InfoCenterActivity, Observer {
            val userMsgList = it ?: return@Observer
            if (adapter?.data?.isNotEmpty() == true) {
                adapter?.addData(userMsgList)//上拉加載
            } else {
                if ((it.size == 0 || it.isNullOrEmpty()) && !mNowLoading) {
                    image_no_message.visibility = View.VISIBLE
                    tv_no_data.visibility = View.GONE
                } else {
                    image_no_message.visibility = View.GONE
                    tv_no_data.visibility = View.VISIBLE
                    adapter?.data = userMsgList//重新載入
                }
            }
        })
        //未讀總筆數
        viewModel.totalUnreadMsgCount.observe(this@InfoCenterActivity, Observer {
            btn_unread_letters.text =
                String.format(resources.getString(R.string.unread_letters), it)
        })
        //Loading
        viewModel.isLoading.observe(this@InfoCenterActivity, Observer {
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
        adapter = InfoCenterAdapter(InfoCenterAdapter.ItemClickListener {
            it.let { data ->
                val detailDialog = InfoCenterDetailDialog(data)
                detailDialog.show(supportFragmentManager, "")

                //未讀的資料打開要變成已讀
                if (btn_unread_letters.isSelected) {
                    viewModel.setDataReaded(data.id.toString())
                }

            }
        })

        rv_data.layoutManager =
            LinearLayoutManager(this@InfoCenterActivity, LinearLayoutManager.VERTICAL, false)

        rv_data.adapter = adapter

        rv_data.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    when (btn_readed_letters.isSelected) {
                        true -> viewModel.getUserMsgList(
                            false, adapter?.itemCount ?: 0,
                            InfoCenterViewModel.DataType.READED
                        )
                        false -> viewModel.getUserMsgList(
                            false, adapter?.itemCount ?: 0,
                            InfoCenterViewModel.DataType.UNREAD
                        )
                    }

                }

            }
        })
    }
}