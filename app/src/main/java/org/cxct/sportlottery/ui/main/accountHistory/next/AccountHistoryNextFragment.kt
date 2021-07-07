package org.cxct.sportlottery.ui.main.accountHistory.next

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_account_history_next.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.component.StatusSheetData

class AccountHistoryNextFragment : BaseFragment<AccountHistoryNextViewModel>(AccountHistoryNextViewModel::class) {

    private val args: AccountHistoryNextFragmentArgs by navArgs()

    private val dateStatusList by lazy {
        listOf(StatusSheetData("0", context?.getString(R.string.sunday)),
               StatusSheetData("1", context?.getString(R.string.monday)),
               StatusSheetData("2", context?.getString(R.string.tuesday)),
               StatusSheetData("3", context?.getString(R.string.wednesday)),
               StatusSheetData("4", context?.getString(R.string.thursday)),
               StatusSheetData("5", context?.getString(R.string.friday)),
               StatusSheetData("6", context?.getString(R.string.saturday)))
    }

    private val sportStatusList by lazy {
        listOf(StatusSheetData("", context?.getString(R.string.all_sport)),
               StatusSheetData("FT", context?.getString(R.string.soccer)),
               StatusSheetData("BK", context?.getString(R.string.basketball)),
               StatusSheetData("TN", context?.getString(R.string.tennis)),
               StatusSheetData("VB", context?.getString(R.string.volleyball)),
               StatusSheetData("BM", context?.getString(R.string.badminton)))
    }

    private val rvAdapter = AccountHistoryNextAdapter(ItemClickListener {
        it.let { data ->

        }
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_history_next, container, false).apply {
            viewModel.searchBetRecord()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRv()
        initOnclick()
        initObserver()
    }

    private fun initView() {
        sport_selector.setCloseBtnText(getString(R.string.bottom_sheet_close))
        sport_selector.dataList = sportStatusList

        date_selector.setCloseBtnText(getString(R.string.bottom_sheet_close))
        date_selector.dataList = dateStatusList
    }

    private fun initOnclick() {

        btn_back_to_top.setOnClickListener {
            rv_account_history.smoothScrollToPosition(0)
        }

    }

    private fun initObserver() {

        viewModel.loading.observe(viewLifecycleOwner, {
            if (it) loading() else hideLoading()
        })

        viewModel.betRecordResult.observe(viewLifecycleOwner, {
            if (it.success) {
                rvAdapter.addFooterAndSubmitList(viewModel.recordDataList, viewModel.isLastPage)
            } else {
                Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.oddsType.observe(viewLifecycleOwner, {
            rvAdapter.oddsType = it
        })

    }

    private fun initRv() {
        rv_account_history.apply {
            adapter = rvAdapter
//            addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.divider_gray)))
        }

    }

}

