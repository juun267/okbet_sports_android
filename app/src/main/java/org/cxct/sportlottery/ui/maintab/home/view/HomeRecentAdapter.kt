package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.setMargins
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemHomeRecentBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.ESportType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.RecentRecord
import org.cxct.sportlottery.util.isHalloweenStyle

class HomeRecentAdapter : BindingAdapter<RecentRecord, ItemHomeRecentBinding>() {

    override fun onCreateDefViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingVH<ItemHomeRecentBinding> {
        val holder = super.onCreateDefViewHolder(parent, viewType)
        if (isHalloweenStyle()) {
            ((holder.vb.blurView.parent as View).layoutParams as MarginLayoutParams).setMargins(4.dp)
        }
        return holder
    }

    override fun onBinding(
        position: Int,
        vb: ItemHomeRecentBinding,
        item: RecentRecord,
    ): Unit = vb.run {
        when (item.recordType) {
            0-> {
                val gameType = GameType.getGameType(item.gameType)
                when {
                    gameType != null -> {
                        if (item.gameType == GameType.ES.key) {
                            val esportType =
                                ESportType.getGameType(item.categoryCode) ?: ESportType.OTHERS
                            ivCover.load(ESportType.getRecentImg(esportType.key))
                            tvName.text = context.getString(GameType.ES.string)
                        } else {
                            ivCover.load(GameType.getRecentImg(item.gameType))
                            tvName.text = GameType.getGameTypeString(context, item.gameType)
                        }
                    }
                    else -> {
                        ivCover.load(R.drawable.bg_recent_rocket)
                        tvName.text = ""
                    }
                }
            }
            else->{
            item.gameBean?.let {
                if (it.firmType==Constants.FIRM_TYPE_SBTY){
                    ivCover.load(it.imgGame, R.drawable.bg_recent_play)
                }else{
                    ivCover.load(it.imgGame, R.drawable.ic_okgames_nodata)
                }
                tvName.text = it.gameName
            }
            }
        }
        blurView
            .setupWith(root)
            .setFrameClearDrawable(root.background)
            .setBlurRadius(4f)
    }
}
