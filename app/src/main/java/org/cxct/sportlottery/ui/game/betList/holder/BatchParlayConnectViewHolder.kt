package org.cxct.sportlottery.ui.game.betList.holder

import android.view.View
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.betList.adapter.BetListRefactorAdapter
import org.cxct.sportlottery.ui.game.betList.listener.OnItemClickListener
import org.cxct.sportlottery.ui.game.betList.listener.OnSelectedPositionListener
import org.cxct.sportlottery.ui.menu.OddsType

//串關
class BatchParlayConnectViewHolder(itemView: View) : BatchParlayViewHolder(itemView) {
    fun bind(
        itemData: ParlayOdd?,
        currentOddsType: OddsType,
        hasBetClosed: Boolean,
        onItemClickListener: OnItemClickListener,
        mSelectedPosition: Int,
        mBetView: BetListRefactorAdapter.BetViewType,
        onSelectedPositionListener: OnSelectedPositionListener,
        position: Int,
        userMoney: Double,
        userLogin: Boolean,
        betList: MutableList<BetInfoListData>?
    ) {
        setupParlayItem(
            itemData,
            currentOddsType,
            hasBetClosed,
            false,
            onItemClickListener,
            mSelectedPosition,
            mBetView,
            onSelectedPositionListener,
            position,
            userMoney,
            userLogin,
            betList
        )
    }
}
