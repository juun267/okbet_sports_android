package org.cxct.sportlottery.ui.betRecord

import android.annotation.SuppressLint
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.FragmentUnsettledBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.bet.settledDetailList.RemarkBetRequest
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.service.dispatcher.BetCashOutDispatcher
import org.cxct.sportlottery.service.dispatcher.CashoutMatchStatusDispatcher
import org.cxct.sportlottery.service.dispatcher.CashoutSwitchDispatcher
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.betRecord.adapter.RecyclerUnsettledAdapter
import org.cxct.sportlottery.ui.betRecord.dialog.PrintDialog
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.BetEmptyView
import org.cxct.sportlottery.view.CashOutButton
import org.cxct.sportlottery.view.isVisible
import org.cxct.sportlottery.view.rumWithSlowRequest
import timber.log.Timber
import java.util.*

/**
 * 未结单列表
 */
class UnsettledFragment : BaseFragment<AccountHistoryViewModel, FragmentUnsettledBinding>() {
    private var mAdapter = RecyclerUnsettledAdapter()
    private val oneDay = 60 * 60 * 24 * 1000
    private val refreshHelper by lazy { RefreshHelper.of(binding.recyclerUnsettled, this, false) }

    var pageIndex=1
    var hasMore=true
    var startTime:Long?=null
    var endTime:Long?=null

    override fun onInitView(view: View) = binding.run {
        startTime = TimeUtil.getTodayStartTimeStamp()
        endTime = TimeUtil.getTodayEndTimeStamp()
        recyclerUnsettled.layoutManager = LinearLayoutManager(requireContext())
        recyclerUnsettled.adapter = mAdapter
        refreshHelper.setLoadMoreListener(object : RefreshHelper.LoadMore {
            override fun onLoadMore(pageIndex: Int, pageSize: Int) {
                if (hasMore) {
                    getUnsettledData()
                }else{
                    refreshHelper.finishLoadMoreWithNoMoreData()
                }
            }
        })
        mAdapter.setEmptyView(BetEmptyView(requireContext()))
        mAdapter.setOnItemClickListener{_, view, position ->
            // 此处不知为啥会越界java.lang.IndexOutOfBoundsException: Index 1 out of bounds for length 1
            val data = mAdapter.data.getOrNull(position) ?: return@setOnItemClickListener
            if(data.cashoutOperationStatus == CashOutButton.STATUS_COMFIRMING ){
                data.cashoutOperationStatus =  0
                mAdapter.notifyItemChanged(position)
            }
        }
        mAdapter.setOnItemChildClickListener { _, view, position ->
            val data = mAdapter.data[position]
            when (view.id) {
                //打印点击
                R.id.tvOrderPrint -> {
                    val dialog = PrintDialog(requireContext())
                    dialog.tvPrintClickListener = { it1 ->
                        if (it1?.isNotEmpty() == true) {
                            val orderNo = data.orderNo
                            val orderTime = data.betConfirmTime
                            val requestBet = RemarkBetRequest(orderNo, it1, orderTime.toString())
                            viewModel.observerRemarkBetLiveData {
                                hideLoading()
                                dialog.dismiss()
                                val newUrl =
                                    Constants.getPrintReceipt(
                                        requireContext(),
                                        it.remarkBetResult?.uniqNo,
                                        orderTime.toString(),
                                        it1
                                    )
                                JumpUtil.toExternalWeb(requireContext(), newUrl)
                            }
                            loading()
                            viewModel.reMarkBet(requestBet)
                        }
                    }
                    dialog.show()
                }
                R.id.cashoutBtn->{
                    if (data.cashoutStatus==1 && data.cashoutOperationStatus==0){
                        mAdapter.selectedCashOut(data)
                    }else if(data.cashoutOperationStatus == CashOutButton.STATUS_COMFIRMING ){
                        data.cashoutOperationStatus =  CashOutButton.STATUS_BETTING
                        mAdapter.notifyItemChanged(position)
                        data.cashoutAmount?.let {
                            loading()
                            viewModel.cashOut(data.uniqNo, it)
                        }
                    }
                }
            }
        }
        //待成立倒计时结束刷新数据
        mAdapter.setOnCountTime {
            reloadData()
        }
        //tab切换
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onTabSelected(tab: TabLayout.Tab) {
                loading()
                hasMore = true
                mAdapter.setNewInstance(null)
                when (tab.position) {
                    0 -> {
                        //今天
                        startTime = TimeUtil.getTodayStartTimeStamp()
                        endTime = TimeUtil.getTodayEndTimeStamp()
                    }

                    1 -> {
                        //昨天
                        startTime = TimeUtil.getDefaultTimeStamp(1).startTime!!.toLong()
                        endTime = startTime!! + oneDay - 1
                    }

                    2 -> {
                        //7天
                        startTime = TimeUtil.getDefaultTimeStamp(6).startTime!!.toLong()
                        endTime = TimeUtil.getTodayEndTimeStamp()
                    }
                    3 -> {
                        //其他  最近90天内除开最近30天的前60天
                        startTime = dateSearchBar.startTime
                        endTime = dateSearchBar.endTime
                    }
                }
                dateSearchBar.isVisible = tab.position==3
                //刷新数据
                recyclerUnsettled.gone()
                reloadData()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        dateSearchBar.setOnClickSearchListener {
            if((dateSearchBar.endTime?:0)-(dateSearchBar.startTime?:0)>7*24*3600*1000){
                ToastUtil.showToast(requireContext(),R.string.P274)
                return@setOnClickSearchListener
            }
            pageIndex=1
            startTime = dateSearchBar.startTime
            endTime = dateSearchBar.endTime
            //刷新数据
            recyclerUnsettled.gone()
            getUnsettledData()
        }
    }

    override fun onInitData() {
        initObservable()
        getUnsettledData()
        checkCashOutStatus()
    }
    private fun initObservable() {
        viewModel.unSettledResult.observe(this) {
            hideLoading()
            if (it==null){
                binding.recyclerUnsettled.visible()
                return@observe
            }
            if (it.success) {
                pageIndex++
                it.rows?.let {
                    if(it.isEmpty()){
                        hasMore=false
                    }
                    refreshHelper.finishWithSuccess()
                    binding.recyclerUnsettled.visible()
                    if (pageIndex == 2) {
                        mAdapter.setList(it)
                    } else {
                        mAdapter.addData(it)
                    }
                }
            } else {
                ToastUtil.showToast(requireContext(), it.msg)
            }
        }
        viewModel.settlementNotificationMsg.observe(this) { event ->
            val it = event.getContentIfNotHandled() ?: return@observe
            if (it.status == Status.UN_DONE.code || it.status == Status.CANCEL.code) {
                reloadData()
            }
        }
        viewModel.cashOutEvent.observe(this){
            hideLoading()
            val cashOutResult = it.getData()
            if (it.succeeded() && cashOutResult?.status==1) {
//                pageIndex = 1
//                getUnsettledData()
                //结算成功
//                showPromptDialog(getString(R.string.prompt), getString(R.string.B74)) {}
//                mAdapter.data?.firstOrNull { it.uniqNo == cashOutResult.uniqNo }?.let {
//                    mAdapter.remove(it)
//                }
            }else if(it.succeeded() && cashOutResult?.status==2){
                //金额不对，结算失败，更新显示金额
                showErrorPromptDialog(getString(R.string.prompt),"${getString(R.string.B72)} $showCurrencySign ${cashOutResult.cashoutAmount}") {}
                if (!cashOutResult.uniqNo.isNullOrEmpty()) {
                    mAdapter.updateItemCashOutByUniqNo(
                        cashOutResult.uniqNo!!,
                        cashOutResult.status,
                        cashOutResult.cashoutAmount
                    )
                }
            }else{
                //status=0 或者其他都认为结算失败
                showErrorPromptDialog(getString(R.string.prompt),getString(R.string.B75)){}
                clearAndReloadData()
            }
        }
        viewModel.checkCashOutStatusEvent.observe(this){
            delayCheckCashOutStatus()
            if (!it.isNullOrEmpty()){
                mAdapter.updateCashOut(it)
            }
        }
        BetCashOutDispatcher.observe(this){ event->
            val item = mAdapter.data?.firstOrNull { it.uniqNo == event.sportBet.uniqNo }?: return@observe
            if (event.cashOutStatus==1){
                mAdapter.remove(item)
                showPromptDialog(getString(R.string.prompt), getString(R.string.B74)) {}
            }else{
                showErrorPromptDialog(getString(R.string.prompt),getString(R.string.B75)){}
                clearAndReloadData()
            }
        }
    }
    private fun clearAndReloadData(){
        mAdapter.data.clear()
        mAdapter.notifyDataSetChanged()
        reloadData()
    }
    private fun reloadData(){
        binding.recyclerUnsettled.postDelayed({
            pageIndex = 1
            getUnsettledData()
        },500)
    }
    private fun getUnsettledData() {
        //获取结算数据
        rumWithSlowRequest(viewModel){
            viewModel.getUnsettledList(pageIndex,startTime,endTime)
        }
    }

    /**
     * 延时请求，带上生命周期
     */
    private fun delayCheckCashOutStatus(){
        lifecycle.coroutineScope.launch {
            delay(3000)
            checkCashOutStatus()
        }
    }

    /**
     * 检查列表数据请求cashout 状态
     */
    private fun checkCashOutStatus(){
        val uniqNos = mAdapter.data.filter { it.cashoutStatus in 1..2 }.mapNotNull { it.uniqNo }
        if (uniqNos.isNullOrEmpty()) {
            delayCheckCashOutStatus()
        }else{
            viewModel.checkCashOutStatus(uniqNos)
        }
    }

}