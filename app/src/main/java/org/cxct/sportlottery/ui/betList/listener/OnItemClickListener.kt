package org.cxct.sportlottery.ui.betList.listener
import android.widget.TextView
import org.cxct.sportlottery.network.bet.info.MatchOdd

interface OnItemClickListener {
        fun onDeleteClick(oddsId: String, currentItemCount: Int)
        fun onRechargeClick()
        fun onShowKeyboard(position: Int)
        fun onShowParlayKeyboard(position: Int)
        fun onHideKeyBoard()
        fun saveOddsHasChanged(matchOdd: MatchOdd)
        fun refreshBetInfoTotal(isSingleAdapter: Boolean = false)
        fun showParlayRule(parlayType: String, parlayRule: String)
        fun onMoreOptionClick()
        fun onOddsChangeAcceptSelect(tvTextSelect: TextView)
        fun onOddsChangesSetOptionListener(text: String)
        fun addMore()
        fun clearCarts()
    }