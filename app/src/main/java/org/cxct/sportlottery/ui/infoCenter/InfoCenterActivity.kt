package org.cxct.sportlottery.ui.infoCenter

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_info_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel


class InfoCenterActivity : BaseActivity<InfoCenterViewModel>(InfoCenterViewModel::class) {

    private val infoCenterViewModel: InfoCenterViewModel by viewModel()
    var adapter = InfoCenterAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_center)

        initLiveData()
        initRecyclerView()
    }

    private fun initLiveData() {
        //獲取訊息清單
        infoCenterViewModel.userMsgList.observe(this@InfoCenterActivity, Observer {
            val userMsgList = it ?: return@Observer
            adapter.data = userMsgList
        })
        //打開訊息->變成已讀
        infoCenterViewModel.setMsgReadResult.observe(this@InfoCenterActivity, Observer {
            val apiResult = it ?: return@Observer
            if(!apiResult.success){ //TODO Bill 之後改成跳窗顯示 等公版
                Toast.makeText(this@InfoCenterActivity,"${apiResult.msg}",Toast.LENGTH_SHORT)
            }else{
                Toast.makeText(this@InfoCenterActivity,"${apiResult.msg}",Toast.LENGTH_SHORT)
            }
        })
    }
    private fun initRecyclerView() {
        rv_data.layoutManager = LinearLayoutManager(this@InfoCenterActivity, LinearLayoutManager.VERTICAL, false)//LinearLayout
        rv_data.adapter = adapter
    }
}