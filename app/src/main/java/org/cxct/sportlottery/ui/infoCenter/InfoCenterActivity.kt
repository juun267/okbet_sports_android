package org.cxct.sportlottery.ui.infoCenter

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_info_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseToolBarActivity

class InfoCenterActivity : BaseToolBarActivity<InfoCenterViewModel>(InfoCenterViewModel::class) {

    var adapter: InfoCenterAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_center)
        initLiveData()
        initRecyclerView()
        initData()
    }

    private fun initData() {
        viewModel.getUserMsgList(true, 0)
    }

    private fun initLiveData() {
        //獲取訊息清單
        viewModel.userMsgList.observe(this@InfoCenterActivity, Observer {
            val userMsgList = it ?: return@Observer
            if (adapter?.data?.isNotEmpty() == true) {
                adapter?.addData(userMsgList)//上拉加載
            } else {
                adapter?.data = userMsgList//重新載入
            }
        })
    }

    private fun initRecyclerView() {
        adapter = InfoCenterAdapter(InfoCenterAdapter.ItemClickListener {
            it.let { data ->
                val detailDialog = InfoCenterDetailDialog(data)
                detailDialog.show(supportFragmentManager, "")
            }
        })

        rv_data.layoutManager = LinearLayoutManager(
            this@InfoCenterActivity,
            LinearLayoutManager.VERTICAL,
            false
        )//LinearLayout
        rv_data.adapter = adapter

        rv_data.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1))
                    viewModel.getUserMsgList(false, adapter?.itemCount?:0)
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