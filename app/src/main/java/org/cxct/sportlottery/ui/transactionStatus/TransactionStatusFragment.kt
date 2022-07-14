package org.cxct.sportlottery.ui.transactionStatus

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import kotlinx.android.synthetic.main.fragment_transaction_status.*
import kotlinx.android.synthetic.main.view_back_to_top.*
import kotlinx.android.synthetic.main.view_status_selector.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.setTextWithStrokeWidth

/**
 * @app_destination 交易狀況
 */
class TransactionStatusFragment : BaseFragment<TransactionStatusViewModel>(TransactionStatusViewModel::class) {
    private val recordDiffAdapter by lazy { TransactionRecordDiffAdapter() }
    private val nestedScrollViewListener =
        NestedScrollView.OnScrollChangeListener { scrollView, _, scrollY, _, _ ->
            val nestedTopView = scrollView.getChildAt(0)
            val nestedScrollViewHeight = scrollView.height
            if (nestedTopView.height <= scrollY + nestedScrollViewHeight) {
                viewModel.getBetList()
            }

            if (!scrollView.canScrollVertically(-1)) {
                view_back_to_top.visibility = View.GONE
            } else {
                view_back_to_top.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
        initRecyclerView()
        initData()
        initFilter()
        viewModel.getBetList(true)

        // Set title weight
        tv_title.setTextWithStrokeWidth(getString(R.string.label_transaction_status), 0.7f)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaction_status, container, false).apply {
            initObserve()
        }
    }

    private fun initRecyclerView() {
        rv_record.apply {
            adapter = recordDiffAdapter
        }
        scroll_view.setOnScrollChangeListener(nestedScrollViewListener)
    }

    private fun initData() {
        viewModel.getSportList()
    }

    private fun initFilter() {
        bet_type_selector.cl_root.layoutParams.height = 40.dp
        game_type_selector.cl_root.layoutParams.height = 40.dp

        bet_type_selector.apply {
            var betTypeStatusSheetData= mutableListOf(StatusSheetData("0,1", context.getString(R.string.label_all)),StatusSheetData("0", context.getString(R.string.waiting)), StatusSheetData("1", context.getString(R.string.not_settled_order)))
            setItemData(betTypeStatusSheetData)
            setSelectCode(betTypeStatusSheetData[0].code)
            itemSelectedListener = { statusSheetData ->
                statusSheetData.code.let { selectedCode ->
                    selectedCode?.let {
                        var list= mutableListOf<Int>()
                        it.split(",").forEach {
                            list.add(it.toInt())
                        }
                        viewModel.statusList = list.toList()
                    }
                }
            }
        }

        game_type_selector.apply {
            itemSelectedListener = { statusSheetData ->
                statusSheetData.code.let { selectedCode ->
                    viewModel.gameType = GameType.values().find { gameType -> gameType.key == selectedCode }?.key
                }
            }
        }
    }

    private fun updateGameTypeSpinnerData(gameTypeSpinnerList: MutableList<StatusSheetData>) {
        with(game_type_selector) {
            setItemData(gameTypeSpinnerList)
            if (gameTypeSpinnerList.isNotEmpty())
                setSelectCode(gameTypeSpinnerList[0].code)
        }
    }

    private val handler by lazy { Handler() }
    private fun initObserve() {
        viewModel.betListData.observe(viewLifecycleOwner) {
            Log.e("Martin", "123321=" + viewModel.statusList)
            recordDiffAdapter.setupBetList(it, viewModel.statusList?.get(0) ?: 0)
            btn_back_to_top.visibility = if (it.row.isEmpty()) View.GONE else View.VISIBLE
            divider.visibility = if (it.row.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.responseFailed.observe(viewLifecycleOwner) {
            if (it == true) {
                handler.postDelayed({
                    viewModel.getBetList(true)
                }, 1000)
            }
        }

        viewModel.sportCodeList.observe(viewLifecycleOwner) {
            updateGameTypeSpinnerData(it.toMutableList())
        }

    }

    private fun initButton() {
        btn_back.setOnClickListener {
            activity?.finish()
        }

        btn_back_to_top.setOnClickListener {
            scroll_view.smoothScrollTo(0, 0)
        }
    }

}