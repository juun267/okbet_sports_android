package org.cxct.sportlottery.ui.betRecord

import android.view.View
import android.widget.FrameLayout
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_bet_record.*
import kotlinx.android.synthetic.main.view_status_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel.Companion.PAGE_SIZE
import org.cxct.sportlottery.ui.betRecord.accountHistory.first.AccountHistoryAdapter
import org.cxct.sportlottery.ui.betRecord.accountHistory.first.ItemClickListener
import org.cxct.sportlottery.ui2.common.adapter.StatusSheetData
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.sport.favorite.SportTypeTextAdapter
import org.cxct.sportlottery.util.EventBusUtil
import timber.log.Timber

class BetRecordFragment: BaseFragment<AccountHistoryViewModel>(AccountHistoryViewModel::class) {

    private val recordDiffAdapter by lazy { TransactionRecordDiffAdapter(viewModel) }
    private val colorSettled = R.color.color_FFFFFF_414655
    private val colorNotSettled = R.color.color_6C7BA8_6C7BA8
    private var startTabPosition: Int = 0

    private val rvAdapter by lazy {
        AccountHistoryAdapter(ItemClickListener {
            if (it == null) {
                return@ItemClickListener
            }

            viewModel.setSelectedDate(it.statDate)
            if (activity is MainTabActivity) {
                (activity as MainTabActivity).goBetRecordDetails(it, it.statDate.orEmpty(), viewModel.gameTypeCode)
            }
        })
    }

    override fun layoutId() = R.layout.fragment_bet_record

    override fun onBindView(view: View) {
//        viewModel.getConfigData()
        initView()
        initObservable()
        initPopwindow()
        initData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            initData()
        }
    }

    private fun initView() {
        initToolBar()
        initTabBar()
        initRecyclerView()
    }

    private var dataSport = mutableListOf<Item>()
    private val mListPop: ListPopupWindow by lazy { ListPopupWindow(requireContext()) }

    private fun initPopwindow() = mListPop.run {
        width = FrameLayout.LayoutParams.WRAP_CONTENT
        height = FrameLayout.LayoutParams.WRAP_CONTENT
        setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_pop_up_arrow))
        setAdapter(SportTypeTextAdapter(dataSport))
        anchorView = cl_bet_all_sports //设置ListPopupWindow的锚点，即关联PopupWindow的显示位置和这个锚点
        isModal = true //设置是否是模式
        setOnDismissListener { cl_bet_all_sports.isSelected = false }
        setOnItemClickListener { _, _, position, _ ->
            dismiss()
            val sportItem = dataSport[position]
            sportItem.isSelected = true
            tv_all_sports.text = sportItem.name
            viewModel.apply {
                gameTypeCode = sportItem.code
                searchBetRecord(gameTypeCode)
                getBetList(true, gameTypeCode)
            }
        }
    }

    private fun initObservable() {
        viewModel.sportCodeList.observe(viewLifecycleOwner) { updateSportList(it) }
        viewModel.betListData.observe(viewLifecycleOwner) {
            recordDiffAdapter.setupBetList(it)
            Timber.d("  rv_record_unsettled.scrollTo(0,0)")
            val layoutManager = rv_record_unsettled.layoutManager  as LinearLayoutManager
            if ( layoutManager.findFirstVisibleItemPosition() != 0){
                rv_record_unsettled.smoothScrollToPosition(0)
            }
        }

        viewModel.betRecordResult.observe(viewLifecycleOwner) {
            if (it.success) {
                rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)
            } else {
                Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.settlementNotificationMsg.observe(viewLifecycleOwner) { event ->
            val it = event.getContentIfNotHandled() ?: return@observe
            if (it.status == Status.UN_DONE.code || it.status == Status.CANCEL.code) {
                viewModel.getBetList(true)
            }
        }
    }

    fun initToolBar() {
        ImmersionBar.with(this)
            .statusBarView(v_statusbar)
            .statusBarDarkFont(true)
            .fitsSystemWindows(true)
            .init()

        iv_menu_left.setOnClickListener { EventBusUtil.post(MenuEvent(true)) }
        iv_logo.setOnClickListener { (activity as MainTabActivity).backMainHome() }
        cl_bet_all_sports.setOnClickListener {
            if (mListPop.isShowing) {
                cl_bet_all_sports.isSelected = false
                mListPop.dismiss()
            } else {
                cl_bet_all_sports.isSelected = true
                mListPop.show()
            }
        }
    }

    private fun initTabBar() {
        tv_tab_settled.setOnClickListener { setupTabUI(true) }
        tv_tab_not_settled.setOnClickListener { setupTabUI(false) }
        //預設為已結算tab
        setupTabUI(startTabPosition == 0)
    }

    private fun setupTabUI(isSettledTab: Boolean) = viewModel.run {
        settled_title_bar.isVisible = isSettledTab
        sm_settled_tab_refresh.isVisible = isSettledTab
        sm_unsettled_tab_refresh.isVisible = !isSettledTab
        if (isSettledTab) {
            tabPosition = 0
            bet_tab.setBackgroundResource(R.drawable.bg_bet_record_tab_1)
            tv_tab_settled.setTextColor(ContextCompat.getColor(requireContext(), colorSettled))
            tv_tab_not_settled.setTextColor(ContextCompat.getColor(requireContext(), colorNotSettled))
            return@run
        }

        tabPosition = 1
        bet_tab.setBackgroundResource(R.drawable.bg_bet_record_tab_2)
        tv_tab_not_settled.setTextColor(ContextCompat.getColor(requireContext(), colorSettled))
        tv_tab_settled.setTextColor(ContextCompat.getColor(requireContext(), colorNotSettled))
    }

    private fun initRecyclerView() {
        rv_record_settled.adapter = rvAdapter
        rv_record_settled.addOnScrollListener(settledRecyclerViewOnScrollListener)
        rv_record_unsettled.adapter = recordDiffAdapter
        rv_record_unsettled.addOnScrollListener(unsettledRecyclerViewOnScrollListener)
    }

    private fun initData() {
        viewModel.getSportList()
        viewModel.getBetList(true)
        viewModel.searchBetRecord()
    }

    private fun updateSportList(list: List<StatusSheetData>) {
        if (dataSport.isNotEmpty()) return
        list.forEach {
            dataSport.add(
                Item(
                    code = it.code.orEmpty(),
                    name = it.showName.orEmpty(),
                    num = 0,
                    play = null,
                    sortNum = 0
                )
            )
        }
        //如沒有選過的，預設選第一個
        dataSport.find {
            it.isSelected
        }.let {
            if (it == null) {
                dataSport.firstOrNull()?.isSelected = true
            }
        }
    }

    private val settledRecyclerViewOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (recyclerView.layoutManager == null) {
                return
            }

            val it = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemCount: Int = it.childCount
            val totalItemCount: Int = it.itemCount
            val firstVisibleItemPosition: Int = it.findFirstVisibleItemPosition()
            if (firstVisibleItemPosition <= 0) {
                return
            }

            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                && firstVisibleItemPosition >= 0
                && totalItemCount >= PAGE_SIZE
            ) {
                viewModel.getBetList()
            }
        }
    }

    private val unsettledRecyclerViewOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (recyclerView.layoutManager == null) {
                return
            }

            val it = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemCount: Int = it.childCount
            val totalItemCount: Int = it.itemCount
            val firstVisibleItemPosition: Int = it.findFirstVisibleItemPosition()
            if (firstVisibleItemPosition > 0) {
                viewModel.getNextPage(visibleItemCount, firstVisibleItemPosition, totalItemCount)
            }
        }
    }

    fun selectTab(tabPosition: Int) {
        this.startTabPosition = tabPosition
        if (!isAdded) {
            return
        }

        setupTabUI(tabPosition == 0)
        if (tabPosition == 0) {
            rv_record_settled.scrollToPosition(0)
        } else {
            rv_record_unsettled.scrollToPosition(0)
        }
    }
}