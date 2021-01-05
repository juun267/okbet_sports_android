package org.cxct.sportlottery.ui.infoCenter

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_info_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel


class InfoCenterActivity : BaseActivity<InfoCenterViewModel>(InfoCenterViewModel::class) {

    private val infoCenterViewModel: InfoCenterViewModel by viewModel()
    var adapter = InfoCenterAdapter()

    var mNextRequestPage = 1
    var pageSize = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_center)


        initLiveData()
        initRecyclerView()
        initData()
    }

    private fun initData() {
        infoCenterViewModel.getUserMsgList(true,0)
    }

    private fun initLiveData() {
        //獲取訊息清單
        infoCenterViewModel.userMsgList.observe(this@InfoCenterActivity, Observer {
            val userMsgList = it ?: return@Observer
            if(adapter.data.isNotEmpty()){
                adapter.addData(userMsgList)//上拉加載
            }else{
                adapter.data = userMsgList//重新載入
            }
        })
        //打開訊息->變成已讀
//        infoCenterViewModel.setMsgReadResult.observe(this@InfoCenterActivity, Observer {
//            val apiResult = it ?: return@Observer
//            if(!apiResult.success){ //之後改成跳窗顯示 等公版
//                Toast.makeText(this@InfoCenterActivity,"${apiResult.msg}",Toast.LENGTH_SHORT)
//            }else{
//                Toast.makeText(this@InfoCenterActivity,"${apiResult.msg}",Toast.LENGTH_SHORT)
//            }
//        })
    }
    private fun initRecyclerView() {
        rv_data.layoutManager = LinearLayoutManager(this@InfoCenterActivity, LinearLayoutManager.VERTICAL, false)//LinearLayout
        rv_data.adapter = adapter

        rv_data.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1))
                infoCenterViewModel.getUserMsgList(true,0)
            }
        })
    }

}