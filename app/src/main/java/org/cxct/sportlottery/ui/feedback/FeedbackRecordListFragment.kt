package org.cxct.sportlottery.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.archit.calendardaterangepicker.customviews.DateSelectedType
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_info_center.*
import kotlinx.android.synthetic.main.content_rv_bank_list_new.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_calendar.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_rech_list.*
import kotlinx.android.synthetic.main.fragment_bet_record_search.*
import kotlinx.android.synthetic.main.fragment_feedback_record_list.*
import kotlinx.android.synthetic.main.fragment_feedback_record_list.ll_end_date
import kotlinx.android.synthetic.main.fragment_feedback_record_list.ll_start_date
import kotlinx.android.synthetic.main.fragment_feedback_record_list.tv_end_date
import kotlinx.android.synthetic.main.fragment_feedback_record_list.tv_start_date
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.util.TimeUtil
import java.text.SimpleDateFormat
import java.util.*

class FeedbackRecordListFragment : BaseFragment<FeedbackViewModel>(FeedbackViewModel::class) {

    private val navController by lazy {
        view?.findNavController()
    }

    lateinit var calendarBottomSheet: BottomSheetDialog
    lateinit var typeBottomSheet: BottomSheetDialog
    lateinit var statusBottomSheet: BottomSheetDialog

    val adapter by lazy {
        context?.let {
            FeedbackListAdapter(it, FeedbackListAdapter.ItemClickListener { data ->
                navController?.navigate(R.id.action_feedbackRecordListFragment_to_feedbackDetailFragment)
                viewModel.dataID = data.id?.toLong()
                viewModel.feedbackCode = data.feedbackCode
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feedback_record_list, container, false).apply {
            setupStateBottomSheet(container)
            setupTypeBottomSheet(container)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initView()
        initButton()
        initDataLive()
        initData()
        initBottomSheetDialog()
    }

    private fun initData() {
        getList()
    }

    private fun initRecyclerView() {
        rv_pay_type.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rv_pay_type.adapter = adapter

        rv_pay_type.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.getFbQueryList(false, adapter?.itemCount ?: 0)
                }
            }
        })
    }

    private fun initDataLive() {
        viewModel.feedbackList.observe(this.viewLifecycleOwner, Observer {
            val listData = it ?: return@Observer
            adapter?.data = listData
        })
    }

    private fun initView() {
        btn_sugession.isSelected = false
        btn_record.isSelected = true
    }

    private fun initButton() {
        btn_sugession.setOnClickListener {
            navController?.navigate(R.id.action_feedbackRecordListFragment_to_feedbackSuggestFragment)
        }
        btn_submit.setOnClickListener {
            getList()
        }

        ll_start_date.setOnClickListener {
            calendarBottomSheet.tv_calendar_title.text = getString(R.string.start_date)
            calendarBottomSheet.calendar.setDateSelectedType(DateSelectedType.START)
            calendarBottomSheet.show()
        }
        ll_end_date.setOnClickListener {
            calendarBottomSheet.tv_calendar_title.text = getString(R.string.end_date)
            calendarBottomSheet.calendar.setDateSelectedType(DateSelectedType.END)
            calendarBottomSheet.show()
        }

        ll_type.setOnClickListener {
            typeBottomSheet.show()
        }
        ll_status.setOnClickListener {
            statusBottomSheet.show()
        }
    }

    private fun initBottomSheetDialog() {
        calendarBottomSheet()
    }

    //日期的BottomSheet
    private fun calendarBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_calendar, null)
        calendarBottomSheet = BottomSheetDialog(this.requireContext())
        calendarBottomSheet.setContentView(bottomSheetView)
        calendarBottomSheet.calendar.setSelectableDateRange(
            TimeUtil.getDateInCalendar(30).first,
            TimeUtil.getDateInCalendar(30).second
        )
        calendarBottomSheet.calendar.setCalendarListener(object : CalendarListener {
            override fun onFirstDateSelected(
                dateSelectedType: DateSelectedType,
                startDate: Calendar
            ) {
                setStartEndDateText(dateSelectedType,TimeUtil.stampToDateTime(startDate.time), "")
                calendarBottomSheet.dismiss()
            }

            override fun onDateRangeSelected(
                dateSelectedType: DateSelectedType,
                startDate: Calendar,
                endDate: Calendar
            ) {
                setStartEndDateText(dateSelectedType,
                    TimeUtil.stampToDateTime(startDate.time),
                    TimeUtil.stampToDateTime(endDate.time)
                )
                calendarBottomSheet.dismiss()
            }
        })
    }

    private fun setStartEndDateText(dateSelectedType:DateSelectedType,startDate: String, endDate: String) {
        tv_start_date.text = startDate
        tv_end_date.text = endDate

        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            tv_start_date.text = startDate
            tv_end_date.text = endDate
        } else {
            //若只有其中一個日期, 則根據當前點選的是開始或結束日期去做更新文字
            when (dateSelectedType) {
                DateSelectedType.START -> tv_start_date.text = startDate
                DateSelectedType.END -> tv_end_date.text = startDate
            }
        }

        viewModel.feedbackListRequest.startTime = startDate
        viewModel.feedbackListRequest.endTime = endDate
    }

    //類別的BottomSheet
    private fun setupStateBottomSheet(container: ViewGroup?) {
        try {
            val textList = ArrayList(viewModel.statusMap.values)

            val bottomSheetView =
                layoutInflater.inflate(R.layout.dialog_bottom_sheet_rech_list, container, false)
            statusBottomSheet = BottomSheetDialog(this.requireContext())
            statusBottomSheet.setContentView(bottomSheetView)
            statusBottomSheet.aaa.text=""
            statusBottomSheet.rech_list.setOnItemClickListener { _, _, position, _ ->
                viewModel.feedbackListRequest.status = position
                tv_status.text = viewModel.statusMap[position]
                statusBottomSheet.dismiss()
            }
            statusBottomSheet.rech_list.apply {
                adapter = ArrayAdapter(context, R.layout.itemview_simple_list_center, textList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //狀態的BottomSheet
    private fun setupTypeBottomSheet(container: ViewGroup?) {
        try {
            val textList = listOf(viewModel.typeMap.values)

            val bottomSheetView =
                layoutInflater.inflate(R.layout.dialog_bottom_sheet_rech_list, container, false)
            typeBottomSheet = BottomSheetDialog(this.requireContext())
            typeBottomSheet.setContentView(bottomSheetView)
            typeBottomSheet.rech_list.setOnItemClickListener { _, _, position, _ ->
                viewModel.feedbackListRequest.type = position
                tv_type.text = viewModel.typeMap[position]
                typeBottomSheet.dismiss()
            }
            typeBottomSheet.rech_list.apply {
                adapter = ArrayAdapter(context, R.layout.itemview_simple_list_center, textList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun getList() {
        viewModel.getFbQueryList(true, 0)//首次進來跟點選查詢都重新撈資料
    }

}