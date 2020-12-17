package org.cxct.sportlottery.ui.bet_record.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.fragment_bet_record_search.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetRecordSearchBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class BetRecordSearchFragment : Fragment() {

    private val betRecordSearchViewModel: BetRecordSearchViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentBetRecordSearchBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bet_record_search, container, false)
        binding.apply {
            betRecordSearchViewModel = this@BetRecordSearchFragment.betRecordSearchViewModel
            lifecycleOwner = this@BetRecordSearchFragment //TODO Cheryl: 用法學習！
        }

//        setOnClick()
        return binding.root
    }
/*

    private fun setOnClick() {
        tv_bet_status.setOnClickListener  {
//            view?.findNavController()?.navigate(BetRecordSearchFragmentDirections.actionBetRecordSearchFragmentToBetRecordResultFragment("hello123"))
        }
    }
*/

}