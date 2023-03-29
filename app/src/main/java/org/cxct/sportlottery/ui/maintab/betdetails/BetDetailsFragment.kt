package org.cxct.sportlottery.ui.maintab.betdetails

import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.game.betList.BetListViewModel
import org.cxct.sportlottery.ui.transactionStatus.TransactionRecordDiffAdapter

class BetDetailsFragment : BaseFragment<BetListViewModel>(BetListViewModel::class) {

    private val recordDiffAdapter by lazy { TransactionRecordDetailAdapter() }

    override fun layoutId()= R.layout.fragment_bet_details

    override fun onBindView(view: View) {
        super.onBindView(view)

    }
}