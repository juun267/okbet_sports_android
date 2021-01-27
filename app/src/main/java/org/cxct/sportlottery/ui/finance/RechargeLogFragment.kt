package org.cxct.sportlottery.ui.finance

import android.os.Bundle
import androidx.fragment.app.Fragment
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
import kotlinx.android.synthetic.main.dialog_bottom_sheet_rech_state.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RechargeLogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RechargeLogFragment : BaseFragment<FinanceViewModel>(FinanceViewModel::class) {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var calendarBottomSheet: BottomSheetDialog
    private lateinit var rechargeStateBottomSheet: BottomSheetDialog

    private val rechargeLogAdapter by lazy {
        RechargeLogAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_recharge_log, container, false).apply {
            setupCalendarBottomSheet(container, this)
            setupRechargeStateBottomSheet(container, this)
            setupDateRangeSelector(this)
            setupRechargeStateSelector(this)
            setupRechargeLogList(this)
        }
    }

    private fun setupCalendarBottomSheet(container: ViewGroup?, view: View) {
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

    private fun setupDateRangeSelector(view: View) {
        view.date_range_selector.ll_start_date.setOnClickListener {
            calendarBottomSheet.show()
        }
        view.date_range_selector.ll_end_date.setOnClickListener {
            calendarBottomSheet.show()
        }
    }

    private fun setupRechargeStateBottomSheet(container: ViewGroup?, view: View) {
        val bottomSheetView =
            layoutInflater.inflate(R.layout.dialog_bottom_sheet_rech_state, container, false)

        rechargeStateBottomSheet = BottomSheetDialog(this.requireContext())
        rechargeStateBottomSheet.setContentView(bottomSheetView)
        rechargeStateBottomSheet.rech_state_list.setOnItemClickListener { _, _, position, _ ->
            rechargeStateBottomSheet.dismiss()

            viewModel.setRechargeState(position)
        }
    }

    private fun setupRechargeStateSelector(view: View) {
        view.order_status_selector.ll_start_date.setOnClickListener {
            rechargeStateBottomSheet.show()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recordCalendarRange.observe(this.viewLifecycleOwner, Observer {
            calendarBottomSheet.calendar.setSelectableDateRange(it.first, it.second)
        })

        viewModel.recordCalendarStartDate.observe(this.viewLifecycleOwner, Observer {
            date_range_selector.tv_start_date.text = it
        })

        viewModel.recordCalendarEndDate.observe(this.viewLifecycleOwner, Observer {
            date_range_selector.tv_end_date.text = it
        })

        viewModel.rechargeStateList.observe(this.viewLifecycleOwner, Observer {
            rechargeStateBottomSheet.rech_state_list.apply {
                adapter = ArrayAdapter(context, R.layout.itemview_simple_list_center, it)
            }
        })

        viewModel.rechargeState.observe(this.viewLifecycleOwner, Observer {
            order_status_selector.tv_start_date.text = it
        })

        viewModel.userRechargeListResult.observe(this.viewLifecycleOwner, Observer {
            if (it?.success == true) {
                rechargeLogAdapter.data = it.rows ?: listOf()
            }
        })

        viewModel.userRechargeFilterList.observe(this.viewLifecycleOwner, Observer {
            rechargeLogAdapter.data = it
        })

        viewModel.getCalendarRange()
        viewModel.getRechargeState()
        viewModel.getUserRechargeList()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RechargeLogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RechargeLogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}