package org.cxct.sportlottery.ui.betList.holder

import android.view.View
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.listener.OnItemClickListener
import org.cxct.sportlottery.util.KeyboardView

//串關
class BatchParlayConnectViewHolder(itemView: View, keyboardView: KeyboardView) : BatchParlayViewHolder(itemView,keyboardView) {
    fun bind(
        itemData: ParlayOdd?,
        hasBetClosed: Boolean,
        onItemClickListener: OnItemClickListener,
        position: Int,
        userMoney: Double,
        userLogin: Boolean,
        betList: MutableList<BetInfoListData>?
    ) {
        setupParlayItem(
            itemData,
            hasBetClosed,
            onItemClickListener,
            position,
            userMoney,
            userLogin,
            betList
        )
    }
}
