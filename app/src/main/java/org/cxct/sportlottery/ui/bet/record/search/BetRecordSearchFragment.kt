package org.cxct.sportlottery.ui.bet.record.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
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
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.ui.bet.record.BetRecordViewModel
import org.cxct.sportlottery.ui.bet.record.statusNameMap
import java.text.SimpleDateFormat
import java.util.*

var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
fun timeStampToDate(time: Long?): String? {
    if (time == null) return null
    return formatter.format(time)
}

class BetRecordSearchFragment : BaseFragment<BetRecordViewModel>(BetRecordViewModel::class) {

    lateinit var calendarBottomSheet: BottomSheetDialog
    lateinit var betStatusBottomSheet: BottomSheetDialog
    private lateinit var betStatusLvAdapter: BetStatusLvAdapter

    private var betStatusList = listOf<BetTypeItemData>()
    private var simpleDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: FragmentBetRecordSearchBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_bet_record_search, container, false)
        binding.apply {
            betRecordViewModel = this@BetRecordSearchFragment.viewModel
            lifecycleOwner = this@BetRecordSearchFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBottomSheetDialog()
        initListView()
        setOnClick()
        setObserver()
    }

    private fun setObserver() {
        viewModel.selectedBetStatus.observe(viewLifecycleOwner, {
            tv_bet_status.text = it
        })
    }

    private fun initBottomSheetDialog() {
        betStatusBottomSheet()
        calendarBottomSheet()
    }

    private fun betStatusBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_bottom_sheet_bet_status, null)
        betStatusBottomSheet = BottomSheetDialog(requireContext())
        betStatusBottomSheet.setContentView(bottomSheetView)
        viewModel.clearStatusList()
        //避免bottomSheet與listView的滑動發生衝突
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
        calendarBottomSheet.calendar.setSelectableDateRange(getDateInCalendar(30).first, getDateInCalendar(30).second)
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

    private fun getDateInCalendar(minusDays: Int? = 0): Pair<Calendar, Calendar> { //<startDate, EndDate>
        val todayCalendar = TimeUtil.getTodayEndTimeCalendar()
        val minusDaysCalendar = TimeUtil.getTodayStartTimeCalendar()
        if (minusDays != null) minusDaysCalendar.add(Calendar.DATE, -minusDays)
        return Pair(minusDaysCalendar, todayCalendar)
    }

    private fun getYesterdayDateInCalendar(): Pair<Calendar, Calendar> {
        val yesterdayStartCalendar = TimeUtil.getTodayStartTimeCalendar().apply { add(Calendar.DATE, -1) }
        val yesterdayEndCalendar = TimeUtil.getTodayEndTimeCalendar().apply { add(Calendar.DATE, -1) }
        return Pair(yesterdayStartCalendar, yesterdayEndCalendar)
    }

    private fun initListView() {
        betStatusList = statusNameMap.map {
            BetTypeItemData(it.key, it.value, false)
        }

        betStatusLvAdapter = BetStatusLvAdapter(betStatusBottomSheet.lv_bet_type.context, betStatusList)
        betStatusBottomSheet.lv_bet_type.adapter = betStatusLvAdapter

        //item selected
        val cbAll = betStatusBottomSheet.layout_all.checkbox_select_all
        betStatusLvAdapter.setOnItemCheckedListener(object : OnSelectItemListener<BetTypeItemData> {
            override fun onClick(select: BetTypeItemData) {
                addOrDelete(select)
                //判斷全選按鈕是否需選取
                var selectCount = 0
                betStatusList.forEach {
                    if (it.isSelected) selectCount++
                }
                cbAll.isChecked = selectCount == betStatusList.size
            }
        })

        //全選按鈕
        cbAll.setOnClickListener {
            viewModel.clearStatusList()
            betStatusList.forEach {
                it.isSelected = cbAll.isChecked
                addOrDelete(it)
            }
            betStatusLvAdapter.notifyDataSetChanged()
        }

        //取消選擇
        val tvCancel = betStatusBottomSheet.layout_all.tv_cancel_selections
        tvCancel.setOnClickListener {
            viewModel.clearStatusList()
            cbAll.isChecked = false

            betStatusList.forEach {
                it.isSelected = false
                addOrDelete(it)
            }

            betStatusLvAdapter.notifyDataSetChanged()
        }

    }

    private fun addOrDelete(item: BetTypeItemData) {
        if (item.isSelected) {
            viewModel.addSelectStatus(item)
        } else {
            viewModel.deleteSelectStatus(item)
        }
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
            setCalendarDate(getDateInCalendar())
        }

        btn_yesterday.setOnClickListener {
            setCalendarDate(getYesterdayDateInCalendar())
        }

        btn_past30days.setOnClickListener {
            setCalendarDate(getDateInCalendar(30))
        }

        btn_search.setOnClickListener {

            val statusList = betStatusList.filter { it.isSelected && (it.code != null) }.map { it.code!! }

            viewModel.checkRequestState(tv_start_date.text.toString(), tv_end_date.text.toString())

            viewModel.betListRequestState.observe(viewLifecycleOwner, {
                if (!it.hasStatus) tv_bet_status.setHintTextColor(ContextCompat.getColor(tv_bet_status.context, R.color.red))
                if (!it.hasStartDate) tv_start_date.setHintTextColor(ContextCompat.getColor(tv_bet_status.context, R.color.red))
                if (!it.hasEndDate) tv_end_date.setHintTextColor(ContextCompat.getColor(tv_bet_status.context, R.color.red))

                if (it.hasStatus && it.hasStartDate && it.hasEndDate) {
                    loading()
                    viewModel.getBetList(btn_champion.isChecked, statusList, tv_start_date.text.toString(), tv_end_date.text.toString())
                }
            })

            viewModel.betRecordResult.observe(viewLifecycleOwner, {
                hideLoading()
                if (it.success) {
                    view?.findNavController()?.navigate(BetRecordSearchFragmentDirections.actionBetRecordSearchFragmentToBetRecordResultFragment())
                } else {
                    Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
                }
            })

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

class BetStatusLvAdapter(private val context: Context, private val dataList: List<BetTypeItemData>) : BaseAdapter() {

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

            if (data.isSelected) linear_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.blue2))
            else linear_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.white))

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                data.isSelected = isChecked
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