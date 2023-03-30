package org.cxct.sportlottery.ui.maintab.betdetails

import android.view.View
import kotlinx.android.synthetic.main.fragment_bet_details.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.betList.BetListViewModel

class BetDetailsFragment : BaseFragment<BetListViewModel>(BetListViewModel::class) {
    //复制的注单列表的适配器
    private val recordDiffAdapter by lazy { TransactionRecordDetailAdapter() }
    override fun layoutId()= R.layout.fragment_bet_details

    override fun onBindView(view: View) {
        super.onBindView(view)
        val row= arguments?.get("data") as Row?

        row?.let {
            recordDiffAdapter.setupBetList(row)
            rv_bet_record.adapter=recordDiffAdapter
        }

    }
}