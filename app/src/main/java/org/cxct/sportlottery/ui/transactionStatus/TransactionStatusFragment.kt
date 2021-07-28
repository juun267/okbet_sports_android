package org.cxct.sportlottery.ui.transactionStatus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import kotlinx.android.synthetic.main.fragment_transaction_status.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.common.StatusSheetData

class TransactionStatusFragment : BaseFragment<TransactionStatusViewModel>(TransactionStatusViewModel::class) {
    private val recordDiffAdapter by lazy { TransactionRecordDiffAdapter() }
    private val nestedScrollViewListener =
        NestedScrollView.OnScrollChangeListener { scrollView, _, scrollY, _, _ ->
            val nestedTopView = scrollView.getChildAt(0)
            val nestedScrollViewHeight = scrollView.height
            if (nestedTopView.height <= scrollY + nestedScrollViewHeight) {
                viewModel.getBetList()
            }
        }

    interface BottomNavigationListener {
        fun onSportHomeNav()
    }

    private var bottomNavigationListener: BottomNavigationListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initObserve()
        getBetListData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
        initRecyclerView()
        initFilter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaction_status, container, false)
    }

    private fun initRecyclerView() {
        rv_record.apply {
            adapter = recordDiffAdapter
        }
        scroll_view.setOnScrollChangeListener(nestedScrollViewListener)
    }

    private fun initFilter() {
        val gameTypeStatusSheetData =
            mutableListOf<StatusSheetData>().apply {
                add(StatusSheetData(null, getString(R.string.all_sport)))
                GameType.values().toList().forEach { gameType ->
                    add(StatusSheetData(gameType.key, getString(gameType.string)))
                }
            }
        game_type_selector.apply {
            dataList = gameTypeStatusSheetData
            itemSelectedListener = { statusSheetData ->
                statusSheetData.code.let { selectedCode ->
                    viewModel.gameType = GameType.values().find { gameType -> gameType.key == selectedCode }?.key
                }
            }
        }
    }

    private fun initObserve() {
        viewModel.betListData.observe(this, {
            recordDiffAdapter.setupBetList(it)
        })
    }

    fun setBottomNavigationListener(listener: BottomNavigationListener) {
        bottomNavigationListener = listener
    }

    private fun initButton() {
        btn_back.setOnClickListener {
            bottomNavigationListener?.onSportHomeNav()
        }
    }

    private fun getBetListData() {
        viewModel.getBetList(true)
    }
}