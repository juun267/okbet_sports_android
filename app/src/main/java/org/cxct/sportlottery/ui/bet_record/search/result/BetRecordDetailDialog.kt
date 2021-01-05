package org.cxct.sportlottery.ui.bet_record.search.result

import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetRecordDetailListBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.bet_record.BetRecordViewModel

class BetRecordDetailDialog: BaseDialog<BetRecordViewModel>(BetRecordViewModel::class) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

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
