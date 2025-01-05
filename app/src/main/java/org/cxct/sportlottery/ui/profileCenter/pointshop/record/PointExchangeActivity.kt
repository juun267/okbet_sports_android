package org.cxct.sportlottery.ui.profileCenter.pointshop.record


import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.databinding.ActivityPointExchangeBinding
import org.cxct.sportlottery.databinding.ViewBetEmptyBinding
import org.cxct.sportlottery.databinding.ViewPointEmptyBinding
import org.cxct.sportlottery.databinding.ViewVipActivatedEmptyBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.profileCenter.pointshop.OrderDetailActivity
import org.cxct.sportlottery.ui.profileCenter.pointshop.PointShopViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.RefreshHelper
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.setMargins
import org.cxct.sportlottery.view.DividerItemDecorator
import org.cxct.sportlottery.view.setColors

class PointExchangeActivity: BaseActivity<PointShopViewModel, ActivityPointExchangeBinding>() {

    override fun pageName() = "兑换记录"

    private lateinit var refreshHelper: RefreshHelper
    private val pointExchangeAdapter = PointExchangeAdapter()
    private var currentPage = 1
    private var status: Int? =null

    override fun onInitView() {
        setStatusbar(R.color.color_F6F7F8, true)
        binding.toolBar.binding.root.setBackgroundResource(R.color.color_F6F7F8)
        binding.toolBar.setOnBackPressListener {
            finish()
        }
        initTab()
        initRecylerView()
        initObservable()
        getRedeemList(1)
    }

    private fun initTab(){
        val sheetList: List<StatusSheetData> = listOf(
            StatusSheetData("-1", getString(R.string.label_all)),
            StatusSheetData("0", getString(R.string.A105)),
            StatusSheetData("1", getString(R.string.A108)),
            StatusSheetData("2", getString(R.string.A109)),
            StatusSheetData("3", getString(R.string.A106)),
            StatusSheetData("4", getString(R.string.A107)),
        )
        binding.selecotrView.apply {
            dataList = sheetList
            setOnItemSelectedListener{
                selectedStatus(it.code.toIntS(-1))
            }
            selectedTag = "-1"
            selectedText = getString(R.string.label_all)
            viewBinding.clRoot.setBackgroundResource(R.drawable.bg_gray_radius_16_f6f7f8)
            viewBinding.tvSelected.setColors(R.color.color_6D7693)
            viewBinding.tvSelected.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            viewBinding.imgArrow.apply {
                val lParams = layoutParams as ViewGroup.MarginLayoutParams
                lParams.width = 12.dp
                lParams.height = 12.dp
                lParams.rightMargin = 12.dp
                layoutParams = lParams
            }
            resetListHeight()
        }
    }
    private fun selectedStatus(status: Int){
        this.status = if(status>=0) status else null
        getRedeemList(1)
    }
    private fun initRecylerView()=binding.rvExchange.run{
        refreshHelper = RefreshHelper.of(this, this@PointExchangeActivity, true, true)
        refreshHelper.setRefreshListener {
            getRedeemList(1)
        }
        refreshHelper.setLoadMoreListener(object : RefreshHelper.LoadMore {
            override fun onLoadMore(pageIndex: Int, pageSize: Int) {
                getRedeemList(currentPage++)
            }
        })
        setLinearLayoutManager()
        addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.recycleview_decoration_e0e3ee)))
        adapter = pointExchangeAdapter
        pointExchangeAdapter.setEmptyView(ViewPointEmptyBinding.inflate(layoutInflater).root)
        pointExchangeAdapter.setOnItemClickListener { adapter, view, position ->
           OrderDetailActivity.start(this@PointExchangeActivity,pointExchangeAdapter.getItem(position))
        }
    }
    private fun initObservable(){
        viewModel.redeemList.collectWith(lifecycleScope){
            refreshHelper.finishRefresh()
            refreshHelper.finishLoadMore()
            if (it.success){
                currentPage = it.page?:1
                if(currentPage == 1){
                    pointExchangeAdapter.setList(it.getData())
                }else{
                    it.getData()?.let {
                            it1 -> pointExchangeAdapter.addData(newData = it1)
                    }
                }
                refreshHelper.setLoadMoreEnable(pointExchangeAdapter.itemCount<it.total)
            }else{
              ToastUtil.showToast(this,it.msg)
            }
        }
    }
    private fun getRedeemList(page: Int){
        viewModel.getRedeemList(page, status)
    }
}