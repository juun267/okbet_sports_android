package org.cxct.sportlottery.ui.infoCenter

import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_info_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityInfoCenterBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel


class InfoCenterActivity : BaseActivity<InfoCenterViewModel>(InfoCenterViewModel::class) {

    var adapter = InfoCenterAdapter()
    private lateinit var infoCenterBinding: ActivityInfoCenterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDataBiding()
        initLiveData()
        initRecyclerView()
        initData()
    }

    private fun initDataBiding() {
        infoCenterBinding = DataBindingUtil.setContentView(this, R.layout.activity_info_center)
        infoCenterBinding.apply {
            infoCenterViewModel = this@InfoCenterActivity.viewModel
            lifecycleOwner = this@InfoCenterActivity
        }
    }

    private fun initData() {
        viewModel.getUserMsgList(true, 0)
    }

    private fun initLiveData() {
        //獲取訊息清單
        viewModel.userMsgList.observe(this@InfoCenterActivity, Observer {
            val userMsgList = it ?: return@Observer
            if (adapter.data.isNotEmpty()) {
                adapter.addData(userMsgList)//上拉加載
            } else {
                adapter.data = userMsgList//重新載入
            }
        })
    }

    private fun initRecyclerView() {
        rv_data.layoutManager = LinearLayoutManager(
            this@InfoCenterActivity,
            LinearLayoutManager.VERTICAL,
            false
        )//LinearLayout
        rv_data.adapter = adapter

        rv_data.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1))
                    viewModel.getUserMsgList(true, 0)
            }
        })
    }

}