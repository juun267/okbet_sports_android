package org.cxct.sportlottery.ui.bet_record.search.result

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_bet_record_result.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetRecordResultBinding
import org.cxct.sportlottery.databinding.FragmentBetRecordSearchBinding
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.bet_record.BetRecordViewModel

class BetRecordResultFragment : BaseFragment<BetRecordViewModel>(BetRecordViewModel::class) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentBetRecordResultBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bet_record_result, container, false)
        binding.apply {
            betRecordViewModel = this@BetRecordResultFragment.viewModel
            lifecycleOwner = this@BetRecordResultFragment
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRv()
    }

    private fun initRv() {
        val rvAdapter = BetRecordAdapter(ItemClickListener {
            it.let { orderNum ->
                Log.e(">>>", "orderNum = $orderNum")
            }
        })
        rv_bet_record.adapter = rvAdapter
        viewModel.betRecordResult.observe(viewLifecycleOwner, {
            Log.e(">>>", "observe")
            it?.let {
                rvAdapter.addFooterAndSubmitList(it.rows)
            }
        })
    }


}