package org.cxct.sportlottery.ui.bet_record.search

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_bet_record_search.*
import kotlinx.android.synthetic.main.spinner_item_bet.view.*
import kotlinx.android.synthetic.main.spinner_item_bet_first.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetRecordSearchBinding
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.ui.bet_record.BetRecordViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class BetRecordSearchFragment : Fragment() {

    private val betRecordViewModel: BetRecordViewModel by viewModel()

    private var mCalendarDialog: BottomSheetDialog? = null
    private var mEndDateCalendarDialog: BottomSheetDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentBetRecordSearchBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bet_record_search, container, false)
        binding.apply {
            betRecordViewModel = this@BetRecordSearchFragment.betRecordViewModel
            lifecycleOwner = this@BetRecordSearchFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBottomSheetDialog()
        initSpinner()
        setObserver()
        setOnClick()
    }


    private fun initBottomSheetDialog() {
        val bottomSheet = layoutInflater.inflate(R.layout.dialog_bottom_sheet_calendar, null)
        val dialog = BottomSheetDialog(this.requireContext())
        dialog.setContentView(bottomSheet)
        bottomSheet.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        /*
        mCalendarDialog?.calendarView?.setOnDayClickListener { eventDay ->
            mStartDate = TimeUtil.timeFormat(eventDay.calendar.timeInMillis, "yyyy-MM-dd")
            tv_start_date.text = mStartDate

            endMinDate.set(Calendar.YEAR, eventDay.calendar.get(Calendar.YEAR))
            endMinDate.set(Calendar.MONTH, eventDay.calendar.get(Calendar.MONTH))
            endMinDate.set(Calendar.DAY_OF_MONTH, eventDay.calendar.get(Calendar.DAY_OF_MONTH) - 1)
            startDate = Date(TimeUtil.timeFormatToLong(mStartDate, "yyyy-MM-dd"))
            if (endDate.before(startDate)) {
                mEndDate = mStartDate
                tv_end_date.text = mEndDate
                mEndDateCalendarDialog?.calendarView?.setDate(startDate) //預設選擇的日期
            }
            mCalendarDialog?.dismiss()
        }

        startDate = Date(TimeUtil.timeFormatToLong(mStartDate, "yyyy-MM-dd"))
        mCalendarDialog?.calendarView?.setDate(startDate) //預設選擇的日期
        mCalendarDialog?.show()
        */
    }

//    private val statusArr = (fun(list) -> betRecordViewModel.statusNameMap.forEach {
//        list += it.value
//    })

    private val statusList = mutableListOf<SpinnerData>()

    private fun Map<Int, String>.addValueToArray(array: MutableList<SpinnerData>) = this.forEach {
        array.add(SpinnerData(it.key, it.value, false))
    }

    private fun initSpinner() {
        statusList.add(SpinnerData(-1, context?.getString(R.string.select_all)?:"全选", false))
        betRecordViewModel.statusNameMap.addValueToArray(statusList)
/*

        val selectNameList = mutableListOf<String>()
        val spinnerAdapter = BetSpinnerAdapter(spinner_bet_state.context, statusList)
        spinnerAdapter.setOnItemCheckedListener(object : OnSelectItemListener<SpinnerData> {
            override fun onClick(select: SpinnerData) {
                if (select.code != -1) { //"全選"選項不顯示
                    if (select.isSelected) {
                        selectNameList.add(select.name)
                    } else {
                        selectNameList.remove(select.name)
                    }
                    tv_bet_status.text = selectNameList.joinToString(",")
                }
            }

        })
        spinner_bet_state.adapter = spinnerAdapter
*/

    }

    private fun setObserver() {
        betRecordViewModel.betListRequest.observe(viewLifecycleOwner, {
            val request = it?: return@observe
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
    }

    private fun timeStampToDate(time: Long?): String? {
        if (time==null) return null
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(time)
    }

    private fun setOnClick() {
        ll_bet_status.setOnClickListener  {
//            view?.findNavController()?.navigate(BetRecordSearchFragmentDirections.actionBetRecordSearchFragmentToBetRecordResultFragment("hello123"))
//            spinner_bet_state.performClick()
        }
    }

}

data class SpinnerData(
    val code: Int? = null,
    val name: String = "",
    var isSelected: Boolean = false
)

class BetSpinnerAdapter(private val context: Context, private val dataList: MutableList<SpinnerData>): BaseAdapter() {

//    var selectList = mutableListOf<Int>()
//    var isAllSelect = false
    var viewType = ViewType.FIRST
    private var mOnSelectItemListener: OnSelectItemListener<SpinnerData>? = null

    enum class ViewType {
        FIRST, OTHERS
    }

    fun setOnItemCheckedListener(onSelectItemListener: OnSelectItemListener<SpinnerData>) {
        this.mOnSelectItemListener = onSelectItemListener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        when (position) {
            0 -> {
                view = LayoutInflater.from(context).inflate(R.layout.spinner_item_bet_first, parent, false)
                viewType = ViewType.FIRST
            }
            else -> {
                view = LayoutInflater.from(context).inflate(R.layout.spinner_item_bet, parent,false)
                viewType = ViewType.OTHERS
            }
        }

        val data = dataList[position]

        when (viewType) {
            ViewType.FIRST -> {
                view.apply {
                    checkbox_first.text = data.name
                    checkbox_first.isChecked = data.isSelected
                    checkbox_first.setOnCheckedChangeListener { _, isChecked ->
                        dataList.forEach {
                            it.isSelected = isChecked
                            mOnSelectItemListener?.onClick(it)
                        }
                        notifyDataSetChanged()
                    }
                }
            }

            ViewType.OTHERS -> {
                view.apply {
                    checkbox.text = data.name
                    checkbox.isChecked = data.isSelected
                    checkbox.setOnCheckedChangeListener { _, isChecked ->
                        data.isSelected = isChecked

                        if (!isChecked && dataList.first().isSelected) {
                            dataList.first().isSelected = false
                            notifyDataSetChanged()
                        }
                        mOnSelectItemListener?.onClick(data)

                    }
                }
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