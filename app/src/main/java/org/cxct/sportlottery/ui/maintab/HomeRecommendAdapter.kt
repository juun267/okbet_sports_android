package org.cxct.sportlottery.ui.maintab

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.databinding.ItemHomeRecommendBinding
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.menu.OddsType

class HomeRecommendAdapter(val lifecycleOwner: LifecycleOwner, private val homeRecommendListener: HomeRecommendListener) :
    RecyclerView.Adapter<ItemHomeRecommendHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHomeRecommendHolder {
        return ItemHomeRecommendHolder(
            lifecycleOwner,
            ItemHomeRecommendBinding.inflate(
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

    var data: List<Recommend> = mutableListOf()
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

    override fun onBindViewHolder(holder: ItemHomeRecommendHolder, position: Int) {
        val itemData = data[position]
        holder.bind(data = itemData, oddsType = oddsType)
    }

    override fun onBindViewHolder(
        holder: ItemHomeRecommendHolder,
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
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size

}