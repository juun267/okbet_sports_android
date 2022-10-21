package org.cxct.sportlottery.ui.maintab

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.databinding.ItemHomeLiveBinding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.menu.OddsType

class HomeLiveAdapter(private val homeRecommendListener: HomeRecommendListener) :
    RecyclerView.Adapter<ItemHomeLiveHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHomeLiveHolder {
        return ItemHomeLiveHolder(
            ItemHomeLiveBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), homeRecommendListener
        )
    }


    var oddsType: OddsType = MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }
    var expandMatchId: String? = null
        set(value) {
            if (value != field) {
                field = value
                data.forEachIndexed { index, recommend ->
                    notifyItemChanged(index, expandMatchId)
                }
            }
        }

    var data: List<Recommend> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
            expandMatchId = value.firstOrNull()?.matchInfo?.id
        }
    var betInfoList: MutableList<BetInfoListData> = mutableListOf()
        set(value) {
            field = value
            var needUpdate = false
            data.forEach {
                it.oddsMap?.values?.forEach { oddList ->
                    oddList?.forEach { odd ->
                        val newSelectStatus = field.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                        if (odd?.isSelected != newSelectStatus) {
                            odd?.isSelected = newSelectStatus
                            needUpdate = true
                        }
                    }
                }
            }
            if (needUpdate) {
                notifyDataSetChanged()
            }
        }

    fun updateLeague(position: Int, payload: LeagueOdd) {
        notifyItemChanged(position, payload)
    }

    override fun onBindViewHolder(holder: ItemHomeLiveHolder, position: Int) {
        val itemData = data[position]
        holder.bind(data = itemData, oddsType = oddsType)
    }

    override fun onBindViewHolder(
        holder: ItemHomeLiveHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            payloads.forEach { payload ->
                when (payload) {
                    is Recommend -> {
                        holder.update(payload, oddsType = oddsType)
                    }
                    is String -> {
                        holder.updateLive(payload == data[position].matchInfo?.id)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size

    open class HomeLiveListener(
        private val onItemClickListener: () -> Unit,
        private val onClickBetListener: (gameType: String, matchType: MatchType, matchInfo: MatchInfo?, odd: Odd, playCateCode: String, playCateName: String, betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?, playCateMenuCode: String?) -> Unit,
        private val onClickStatisticsListener: (matchId: String) -> Unit,
    ) {
        fun onItemClickListener() = onItemClickListener.invoke()
        fun onClickBetListener(
            gameType: String,
            matchType: MatchType,
            matchInfo: MatchInfo?,
            odd: Odd,
            playCateCode: String,
            playCateName: String,
            betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
            playCateMenuCode: String?,
        ) {
            onClickBetListener.invoke(
                gameType,
                matchType,
                matchInfo,
                odd,
                playCateCode,
                playCateName,
                betPlayCateNameMap,
                playCateMenuCode
            )
        }

        fun onClickStatisticsListener(matchId: String) = onClickStatisticsListener.invoke(matchId)
    }

}