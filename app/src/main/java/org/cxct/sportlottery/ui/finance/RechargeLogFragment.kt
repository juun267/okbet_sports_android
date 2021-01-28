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
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_recharge_log.*
import kotlinx.android.synthetic.main.activity_recharge_log.view.*
import kotlinx.android.synthetic.main.component_date_range_selector.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_calendar.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_rech_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import java.util.*


class RechargeLogFragment : BaseFragment<FinanceViewModel>(FinanceViewModel::class) {
    private lateinit var calendarBottomSheet: BottomSheetDialog
    private lateinit var rechargeStateBottomSheet: BottomSheetDialog
    private lateinit var rechargeChannelBottomSheet: BottomSheetDialog
    private val logDetailDialog by lazy {
        LogDetailDialog()
    }

    private val rechargeLogAdapter by lazy {
        RechargeLogAdapter().apply {
            rechargeLogListener = RechargeLogListener {
                viewModel.setLogDetail(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_recharge_log, container, false).apply {
            setupCalendarBottomSheet(container)
            setupRechargeStateBottomSheet(container)
            setupRechargeChannelBottomSheet(container)
            setupDateRangeSelector(this)
            setupRechargeStateSelector(this)
            setupRechargeChannelSelector(this)
            setupRechargeLogList(this)
            setupSearch(this)
        }
    }

    private fun setupCalendarBottomSheet(container: ViewGroup?) {
        val bottomSheetView =
            layoutInflater.inflate(R.layout.dialog_bottom_sheet_calendar, container, false)

        calendarBottomSheet = BottomSheetDialog(this.requireContext())
        calendarBottomSheet.setContentView(bottomSheetView)
        calendarBottomSheet.calendar.setCalendarListener(object : CalendarListener {
            override fun onFirstDateSelected(startDate: Calendar) {
                calendarBottomSheet.dismiss()

                viewModel.setRecordTimeRange(startDate)
            }

            override fun onDateRangeSelected(startDate: Calendar, endDate: Calendar) {
                calendarBottomSheet.dismiss()

                viewModel.setRecordTimeRange(startDate, endDate)
            }
        })
    }

    private fun setupRechargeStateBottomSheet(container: ViewGroup?) {
        val bottomSheetView =
            layoutInflater.inflate(R.layout.dialog_bottom_sheet_rech_list, container, false)

        rechargeStateBottomSheet = BottomSheetDialog(this.requireContext())
        rechargeStateBottomSheet.setContentView(bottomSheetView)
        rechargeStateBottomSheet.rech_list.setOnItemClickListener { _, _, position, _ ->
            rechargeStateBottomSheet.dismiss()

            viewModel.setRechargeState(position)
        }
    }

    private fun setupRechargeChannelBottomSheet(container: ViewGroup?) {
        val bottomSheetView =
            layoutInflater.inflate(R.layout.dialog_bottom_sheet_rech_list, container, false)

        rechargeChannelBottomSheet = BottomSheetDialog(this.requireContext())
        rechargeChannelBottomSheet.setContentView(bottomSheetView)
        rechargeChannelBottomSheet.rech_list.setOnItemClickListener { _, _, position, _ ->
            rechargeChannelBottomSheet.dismiss()

            viewModel.setRechargeChannel(position)
        }
    }

    private fun setupDateRangeSelector(view: View) {
        view.date_range_selector.ll_start_date.setOnClickListener {
            calendarBottomSheet.show()
        }
        view.date_range_selector.ll_end_date.setOnClickListener {
            calendarBottomSheet.show()
        }
    }

    private fun setupRechargeStateSelector(view: View) {
        view.order_status_selector.ll_start_date.setOnClickListener {
            rechargeStateBottomSheet.show()
        }
    }

    private fun setupRechargeChannelSelector(view: View) {
        view.order_status_selector.ll_end_date.setOnClickListener {
            rechargeChannelBottomSheet.show()
        }
    }

    private fun setupRechargeLogList(view: View) {
        view.rvlist.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            this.adapter = rechargeLogAdapter

            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun setupSearch(view: View) {
        view.date_range_selector.btn_search.setOnClickListener {
            viewModel.getUserRechargeList()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recordCalendarRange.observe(this.viewLifecycleOwner, Observer {
            calendarBottomSheet.calendar.setSelectableDateRange(it.first, it.second)
        })

        viewModel.rechargeStateList.observe(this.viewLifecycleOwner, Observer {
            val textList = it.map { rechargeState -> rechargeState.state }

            rechargeStateBottomSheet.rech_list.apply {
                adapter = ArrayAdapter(context, R.layout.itemview_simple_list_center, textList)
            }

            order_status_selector.tv_start_date.text = it.find { rechargeState ->
                rechargeState.isSelected
            }?.state
        })

        viewModel.rechargeChannelList.observe(this.viewLifecycleOwner, Observer {
            val textList = it.map { rechargeChannel -> rechargeChannel.channel }

            rechargeChannelBottomSheet.rech_list.apply {
                adapter = ArrayAdapter(context, R.layout.itemview_simple_list_center, textList)
            }

            order_status_selector.tv_end_date.text = it.find { rechargeChannel ->
                rechargeChannel.isSelected
            }?.channel
        })

        viewModel.recordCalendarStartDate.observe(this.viewLifecycleOwner, Observer {
            date_range_selector.tv_start_date.text = it.date
        })

        viewModel.recordCalendarEndDate.observe(this.viewLifecycleOwner, Observer {
            date_range_selector.tv_end_date.text = it.date
        })

        viewModel.userRechargeListResult.observe(this.viewLifecycleOwner, Observer {
            if (it?.success == true) {
                rechargeLogAdapter.data = it.rows ?: listOf()
            }
        })

        viewModel.logDetail.observe(this.viewLifecycleOwner, Observer {
            if (logDetailDialog.dialog?.isShowing != true) {

                logDetailDialog.show(
                    parentFragmentManager,
                    RechargeLogFragment::class.java.simpleName
                )
            }
        })

        viewModel.getCalendarRange()
        viewModel.getRechargeState()
        viewModel.getRechargeChannel()
        viewModel.getUserRechargeList()
    }
}