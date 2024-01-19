package org.cxct.sportlottery.ui.betList.receipt

import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_match_receipt.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.network.bet.add.betReceipt.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.PlayCate.Companion.needShowSpread
import org.cxct.sportlottery.util.*

class BetReceiptDiffForParlayShowSingleAdapter : ListAdapter<MatchOdd, RecyclerView.ViewHolder>(
    BetReceiptForParlayShowSingleCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var betParlayList: List<ParlayOdd>? = null
    var matchType: MatchType? = null

    var items = listOf<MatchOdd>()

    fun submit(
        dataItems: List<MatchOdd>,
        betParlayList: List<ParlayOdd>,
        matchType: MatchType?
    ) {
        adapterScope.launch {
            this@BetReceiptDiffForParlayShowSingleAdapter.betParlayList = betParlayList
            this@BetReceiptDiffForParlayShowSingleAdapter.matchType = matchType

            items = dataItems

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SingleViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        var currentOddsType = oddsType

        if (betParlayList?.getOrNull(position)?.odds == betParlayList?.getOrNull(position)?.malayOdds) {
            currentOddsType = OddsType.EU
        }

        when (holder) {
            is SingleViewHolder -> {
                val itemData = getItem(position) as MatchOdd
                holder.bind(itemData, currentOddsType, position, matchType)
            }
        }
    }

    class SingleViewHolder private constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.item_match_receipt, viewGroup, false)
                return SingleViewHolder(view)
            }
        }

        fun bind(itemData: MatchOdd?, oddsType: OddsType, position: Int, matchType: MatchType?) {
            itemView.apply {

                //展示串單內容，其中狀態和金額不用顯示
                tv_bet_status_single.isVisible = false
                match_receipt_bet_layout.isVisible = false
                val inPlay = System.currentTimeMillis() > (itemData?.startTime ?: 0)
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

                itemData?.apply {
                    val formatForOdd =
                        if (this.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage(
                            getOdds(this, oddsType ?: OddsType.EU) - 1
                        ) else TextUtil.formatForOdd(getOdds(this, oddsType))

                    tv_play_content.text = playName
                    tvSpread.text = if (matchType != MatchType.OUTRIGHT) spread else ""
                    dividerTitle.isVisible = tvSpread.text.isNotEmpty()

                    tv_odds.text = "@ $formatForOdd"


                    tv_name_type.text = context.getString(getOddTypeRes(itemData,oddsType))

                    tv_league.text = leagueName
                    tv_team_names.setTeamNames(15, homeName, awayName)
                    tv_match_type.tranByPlayCode(playCode, playCateCode, playCateName, rtScore)

                    if (matchType == MatchType.OUTRIGHT) {
                        tv_team_names.visibility = View.GONE
                    }
                }
            }
        }
    }
}

class BetReceiptForParlayShowSingleCallback : DiffUtil.ItemCallback<MatchOdd>() {
    override fun areItemsTheSame(oldItem: MatchOdd, newItem: MatchOdd): Boolean {
        return oldItem.oddsId == newItem.oddsId
    }

    override fun areContentsTheSame(oldItem: MatchOdd, newItem: MatchOdd): Boolean {
        return oldItem == newItem
    }
}
