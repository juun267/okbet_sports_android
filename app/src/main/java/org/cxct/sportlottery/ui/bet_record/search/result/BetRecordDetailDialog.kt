package org.cxct.sportlottery.ui.bet_record.search.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetRecordDetailListBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.bet_record.BetRecordViewModel

class BetRecordDetailDialog: BaseDialog<BetRecordViewModel>(BetRecordViewModel::class) {

    init {
//        val width = resources.getDimensionPixelSize(R.dimen.popup_width)
//        val height = resources.getDimensionPixelSize(R.dimen.popup_height)
        dialog?.window?.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.setCanceledOnTouchOutside(false)

//        val binding: FragmentBetRecordResultBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_bet_record_detail_list, container, false)
        val binding: DialogBetRecordDetailListBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_bet_record_detail_list, container, false)
        binding.apply {
            betRecordDetailViewModel = this@BetRecordDetailDialog.viewModel
            lifecycleOwner = this@BetRecordDetailDialog
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }


}
