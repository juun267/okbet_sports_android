package org.cxct.sportlottery.ui.maintab.home.view.hotmatch

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.databinding.ItemHomeHotMatchBinding
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.maintab.home.HomeRecommendListener
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.drawable.shape.ShapeType
import org.cxct.sportlottery.util.isHalloweenStyle

class HomeHotMatchAdapter(
    val lifecycleOwner: LifecycleOwner,
    val listener: HomeRecommendListener,
) :
    RecyclerView.Adapter<ItemHomeHotMatchHolder>() {

    private lateinit var context: Context
    private val logoBg by lazy {
        ShapeDrawable()
            .setType(ShapeType.OVAL)
            .setSolidColor(context.getColor(R.color.transparent_white_30))
            .setStrokeColor(Color.WHITE)
            .setStrokeSize(1.dp)
            .setHeight(32.dp)
            .setWidth(32.dp)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        context = recyclerView.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHomeHotMatchHolder {

        val vh = ItemHomeHotMatchHolder(lifecycleOwner,
            ItemHomeHotMatchBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false
            ), listener
        )
        vh.binding.ivHomeIcon.background = logoBg
        vh.binding.ivAwayIcon.background = logoBg
        if (isHalloweenStyle()) {
            vh.itemView.setBackgroundResource(R.drawable.bg_trans_home_hot_match_h)
            vh.itemView.setPadding(0, 4.dp, 0, 0)
        }

        return vh
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