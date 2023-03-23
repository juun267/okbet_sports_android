package org.cxct.sportlottery.ui.main.accountHistory.next

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.*
import org.cxct.sportlottery.network.bet.settledDetailList.MatchOdd
import org.cxct.sportlottery.enum.OddsType
import org.cxct.sportlottery.util.*

class ParlayItemAdapter : ListAdapter<ParlayDataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
        ITEM, NO_DATA
    }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var gameType: String = ""
    var matchType: String? = null

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addFooterAndSubmitList(list: List<MatchOdd>?, isLastPage: Boolean) {
        adapterScope.launch {
            val items = when {
                list.isNullOrEmpty() -> listOf(ParlayDataItem.NoData)
                isLastPage -> list.map { ParlayDataItem.Item(it) } + listOf(ParlayDataItem.Footer)
                else -> list.map { ParlayDataItem.Item(it) }
            }

            withContext(Dispatchers.Main) { //update in main ui thread
                submitList(items)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> ItemViewHolder.from(parent)
            else -> NoDataViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val data = getItem(position) as ParlayDataItem.Item
                data.matchOdd.oddsType?.let { holder.bind(data.matchOdd, it, gameType, matchType) }
            }

            is NoDataViewHolder -> {
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ParlayDataItem.Item -> ItemType.ITEM.ordinal
            else -> ItemType.NO_DATA.ordinal
        }
    }


    class ItemViewHolder private constructor(val binding: ItemAccountHistoryNextContentParlayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val roundAdapter by lazy { RoundAdapter() }

        fun bind(matchOdd: MatchOdd, oddsType: String, gameType: String, matchType: String?) {

            binding.gameType = gameType
            binding.matchOdd = matchOdd

            binding.tvTime.text = TimeUtil.timeFormat(matchOdd.startTime, TimeUtil.YMD_HM_FORMAT)

            matchOdd.apply {
                binding.tvTeamNames.setTeamsNameWithVS(homeName, awayName)

                //篮球 滚球 全场让分【欧洲盘】
                binding.tvGameTypePlayCate.setGameType_MatchType_PlayCateName_OddsType(gameType, matchType, playCateName, oddsType)

                val odds = getOdds(matchOdd, matchOdd.oddsType.toString())
                binding.playContent.setPlayContent(
                    playName,
                    spread,
                    TextUtil.formatForOdd(odds)
                )
//                when (gameType) {
//                    GameType.FT.key -> {
//                        if (rtScore?.isNotEmpty() == true) {
//                            binding.tvScore.text = "(${rtScore})"
//                            binding.tvScore.visibility = View.VISIBLE
//                        }
//                        else {
//                            binding.tvScore.visibility = View.GONE
//                        }
//                    }
//                    else -> {
//                        binding.tvScore.visibility = View.GONE
//                    }
//                }
//                val scoreList = mutableListOf<String>()
//                playCateMatchResultList?.map { scoreData ->
//                    scoreList.add(
//                        "${
//                            scoreData.statusNameI18n?.get(
//                                LanguageManager.getSelectLanguage(
//                                    itemView.context
//                                ).key
//                            )
//                        }: ${scoreData.score}"
//                    )
//                }
//
//                binding.listScore.apply {
//                    layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
//                    adapter = roundAdapter
//                }
//                roundAdapter.submitList(scoreList)

            }
            binding.executePendingBindings() //加上這句之後數據每次丟進來時才能夠即時更新
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAccountHistoryNextContentParlayItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return ItemViewHolder(binding)
            }
        }

    }

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_no_record, parent, false)
                )
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<ParlayDataItem>() {
        override fun areItemsTheSame(oldItem: ParlayDataItem, newItem: ParlayDataItem): Boolean {
            return oldItem.matchId == newItem.matchId
        }

        override fun areContentsTheSame(oldItem: ParlayDataItem, newItem: ParlayDataItem): Boolean {
            return oldItem == newItem
        }

    }
}

sealed class ParlayDataItem {

    abstract val matchId: String?

    data class Item(val matchOdd: MatchOdd) : ParlayDataItem() {
        override val matchId = matchOdd.matchId
    }

    object Footer : ParlayDataItem() {
        override val matchId: String = ""
    }

    object NoData : ParlayDataItem() {
        override val matchId: String? = null
    }
}