package org.cxct.sportlottery.ui.bet.record.search.result

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.dialog_bet_record_detail_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetRecordDetailListBinding
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.bet.record.BetRecordViewModel

class BetRecordDetailDialog(val data: Row) : BaseDialog<BetRecordViewModel>(BetRecordViewModel::class) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: DialogBetRecordDetailListBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_bet_record_detail_list, container, false)
        binding.apply {
            betRecordDetailViewModel = this@BetRecordDetailDialog.viewModel
            lifecycleOwner = this@BetRecordDetailDialog
            row = data
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        img_close.setOnClickListener {
            dismiss()
        }

        initRv()
    }

    private fun initRv() {
        val rvAdapter = BetDetailAdapter()
        rv_detail.adapter = rvAdapter
        rvAdapter.submitList(data.matchOdds)
    }

}
