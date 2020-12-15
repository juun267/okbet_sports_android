package org.cxct.sportlottery.ui.bet_record.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetRecordSearchBinding

class BetRecordSearchFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.fragment_bet_record_search, container, false)
        val binding: FragmentBetRecordSearchBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bet_record_search, container, false)
        setOnClick(binding)
        return binding.root
    }

    private fun setOnClick(binding: FragmentBetRecordSearchBinding) {
        binding.tvBetStatus.setOnClickListener  {
            view?.findNavController()?.navigate(BetRecordSearchFragmentDirections.actionBetRecordSearchFragmentToBetRecordResultFragment("hello123"))
        }

    }

}