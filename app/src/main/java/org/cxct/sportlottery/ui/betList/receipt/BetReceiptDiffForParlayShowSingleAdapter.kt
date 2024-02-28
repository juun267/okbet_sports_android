package org.cxct.sportlottery.ui.betList.receipt

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemMatchReceiptBinding
import org.cxct.sportlottery.network.bet.add.betReceipt.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.util.*

class BetReceiptDiffForParlayShowSingleAdapter : BindingAdapter<MatchOdd,ItemMatchReceiptBinding>() {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var betParlayList: List<ParlayOdd>? = null
    var matchType: MatchType? = null

    fun submit(
        dataItems: List<MatchOdd>,
        betParlayList: List<ParlayOdd>,
        matchType: MatchType?
    ) {
        adapterScope.launch {
            this@BetReceiptDiffForParlayShowSingleAdapter.betParlayList = betParlayList
            this@BetReceiptDiffForParlayShowSingleAdapter.matchType = matchType

            withContext(Dispatchers.Main) {
                setList(dataItems)
            }
        }
    }
    override fun onBinding(position: Int, binding: ItemMatchReceiptBinding, item: MatchOdd)=binding.run {
        var currentOddsType = oddsType

        if (betParlayList?.getOrNull(position)?.odds == betParlayList?.getOrNull(position)?.malayOdds) {
            currentOddsType = OddsType.EU
        }
        //展示串單內容，其中狀態和金額不用顯示
        tvBetStatusSingle.isVisible = false
         matchReceiptBetLayout.root.isVisible = false
        val inPlay = System.currentTimeMillis() > (item.startTime ?: 0)
        if(inPlay){
            tvTypeMatch.visible()
            tvTypeMatch.text = LocalUtils.getString(R.string.home_tab_in_play) //滚球
            tvTypeMatch.background =  AppCompatResources.getDrawable(context,R.drawable.bg_match_type_red_circle)
        }else{
            tvTypeMatch.visible()
            tvTypeMatch.text = LocalUtils.getString(R.string.home_tab_early) //早盘
            tvTypeMatch.background =  AppCompatResources.getDrawable(context,R.drawable.bg_match_type_green_circle)
        }

        if (matchType== MatchType.OUTRIGHT){
            tvTypeMatch.gone()
        }

        val formatForOdd =
            if (item.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(
                getOdds(item, oddsType ?: OddsType.EU) - 1
            ) else TextUtil.formatForOdd(getOdds(item, oddsType))

        tvPlayContent.text = item.playName
        tvSpread.text = if (matchType != MatchType.OUTRIGHT) item.spread else ""
        dividerTitle.isVisible = tvSpread.text.isNotEmpty()

        tvOdds.text = "@ $formatForOdd"


        tvNameType.text = context.getString(getOddTypeRes(item,oddsType))

        tvLeague.text = item.leagueName
        tvTeamNames.setTeamNames(15, item.homeName, item.awayName)
        tvMatchType.tranByPlayCode(item.playCode, item.playCateCode, item.playCateName, item.rtScore)

        if (matchType == MatchType.OUTRIGHT) {
            tvTeamNames.visibility = View.GONE
        }
    }

}

