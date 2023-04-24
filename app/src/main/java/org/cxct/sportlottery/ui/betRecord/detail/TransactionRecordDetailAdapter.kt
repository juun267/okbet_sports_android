package org.cxct.sportlottery.ui.betRecord.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_bet_details.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.MatchOdd
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.network.bet.settledDetailList.ParlayComsDetailVO
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.betRecord.ParlayType
import org.cxct.sportlottery.util.TextUtil

class TransactionRecordDetailAdapter :
    ListAdapter<ParlayComsDetailVO, RecyclerView.ViewHolder>(TransactionRecordDiffCallBack()) {
    private val itemList = arrayListOf<ParlayComsDetailVO>()
    private var mParlayType=""
    private var mGameType=""
    private var mMatchType:String?=null
    private enum class ViewType {Outright, NoData }

    fun setupBetList(rowData: Row) {
        rowData.parlayComsDetailVOs?.let {
            //从matchOdds拿到icon
            getCategoryIcon(it,rowData.matchOdds)
            itemList.addAll(rowData.parlayComsDetailVOs)
            mParlayType=rowData.parlayType
            mGameType=rowData.gameType
            mMatchType=rowData.matchType
            submitList(itemList)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.NoData.ordinal -> NoDataViewHolder.from(parent)
            else -> ParlayRecordViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val rvData = getItem(holder.bindingAdapterPosition)
        when (holder) {

            is ParlayRecordViewHolder -> {
                holder.bind(rvData,mParlayType,mGameType,mMatchType)
            }

            is NoDataViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return ViewType.Outright.ordinal
    }



    class ParlayRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.item_bet_details, viewGroup, false)
                return ParlayRecordViewHolder(view)
            }
        }

        fun bind(data: ParlayComsDetailVO,mParlayType:String,mGameType:String,mMatchType:String?) {
            val contentParlayMatchAdapter by lazy { ContentParlayDetailAdapter(data.status!!) }

            itemView.apply {
                ParlayType.getParlayStringRes(mParlayType)?.let { parlayTypeStringResId ->
                    val parlayTitle = context.getString(R.string.bet_record_parlay) +
                            "(${context.getString(parlayTypeStringResId)})" +
                            "-${GameType.getGameTypeString(context, mGameType)}"
                    title_parlay_type.text = parlayTitle
                }
                rv_parlay_match.apply {
                    adapter = contentParlayMatchAdapter
                    layoutManager =
                        LinearLayoutManager(itemView.context, RecyclerView.VERTICAL, false)
                    contentParlayMatchAdapter.setupMatchData(
                        mGameType,
                        data.matchOddsVOList,
                        mMatchType
                    )

                }

                data.stake?.let {
                    content_parlay_bet_amount.text = TextUtil.format(it)
                }

            }
        }
    }

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.itemview_game_no_record, parent, false)
                )
        }

        fun bind() {
            itemView.apply {
            }
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

class TransactionRecordDiffCallBack : DiffUtil.ItemCallback<ParlayComsDetailVO>() {
    override fun areItemsTheSame(oldItem: ParlayComsDetailVO, newItem: ParlayComsDetailVO): Boolean {
//        return oldItem.orderNo == newItem.orderNo
        return false
    }

    override fun areContentsTheSame(oldItem: ParlayComsDetailVO, newItem: ParlayComsDetailVO): Boolean {
        return oldItem == newItem
    }

}
