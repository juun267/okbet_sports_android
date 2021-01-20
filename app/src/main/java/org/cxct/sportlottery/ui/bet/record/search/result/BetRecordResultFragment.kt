package org.cxct.sportlottery.ui.bet.record.search.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_bet_record_result.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetRecordResultBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.bet.record.BetRecordViewModel

class BetRecordResultFragment : BaseFragment<BetRecordViewModel>(BetRecordViewModel::class) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentBetRecordResultBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bet_record_result, container, false)
        binding.apply {
            betRecordViewModel = this@BetRecordResultFragment.viewModel
            lifecycleOwner = this@BetRecordResultFragment
//            other = this@BetRecordResultFragment.viewModel.betRecordResult.value?.other
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTv()
        initRv()
    }

    private fun initTv() {
        viewModel.selectStatusNameList.observe(viewLifecycleOwner, {
            tv_bet_status.text = viewModel.getBetStatus()
        })
    }

    private fun initRv() {
        val rvAdapter = BetRecordAdapter(ItemClickListener {
            it.let { data ->
                val detailDialog = BetRecordDetailDialog(data)
                detailDialog.show(parentFragmentManager, "BetRecordDetailDialog")
            }
        })
        rvAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                rv_bet_record.layoutManager?.scrollToPosition(0)
            }
        })

        rv_bet_record.adapter = rvAdapter
        viewModel.betRecordResult.observe(viewLifecycleOwner, {
            it.let {
                rvAdapter.addFooterAndSubmitList(it.rows)
            }
        })

    }


}