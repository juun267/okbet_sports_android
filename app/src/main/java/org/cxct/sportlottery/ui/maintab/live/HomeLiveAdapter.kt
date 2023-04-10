package org.cxct.sportlottery.ui.maintab.live

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.databinding.ItemHomeLiveBinding
import org.cxct.sportlottery.network.odds.list.MatchLiveData
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.view.OKVideoPlayer

class HomeLiveAdapter(
    val lifecycleOwner: LifecycleOwner,
    val homeLiveListener: HomeLiveListener
) :
    RecyclerView.Adapter<ItemHomeLiveHolder>() {

    var playerView: OKVideoPlayer? = null

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
            notifyDataSetChanged()
        }

    var data: List<MatchLiveData> = mutableListOf()
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


    override fun onBindViewHolder(holder: ItemHomeLiveHolder, position: Int) {
        val itemData = data[position]
        holder.bind(data = itemData, oddsType = oddsType)
    }

    override fun getItemCount(): Int = data.size


}