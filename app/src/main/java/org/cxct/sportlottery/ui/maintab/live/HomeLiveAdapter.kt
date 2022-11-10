package org.cxct.sportlottery.ui.maintab.live

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.databinding.ItemHomeLiveBinding
import org.cxct.sportlottery.network.odds.list.MatchLiveData
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.menu.OddsType
import timber.log.Timber

class HomeLiveAdapter(
    val lifecycleOwner: LifecycleOwner,
    val homeLiveListener: HomeLiveListener
) :
    RecyclerView.Adapter<ItemHomeLiveHolder>() {


    private lateinit var holder: ItemHomeLiveHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHomeLiveHolder {
        return ItemHomeLiveHolder(
            lifecycleOwner,
            ItemHomeLiveBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), homeLiveListener
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
            } else {
                field = null
            }
            data.forEachIndexed { index, _ ->
                notifyItemChanged(index)
            }
        }

    var data: List<MatchLiveData> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
            if (expandMatchId == null) {
                expandMatchId = value.firstOrNull()?.matchInfo?.id
            }
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


    override fun onBindViewHolder(holder: ItemHomeLiveHolder, position: Int) {
        val itemData = data[position]
        if (expandMatchId == itemData.matchInfo.id && itemData.matchInfo.isLive == 1) {
            this.holder = holder
        }
        holder.bind(data = itemData, oddsType = oddsType)
    }

    override fun getItemCount(): Int = data.size

    fun setVolumeMute() {
        if (this::holder.isInitialized) {
            holder.setVolumeStateMute()
        }
    }

}