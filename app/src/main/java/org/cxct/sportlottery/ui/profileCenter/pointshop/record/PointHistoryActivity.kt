package org.cxct.sportlottery.ui.profileCenter.pointshop.record


import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.ActivityPointHistoryBinding
import org.cxct.sportlottery.databinding.ViewPointEmptyBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.pointshop.PointShopViewModel
import org.cxct.sportlottery.util.RefreshHelper
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.DividerItemDecorator
import org.cxct.sportlottery.view.setColors

class PointHistoryActivity: BaseActivity<PointShopViewModel, ActivityPointHistoryBinding>() {

    override fun pageName() = "积分规则"
    private lateinit var refreshHelper: RefreshHelper
    private val pointHistoryAdapter = PointHistoryAdapter()
    private var currentPage = 1
    private var quertType:Int? = null //0:积分收入类型, 1:积分支出类型

    override fun onInitView() {
        setStatusbar(R.color.color_F6F7F8, true)
        binding.toolBar.binding.root.setBackgroundResource(R.color.color_F6F7F8)
        binding.toolBar.setOnBackPressListener {
            finish()
        }
        initTab()
        initRecylerView()
        initObservable()
        getPointBill(1)
    }

    private fun initTab(){
        binding.tvTabAll.setOnClickListener {
            selectTab(0)
            quertType = null
            getPointBill(1)
        }
        binding.tvTabIncome.setOnClickListener {
            selectTab(1)
            quertType = 0
            getPointBill(1)
        }
        binding.tvTabExpenses.setOnClickListener {
            selectTab(2)
            quertType = 1
            getPointBill(1)
        }
        selectTab(0)
    }
    private fun selectTab(position: Int){
        binding.linTab.children.forEachIndexed { index, view ->
            val tabView = view as TextView
            if (index == position){
                tabView.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,R.drawable.ic_underline_smile)
                tabView.setColors(R.color.color_025BE8)
            }else{
                tabView.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0)
                tabView.setColors(R.color.color_0D2245)
            }
        }
    }
    private fun initRecylerView()=binding.rvHistory.run{
        refreshHelper = RefreshHelper.of(this, this@PointHistoryActivity, true, true)
        refreshHelper.setRefreshListener {
            getPointBill(1)
        }
        refreshHelper.setLoadMoreListener(object : RefreshHelper.LoadMore {
            override fun onLoadMore(pageIndex: Int, pageSize: Int) {
                getPointBill(currentPage++)
            }
        })
        setLinearLayoutManager()
        addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.recycleview_decoration_e0e3ee)))
        adapter = pointHistoryAdapter
        pointHistoryAdapter.setEmptyView(ViewPointEmptyBinding.inflate(layoutInflater).root)
        pointHistoryAdapter.setOnItemClickListener { adapter, view, position ->

        }
    }
    private fun initObservable(){
        viewModel.pointBillList.collectWith(lifecycleScope){
            refreshHelper.finishRefresh()
            refreshHelper.finishLoadMore()
            if (it.success){
                currentPage = it.page?:1
                if(currentPage == 1){
                    pointHistoryAdapter.setList(it.getData())
                }else{
                    it.getData()?.let {
                            it1 -> pointHistoryAdapter.addData(newData = it1)
                    }
                }
                refreshHelper.setLoadMoreEnable(pointHistoryAdapter.itemCount<it.total)
            }else{
                ToastUtil.showToast(this,it.msg)
            }
        }
    }
    private fun getPointBill(page: Int){
        viewModel.getPointBill(page,quertType)
    }

}