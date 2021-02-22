package org.cxct.sportlottery.ui.infoCenter

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_info_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseToolBarActivity

const val BEEN_READ = 0
const val YET_READ = 1
class InfoCenterActivity : BaseToolBarActivity<InfoCenterViewModel>(InfoCenterViewModel::class) {

    var adapter: InfoCenterAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initLiveData()
        initRecyclerView()
        initData()
        initButton()
    }

    private fun initView() {
        isOpenMenu(false)
    }

    private fun initButton() {
        btn_readed_letters.setOnClickListener {
            letterTabStatus(BEEN_READ)
            adapter?.data = mutableListOf()//清空資料
            viewModel.getUserMsgList(true, 0, InfoCenterViewModel.DataType.READED)//已讀
            viewModel.getMsgCount(InfoCenterViewModel.DataType.UNREAD)//未讀資料比數
        }
        btn_unread_letters.setOnClickListener {
            letterTabStatus(YET_READ)
            adapter?.data = mutableListOf()//清空資料
            viewModel.getMsgCount(InfoCenterViewModel.DataType.READED)//已讀資料筆數
            viewModel.getUserMsgList(true, 0, InfoCenterViewModel.DataType.UNREAD)//未讀
        }
        letterTabStatus(BEEN_READ)
    }

    private fun letterTabStatus(status: Int){
        when(status){
            BEEN_READ ->{
                btn_readed_letters.isSelected = true
                btn_unread_letters.isSelected = false
                btn_readed_letters.setTextColor(ContextCompat.getColor(this, R.color.pumpkinOrange))
                v_been_read_indicator.visibility = View.VISIBLE
                btn_unread_letters.setTextColor(ContextCompat.getColor(this, R.color.gray3))
                v_yet_read_indicator.visibility = View.GONE
            }
            YET_READ -> {
                btn_readed_letters.isSelected = false
                btn_unread_letters.isSelected = true
                btn_readed_letters.setTextColor(ContextCompat.getColor(this, R.color.gray3))
                v_been_read_indicator.visibility = View.GONE
                btn_unread_letters.setTextColor(ContextCompat.getColor(this, R.color.pumpkinOrange))
                v_yet_read_indicator.visibility = View.VISIBLE
            }
        }
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
                adapter?.data = userMsgList//重新載入
            }
        })
        //已讀總筆數
        viewModel.totalReadedMsgCount.observe(this@InfoCenterActivity, {
            btn_readed_letters.text =
                String.format(resources.getString(R.string.inbox), it)
        })
        //未讀訊息清單
        viewModel.userUnreadMsgList.observe(this@InfoCenterActivity, Observer {
            val userMsgList = it ?: return@Observer
            if (adapter?.data?.isNotEmpty() == true) {
                adapter?.addData(userMsgList)//上拉加載
            } else {
                adapter?.data = userMsgList//重新載入
            }
        })
        //未讀總筆數
        viewModel.totalUnreadMsgCount.observe(this@InfoCenterActivity, {
            btn_unread_letters.text =
                String.format(resources.getString(R.string.unread_letters), it)
        })
        //Loading
        viewModel.isLoading.observe(this@InfoCenterActivity, {
            if (it)
                loading()
            else
                hideLoading()

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

    override fun setContentView(): Int {
        return R.layout.activity_info_center
    }

    override fun setToolBarName(): String {
        return "消息中心"
    }

}