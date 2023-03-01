package org.cxct.sportlottery.ui.maintab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_bet_record.*
import kotlinx.android.synthetic.main.fragment_bet_record.iv_logo
import kotlinx.android.synthetic.main.view_status_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.component.StatusSpinnerAdapter
import org.cxct.sportlottery.ui.main.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.main.accountHistory.AccountHistoryViewModel.Companion.PAGE_SIZE
import org.cxct.sportlottery.ui.main.accountHistory.first.AccountHistoryAdapter
import org.cxct.sportlottery.ui.main.accountHistory.first.ItemClickListener
import org.cxct.sportlottery.ui.sport.favorite.SportTypeTextAdapter
import org.cxct.sportlottery.ui.transactionStatus.TransactionRecordDiffAdapter
import org.cxct.sportlottery.util.EventBusUtil
import timber.log.Timber

class BetRecordFragment :
    BaseFragment<AccountHistoryViewModel>(AccountHistoryViewModel::class) {

    private val recordDiffAdapter by lazy { TransactionRecordDiffAdapter() }
    private val colorSettled = R.color.color_FFFFFF_414655
    private val colorNotSettled = R.color.color_6C7BA8_6C7BA8
    private var startTabPosition: Int = 0

    private val rvAdapter by lazy {
        AccountHistoryAdapter(ItemClickListener {
            it.let { data ->
                viewModel.setSelectedDate(data.statDate)
//                val action =
//                    AccountHistoryFragmentDirections.actionAccountHistoryFragmentToAccountHistoryNextFragment(data.statDate)
//                findNavController().navigate(action)
                if (activity is MainTabActivity) {
                    (activity as MainTabActivity).goBetRecordDetails(
                        data,
                        data.statDate.orEmpty(),
                        viewModel.gameTypeCode
                    )
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_bet_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    private var dataList = mutableListOf<StatusSheetData>()
    private var spinnerAdapter: StatusSpinnerAdapter? = null
    private var dataSport = mutableListOf<Item>()
    private lateinit var mListPop: ListPopupWindow

    private fun initPopwindow() {
        mListPop = ListPopupWindow(requireContext())
        mListPop.width = FrameLayout.LayoutParams.WRAP_CONTENT
        mListPop.height = FrameLayout.LayoutParams.WRAP_CONTENT
        mListPop.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.bg_pop_up_arrow
            )
        )
        mListPop.setAdapter(SportTypeTextAdapter(dataSport))
        mListPop.anchorView = cl_bet_all_sports //设置ListPopupWindow的锚点，即关联PopupWindow的显示位置和这个锚点
        mListPop.isModal = true //设置是否是模式
        mListPop.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mListPop.dismiss()
                val sportItem = dataSport[position]
                sportItem.isSelected = true
                tv_all_sports.text = sportItem.name
//                selectItem = dataList.get(position)
//                setSelectCode(selectItem.code)
//                itemSelectedListener?.invoke(selectItem)
//                setSelectInfo(selectItem)
                viewModel.apply {
                    gameTypeCode = sportItem.code
                    searchBetRecord(gameTypeCode)
                    getBetList(true, gameTypeCode)
                }
            }
        })
        mListPop.setOnDismissListener {
            cl_bet_all_sports.isSelected = false
        }
    }

    private fun initObservable() {
        viewModel.sportCodeList.observe(viewLifecycleOwner) {
            System.out.println("============ initObservable size ================" + it.size)
            updateSportList(it)
        }
        viewModel.betListData.observe(viewLifecycleOwner) {
            recordDiffAdapter.setupBetList(it)
            Timber.d("  rv_record_unsettled.scrollTo(0,0)")
            val layoutManager = rv_record_unsettled.layoutManager  as LinearLayoutManager
            if ( layoutManager.findFirstVisibleItemPosition()!=0){
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
            event.getContentIfNotHandled()?.let {
                if (it.status == Status.UN_DONE.code || it.status == Status.CANCEL.code) {
                    viewModel.getBetList(true)
                }
            }
        }
    }

    fun initToolBar() {
        ImmersionBar.with(this)
            .statusBarView(v_statusbar)
            .statusBarDarkFont(true)
            .fitsSystemWindows(true)
            .init()

        iv_menu_left.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
        }
        iv_logo.setOnClickListener {
            (activity as MainTabActivity).jumpToHome(0)
        }
        cl_bet_all_sports.setOnClickListener(View.OnClickListener {
            if (mListPop.isShowing) {
                cl_bet_all_sports.isSelected = false
                mListPop.dismiss()
            } else {
                cl_bet_all_sports.isSelected = true
                mListPop.show()
            }
        })
    }

    private fun initTabBar() {
        tv_tab_settled.setOnClickListener {
            setupTabUI(true)
        }
        tv_tab_not_settled.setOnClickListener {
            setupTabUI(false)
        }
        //預設為已結算tab
        setupTabUI(startTabPosition == 0)
    }

    private fun setupTabUI(isSettledTab: Boolean) {
        viewModel.apply {
            if (isSettledTab) {
                tabPosition = 0
                bet_tab.setBackgroundResource(R.drawable.bg_bet_record_tab_1)
                tv_tab_settled.setTextColor(
                    ContextCompat.getColor(requireContext(), colorSettled)
                )
                tv_tab_not_settled.setTextColor(
                    ContextCompat.getColor(requireContext(), colorNotSettled)
                )
            } else {
                tabPosition = 1
                bet_tab.setBackgroundResource(R.drawable.bg_bet_record_tab_2)
                tv_tab_not_settled.setTextColor(
                    ContextCompat.getColor(requireContext(), colorSettled)
                )
                tv_tab_settled.setTextColor(
                    ContextCompat.getColor(requireContext(), colorNotSettled)
                )
            }
            settled_title_bar.isVisible = isSettledTab
            sm_settled_tab_refresh.isVisible = isSettledTab
            sm_unsettled_tab_refresh.isVisible = !isSettledTab
        }
    }

    private fun initRecyclerView() {
        rv_record_settled.apply {
            adapter = rvAdapter
            addOnScrollListener(settledRecyclerViewOnScrollListener)
        }
        rv_record_unsettled.apply {
            adapter = recordDiffAdapter
            addOnScrollListener(unsettledRecyclerViewOnScrollListener)
        }
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

    private val settledRecyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                recyclerView.layoutManager?.let {
                    val visibleItemCount: Int = it.childCount
                    val totalItemCount: Int = it.itemCount
                    val firstVisibleItemPosition: Int =
                        (it as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (firstVisibleItemPosition > 0) {
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount &&
                            firstVisibleItemPosition >= 0 &&
                            totalItemCount >= PAGE_SIZE
                        ) {
                            viewModel.getBetList()
                        }
                    }
                }
            }
        }

    private val unsettledRecyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                recyclerView.layoutManager?.let {
                    val visibleItemCount: Int = it.childCount
                    val totalItemCount: Int = it.itemCount
                    val firstVisibleItemPosition: Int =
                        (it as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (firstVisibleItemPosition > 0) {
                        viewModel.getNextPage(
                            visibleItemCount,
                            firstVisibleItemPosition,
                            totalItemCount
                        )
                    }
                }
            }
        }

    fun selectTab(tabPosition: Int) {
        this.startTabPosition = tabPosition
        if (isAdded) {
            setupTabUI(tabPosition == 0)
            if (tabPosition == 0) {
                rv_record_settled.scrollToPosition(0)
            } else {
                rv_record_unsettled.scrollToPosition(0)
            }
        }
    }
}