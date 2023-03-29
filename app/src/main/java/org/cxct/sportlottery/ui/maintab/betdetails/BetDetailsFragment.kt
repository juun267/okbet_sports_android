package org.cxct.sportlottery.ui.maintab.betdetails

import android.view.View
import kotlinx.android.synthetic.main.fragment_bet_details.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.OddsType
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.betList.BetListViewModel
import org.cxct.sportlottery.ui.transactionStatus.BetListData

class BetDetailsFragment : BaseFragment<BetListViewModel>(BetListViewModel::class) {

    private val recordDiffAdapter by lazy { TransactionRecordDetailAdapter() }
    private val rowList= arrayListOf<Row>()
    override fun layoutId()= R.layout.fragment_bet_details

    override fun onBindView(view: View) {
        super.onBindView(view)

        val row= arguments?.get("data") as Row?
        row?.let {
            rowList.add(row)
            //初始化数据，只用到了rowList
            val betListData= BetListData(
                rowList,
                OddsType.EU,
                1.0,
                1,
                true
            )

            recordDiffAdapter.setupBetList(betListData)
            rv_bet_record.adapter=recordDiffAdapter
        }




//        recordDiffAdapter.setupBetList()  BetListData

    }
}