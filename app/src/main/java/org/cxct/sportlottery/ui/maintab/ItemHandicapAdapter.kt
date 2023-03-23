package org.cxct.sportlottery.ui.maintab

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.common.OddsType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.third_game.third_games.hot.HotMatchInfo
import org.cxct.sportlottery.ui.bet.list.BetInfoListData

class ItemHandicapAdapter(val lifecycleOwner: LifecycleOwner, private val homeRecommendListener: HomeRecommendListener) :
    RecyclerView.Adapter<ItemHandicapHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHandicapHolder {
        return ItemHandicapHolder(lifecycleOwner, parent, homeRecommendListener)
    }

    var oddsType: OddsType = MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    var data: MutableList<HotMatchInfo> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
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

    override fun onBindViewHolder(holder: ItemHandicapHolder, position: Int) {
        val itemData = data[position]
        holder.bind(data = itemData, oddsType = oddsType)
    }

    override fun onBindViewHolder(holder: ItemHandicapHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }

        payloads.forEach { payload ->
            if (payload is HotMatchInfo) {
                holder.update(payload, oddsType = oddsType)
            }
        }
    }

    override fun getItemCount(): Int = data.size


}