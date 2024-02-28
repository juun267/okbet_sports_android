package org.cxct.sportlottery.ui.betRecord.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemBetDetailsBinding
import org.cxct.sportlottery.network.bet.MatchOdd
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.bet.settledDetailList.ParlayComsDetailVO
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.betRecord.ParlayType
import org.cxct.sportlottery.util.TextUtil

class TransactionRecordDetailAdapter :
    BindingAdapter<ParlayComsDetailVO,ItemBetDetailsBinding>() {
    private val itemList = arrayListOf<ParlayComsDetailVO>()
    private var mParlayType=""
    private var mGameType=""
    private var mMatchType:String?=null

    fun setupBetList(rowData: Row) {
        rowData.parlayComsDetailVOs?.let {
            //从matchOdds拿到icon
            getCategoryIcon(it,rowData.matchOdds)
            itemList.addAll(rowData.parlayComsDetailVOs)
            mParlayType=rowData.parlayType
            mGameType=rowData.gameType
            mMatchType=rowData.matchType
            setList(itemList)
        }
    }

    override fun onBinding(
        position: Int,
        binding: ItemBetDetailsBinding,
        item: ParlayComsDetailVO,
    ){
        ParlayType.getParlayStringRes(mParlayType)?.let { parlayTypeStringResId ->
            val parlayTitle = context.getString(R.string.bet_record_parlay) +
                    "(${context.getString(parlayTypeStringResId)})" +
                    "-${GameType.getGameTypeString(context, mGameType)}"
            binding.titleParlayType.text = parlayTitle
        }
        binding.rvParlayMatch.apply {
            val contentParlayMatchAdapter = ContentParlayDetailAdapter(item.status!!)
            adapter = contentParlayMatchAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            contentParlayMatchAdapter.setupMatchData(
                mGameType,
                item.matchOddsVOList,
                mMatchType
            )

        }

        item.stake?.let {
            binding.contentParlayBetAmount.text = TextUtil.format(it)
        }

    }

    /**
     * 从matchOdds列表中，拿到icon给parlayComsDetailVOs列表
     */
    private fun getCategoryIcon(parlayComsDetailVOs: List<ParlayComsDetailVO>,matchOdds: List<MatchOdd>){
        parlayComsDetailVOs.forEach {
            it.matchOddsVOList.forEach {matchOddVO->
                matchOdds.forEach {matchOdd->
                    if(matchOddVO.oddsId==matchOdd.oddsId){
                        matchOdd.categoryIcon?.let {
                            matchOddVO.categoryIcon=matchOdd.categoryIcon
                        }
                    }
                }
            }
        }
    }



}
