package org.cxct.sportlottery.ui.maintab.home.view.hotmatch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.databinding.ItemHomeHotMatchBinding
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.maintab.home.HomeRecommendListener

class HomeHotMatchAdapter(
    val lifecycleOwner: LifecycleOwner,
    val listener: HomeRecommendListener,
) :
    RecyclerView.Adapter<ItemHomeHotMatchHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHomeHotMatchHolder {
        return ItemHomeHotMatchHolder(
            lifecycleOwner,
            ItemHomeHotMatchBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), listener
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

    override fun onBindViewHolder(holder: ItemHomeHotMatchHolder, position: Int) {

        val itemData = data[position]
        holder.bind(data = itemData, oddsType = oddsType)
    }

    override fun getItemCount(): Int = data.size

}