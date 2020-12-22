package org.cxct.sportlottery.ui.bet_record.search

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.archit.calendardaterangepicker.customviews.CalendarListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_bottom_sheet_bet_status.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_calendar.*
import kotlinx.android.synthetic.main.fragment_bet_record_search.*
import kotlinx.android.synthetic.main.item_listview_bet_type.view.*
import kotlinx.android.synthetic.main.item_listview_bet_type_all.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetRecordSearchBinding
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.common.IdParams
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams
import org.cxct.sportlottery.ui.bet_record.BetRecordViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*


class BetRecordSearchFragment : Fragment() {

    private val betRecordViewModel: BetRecordViewModel by viewModel()

    lateinit var calendarBottomSheet: BottomSheetDialog
    lateinit var betStatusBottomSheet: BottomSheetDialog
    private lateinit var betStatusLvAdapter: BetStatusLvAdapter

    private val betStatusList = mutableListOf<BetTypeItemData>()
    private var simpleDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: FragmentBetRecordSearchBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_bet_record_search, container, false)
        binding.apply {
            betRecordViewModel = this@BetRecordSearchFragment.betRecordViewModel
            lifecycleOwner = this@BetRecordSearchFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBottomSheetDialog()
        initListView()
        setObserver()
        setOnClick()
    }

    private fun initBottomSheetDialog() {
        betStatusBottomSheet()
        calendarBottomSheet()
    }

    private fun betStatusBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_bet_status, null)
        betStatusBottomSheet = BottomSheetDialog(this.requireContext())
        betStatusBottomSheet.setContentView(bottomSheetView)

        //避免bottomSheet的滑動與listView發生衝突
        betStatusBottomSheet.behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    betStatusBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun calendarBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_calendar, null)
        calendarBottomSheet = BottomSheetDialog(this.requireContext())
        calendarBottomSheet.setContentView(bottomSheetView)
        calendarBottomSheet.calendar.setCalendarListener(object : CalendarListener {
            override fun onFirstDateSelected(startDate: Calendar) {
                setStartEndDateText(simpleDateFormat.format(startDate.time), "")
                calendarBottomSheet.dismiss()
            }

            override fun onDateRangeSelected(startDate: Calendar, endDate: Calendar) {
                setStartEndDateText(simpleDateFormat.format(startDate.time), simpleDateFormat.format(endDate.time))
                calendarBottomSheet.dismiss()
            }

        })
    }

    private fun setStartEndDateText(startDate: String, endDate: String) {
        tv_start_date.text = startDate
        tv_end_date.text = endDate
    }

    private fun getDateCalendar(minusDays: Int? = null): Pair<Calendar, Calendar> { //<startDate, EndDate>
        val todayCalendar = Calendar.getInstance()
        val minusDaysCalendar = Calendar.getInstance()
        if (minusDays != null) minusDaysCalendar.add(Calendar.DATE, -minusDays)
        return Pair<Calendar, Calendar>(minusDaysCalendar, todayCalendar)

    }


    private fun Map<Int, String>.addValueToArray(array: MutableList<BetTypeItemData>) =
        this.forEach {
            array.add(BetTypeItemData(it.key, it.value, false))
        }

    private fun initListView() {
        betRecordViewModel.statusNameMap.addValueToArray(betStatusList)

        val selectNameList = mutableListOf<String>()
        betStatusLvAdapter = BetStatusLvAdapter(betStatusBottomSheet.lv_bet_type.context, betStatusList)
        betStatusBottomSheet.lv_bet_type.adapter = betStatusLvAdapter

        //item selected
        val cbAll = betStatusBottomSheet.layout_all.checkbox_select_all
        betStatusLvAdapter.setOnItemCheckedListener(object : OnSelectItemListener<BetTypeItemData> {
            override fun onClick(select: BetTypeItemData) {
                if (select.isSelected) {
                    selectNameList.add(select.name)
                } else {
                    selectNameList.remove(select.name)
                }

                //判斷全選按鈕是否需選取
                var selectCount = 0
                betStatusList.forEach {
                    if (it.isSelected) selectCount++
                }
                cbAll.isChecked = selectCount == betStatusList.size
                tv_bet_status.text = selectNameList.joinToString(",")
            }

        })

        //全選按鈕
        cbAll.setOnClickListener {
            selectNameList.clear()
            betStatusList.forEach {
                it.isSelected = cbAll.isChecked

                if (it.isSelected) {
                    selectNameList.add(it.name)
                } else {
                    selectNameList.remove(it.name)
                }
            }
            betStatusLvAdapter.notifyDataSetChanged()
            tv_bet_status.text = selectNameList.joinToString(",")
        }

        //取消選擇
        val tvCancel = betStatusBottomSheet.layout_all.tv_cancel_selections
        tvCancel.setOnClickListener {
            selectNameList.clear()
            cbAll.isChecked = false

            betStatusList.forEach {
                it.isSelected = false
            }
            betStatusLvAdapter.notifyDataSetChanged()
            tv_bet_status.text = selectNameList.joinToString(",")
        }

    }

    private fun setObserver() {

        betRecordViewModel.betRecordResult.observe(viewLifecycleOwner, {
            if (it.success) {
                view?.findNavController()?.navigate(BetRecordSearchFragmentDirections.actionBetRecordSearchFragmentToBetRecordResultFragment())
            } else {
                Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
            }
        })

        /*
        betRecordViewModel.betListRequest.observe(viewLifecycleOwner, {
            val request = it ?: return@observe
            //status
            var statusStr = ""
            it.statusList?.forEach { statusCode ->
                statusStr += betRecordViewModel.statusNameMap[statusCode]
            }
            if (statusStr.isNotEmpty()) tv_bet_status.text = statusStr

            //startTime
            val startTime = request.timeRangeParams?.startTime
            if (startTime != null) tv_start_date.text = timeStampToDate(startTime)

            //endTime
            val endTime = it.timeRangeParams?.endTime
            if (endTime != null) tv_end_date.text = timeStampToDate(endTime)
        })
        */
    }

    private fun timeStampToDate(time: Long?): String? {
        if (time == null) return null
        return simpleDateFormat.format(time)
    }

    private fun setOnClick() {
        ll_bet_status.setOnClickListener {
            betStatusBottomSheet.show()
        }

        ll_start_date.setOnClickListener {
            calendarBottomSheet.tv_calendar_title.text = getString(R.string.start_date)
            calendarBottomSheet.show()
        }

        ll_end_date.setOnClickListener {
            calendarBottomSheet.tv_calendar_title.text = getString(R.string.end_date)
            calendarBottomSheet.show()
        }

        btn_today.setOnClickListener {
            setCalendarDate(getDateCalendar())
        }

        btn_yesterday.setOnClickListener {
            setCalendarDate(getDateCalendar(1))
        }

        btn_past30days.setOnClickListener {
            setCalendarDate(getDateCalendar(30))
        }

        btn_search.setOnClickListener {
            val statusList = mutableListOf<Int>()
            betStatusList.filter { it.isSelected }.forEach {
                if (it.code != null) statusList.add(it.code)
            }

            val startTimeStamp = betRecordViewModel.dateToTimeStamp(tv_start_date.text.toString(), true)
            val endTimeStamp = betRecordViewModel.dateToTimeStamp(tv_start_date.text.toString(), false)
            val betListRequest = BetListRequest(statusList = statusList, startTime = startTimeStamp.toString(), endTime = endTimeStamp.toString())
            betRecordViewModel.searchBetRecordHistory(betListRequest)
        }
    }

    private fun setCalendarDate(datePair: Pair<Calendar, Calendar>) { //<startDate, endDate>
        val startDate = datePair.first
        val endDate = datePair.second
        calendarBottomSheet.calendar.setSelectedDateRange(startDate, endDate)

        val startDateStr = simpleDateFormat.format(startDate.time)
        val endDateStr = simpleDateFormat.format(endDate.time)

        setStartEndDateText(startDateStr, endDateStr)
    }

}

data class BetTypeItemData(val code: Int? = null, val name: String = "", var isSelected: Boolean = false)

class BetStatusLvAdapter(private val context: Context, private val dataList: MutableList<BetTypeItemData>) : BaseAdapter() {

    private var mOnSelectItemListener: OnSelectItemListener<BetTypeItemData>? = null

    fun setOnItemCheckedListener(onSelectItemListener: OnSelectItemListener<BetTypeItemData>) {
        this.mOnSelectItemListener = onSelectItemListener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_listview_bet_type, parent, false)
        val data = dataList[position]

        view.apply {
            checkbox.text = data.name
            checkbox.isChecked = data.isSelected
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                data.isSelected = isChecked
                linear_layout.isSelected = isChecked
                notifyDataSetChanged()
                mOnSelectItemListener?.onClick(data)
            }
        }

        return view
    }

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

}