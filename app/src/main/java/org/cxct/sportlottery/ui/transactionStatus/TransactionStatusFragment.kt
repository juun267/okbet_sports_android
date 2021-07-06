package org.cxct.sportlottery.ui.transactionStatus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import kotlinx.android.synthetic.main.fragment_transaction_status.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment

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