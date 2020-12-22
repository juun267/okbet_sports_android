package org.cxct.sportlottery.ui.bet_record.search.result

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.fragment_bet_record_result.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetRecordSearchBinding
import org.cxct.sportlottery.ui.bet_record.BetRecordViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class BetRecordResultFragment : Fragment() {

    private val betRecordViewModel: BetRecordViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentBetRecordSearchBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_bet_record_result, container, false)
        binding.apply {
            betRecordViewModel = this@BetRecordResultFragment.betRecordViewModel
            lifecycleOwner = this@BetRecordResultFragment
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRv()
    }

    private fun initRv() {
        rv_bet_record
    }
}