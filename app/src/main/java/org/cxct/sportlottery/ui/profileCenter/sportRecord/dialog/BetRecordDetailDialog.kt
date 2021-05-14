package org.cxct.sportlottery.ui.profileCenter.sportRecord.dialog

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.dialog_bet_record_detail_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetRecordDetailListBinding
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.profileCenter.sportRecord.BetRecordViewModel
import org.cxct.sportlottery.util.TextUtil

class BetRecordDetailDialog(val data: Row) : BaseDialog<BetRecordViewModel>(BetRecordViewModel::class) {

    var rvAdapter = BetDetailAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: DialogBetRecordDetailListBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_bet_record_detail_list, container, false)
        binding.apply {
            betRecordDetailViewModel = this@BetRecordDetailDialog.viewModel
            lifecycleOwner = this@BetRecordDetailDialog
            row = data
            this.textUtil = TextUtil
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootLayout.setOnClickListener {
            dismiss()
        }

        img_close.setOnClickListener {
            dismiss()
        }

        initRv()

        viewModel.oddsType.observe(viewLifecycleOwner, {
            rvAdapter.oddsType = it
        })

    }

    private fun initRv() {
        rvAdapter = BetDetailAdapter()
        rv_detail.adapter = rvAdapter
        rvAdapter.submitList(data.matchOdds)
    }

}
