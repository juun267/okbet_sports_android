package org.cxct.sportlottery.ui.bet_record.search.result

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_bet_record_result.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetRecordResultBinding
import org.cxct.sportlottery.databinding.FragmentBetRecordSearchBinding
import org.cxct.sportlottery.ui.bet_record.BetRecordViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class BetRecordResultFragment : Fragment() {

    private val betRecordResultViewModel: BetRecordViewModel by viewModel()
//    private val rvAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> = BetRecordAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding: FragmentBetRecordResultBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bet_record_result, container, false)
        binding.apply {
//            betRecordResultViewModel = this@BetRecordResultFragment.betRecordResultViewModel
            lifecycleOwner = this@BetRecordResultFragment
        }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRv()

        Log.e(">>>", "list = ${betRecordResultViewModel.betRecordResult.value?.total}")
    }

    private fun initRv() {
        rv_bet_record
    }
}