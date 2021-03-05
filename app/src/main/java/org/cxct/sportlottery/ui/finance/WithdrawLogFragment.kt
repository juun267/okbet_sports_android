package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.archit.calendardaterangepicker.customviews.DateSelectedType
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_recharge_log.*
import kotlinx.android.synthetic.main.activity_recharge_log.view.*
import kotlinx.android.synthetic.main.component_date_range_selector.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_calendar.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_rech_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import java.util.*


class WithdrawLogFragment : BaseFragment<FinanceViewModel>(FinanceViewModel::class) {
    private lateinit var withdrawStateBottomSheet: BottomSheetDialog
    private lateinit var withdrawTypeBottomSheet: BottomSheetDialog
    private val logDetailDialog by lazy {
        WithdrawLogDetailDialog()
    }

    private val withdrawLogAdapter by lazy {
        WithdrawLogAdapter().apply {
            withdrawLogListener = WithdrawLogListener {
                viewModel.setWithdrawLogDetail(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_recharge_log, container, false).apply {
            setupWithdrawStateBottomSheet(container)
            setupWithdrawTypeBottomSheet(container)
//            setupWithdrawStateSelector(this)
//            setupWithdrawTypeSelector(this)
            setupListColumn(this)
            setupWithdrawLogList(this)
            setupSwipeRefreshLayout(this)
            setupSearch(this)
            initOnclick(this)
        }
    }

    private fun initOnclick(view: View?) {
    }


    private fun setupWithdrawStateBottomSheet(container: ViewGroup?) {
        val bottomSheetView =
            layoutInflater.inflate(R.layout.dialog_bottom_sheet_rech_list, container, false)

        withdrawStateBottomSheet = BottomSheetDialog(this.requireContext())
        withdrawStateBottomSheet.setContentView(bottomSheetView)
        withdrawStateBottomSheet.rech_list.setOnItemClickListener { _, _, position, _ ->
            withdrawStateBottomSheet.dismiss()

            viewModel.setWithdrawState(position)
        }
    }

    private fun setupWithdrawTypeBottomSheet(container: ViewGroup?) {
        val bottomSheetView =
            layoutInflater.inflate(R.layout.dialog_bottom_sheet_rech_list, container, false)

        withdrawTypeBottomSheet = BottomSheetDialog(this.requireContext())
        withdrawTypeBottomSheet.setContentView(bottomSheetView)
        withdrawTypeBottomSheet.rech_list.setOnItemClickListener { _, _, position, _ ->
            withdrawTypeBottomSheet.dismiss()

            viewModel.setWithdrawType(position)
        }
    }


/*

    private fun setupWithdrawStateSelector(view: View) {
        view.order_status_selector.ll_start_date.setOnClickListener {
            withdrawStateBottomSheet.show()
        }
    }

    private fun setupWithdrawTypeSelector(view: View) {
        view.order_status_selector.ll_end_date.setOnClickListener {
            withdrawTypeBottomSheet.show()
        }
    }
*/

    private fun setupListColumn(view: View) {
        view.rech_log_recharge_amount.text = getString(R.string.withdraw_log_withdraw_amount)
    }

    private fun setupWithdrawLogList(view: View) {
        view.rvlist.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            this.adapter = withdrawLogAdapter

            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun setupSwipeRefreshLayout(view: View) {
        view.list_swipe_refresh_layout.apply {
            setOnRefreshListener {
                viewModel.getUserWithdrawList(false)
                this.isRefreshing = false
            }
        }
    }

    private fun setupSearch(view: View) {
        view.date_range_selector.btn_search.setOnClickListener {
            viewModel.getUserWithdrawList(true)
            loading()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.withdrawStateList.observe(this.viewLifecycleOwner, Observer {
            val textList = it.map { withdrawState -> withdrawState.state }

            withdrawStateBottomSheet.rech_list.apply {
                adapter = ArrayAdapter(context, R.layout.itemview_simple_list_center, textList)
            }

//            selector_order_status.setAdapter()
/*
            order_status_selector.tv_start_date.text = it.find { withdrawState ->
                withdrawState.isSelected
            }?.state
            */
        })

        viewModel.withdrawTypeList.observe(this.viewLifecycleOwner, Observer {
            val textList = it.map { withdrawType -> withdrawType.channel }

            withdrawTypeBottomSheet.rech_list.apply {
                adapter = ArrayAdapter(context, R.layout.itemview_simple_list_center, textList)
            }
/*
            order_status_selector.tv_end_date.text = it.find { withdrawType ->
                withdrawType.isSelected
            }?.channel
            */
        })

        viewModel.userWithdrawListResult.observe(this.viewLifecycleOwner, Observer {
            if (it?.success == true) {
                val list = it.rows ?: listOf()

                withdrawLogAdapter.data = list
                setupNoRecordView(list.isEmpty())
            }

            hideLoading()
        })

        viewModel.withdrawLogDetail.observe(this.viewLifecycleOwner, Observer {
            if (logDetailDialog.dialog?.isShowing != true) {

                logDetailDialog.show(
                    parentFragmentManager,
                    WithdrawLogFragment::class.java.simpleName
                )
            }
        })

        viewModel.isFinalPage.observe(this.viewLifecycleOwner, Observer {
            withdrawLogAdapter.isFinalPage = it
        })

        viewModel.getWithdrawState()
        viewModel.getWithdrawType()
        viewModel.getUserWithdrawList(true)
        loading()
    }

    private fun setupNoRecordView(visible: Boolean) {
        if (visible) {
            list_swipe_refresh_layout.visibility = View.GONE
            view_no_record.visibility = View.VISIBLE
        } else {
            list_swipe_refresh_layout.visibility = View.VISIBLE
            view_no_record.visibility = View.GONE
        }
    }
}