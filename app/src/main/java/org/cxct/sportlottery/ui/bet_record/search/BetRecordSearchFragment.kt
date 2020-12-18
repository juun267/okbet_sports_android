package org.cxct.sportlottery.ui.bet_record.search

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.fragment_bet_record_search.*
import kotlinx.android.synthetic.main.spinner_item_bet.view.*
import kotlinx.android.synthetic.main.spinner_item_bet.view.text_view
import kotlinx.android.synthetic.main.spinner_item_bet_first.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetRecordSearchBinding
import org.cxct.sportlottery.ui.bet_record.BetRecordViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class BetRecordSearchFragment : Fragment() {

    private val betRecordViewModel: BetRecordViewModel by viewModel()

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

        initSpinner()
        setObserver()
        setOnClick()
    }

//    private val statusArr = (fun(list) -> betRecordViewModel.statusNameMap.forEach {
//        list += it.value
//    })

    private val statusList = mutableListOf<String>()

    private fun Map<Int, String>.addValueToArray(array: MutableList<String>) = this.forEach {
        array.add(it.value)
    }

    private fun initSpinner() {
        betRecordViewModel.statusNameMap.addValueToArray(statusList)

        spinner_bet_state.adapter = BetSpinnerAdapter(spinner_bet_state.context, statusList)
        spinner_bet_state.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
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
            spinner_bet_state.performClick()
        }
    }

}

class BetSpinnerAdapter(private val context: Context, private val dataList: MutableList<String>): BaseAdapter() {

    var viewType = ViewType.FIRST

    enum class ViewType {
        FIRST, OTHERS
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
                view.text_view_first.text = data
            }

            ViewType.OTHERS -> {
                view.text_view.text = data
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