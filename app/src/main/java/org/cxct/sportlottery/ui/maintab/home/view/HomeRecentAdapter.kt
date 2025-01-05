package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.setMargins
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemHomeRecentBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.ESportType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.isHalloweenStyle

class HomeRecentAdapter : BindingAdapter<OKGameBean, ItemHomeRecentBinding>() {

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
        item: OKGameBean,
    ): Unit = vb.run {
        when (item.gameType) {
            GameEntryType.SPORT-> {
                val gameType = GameType.getGameType(item.gameId)
                if(gameType!=null){
                    ivCover.load(GameType.getRecentImg(item.gameId))
                    tvName.text = GameType.getGameTypeString(context, item.gameId)
                }else{
                    ivCover.load(R.drawable.bg_recent_rocket)
                    tvName.text = ""
                }
            }
            GameEntryType.ES-> {
                val esportType = ESportType.getGameType(item.gameId) ?: ESportType.OTHERS
                ivCover.load(ESportType.getRecentImg(esportType.key))
                tvName.text = context.getString(GameType.ES.string)
            }
            else ->{
                if (item.firmType==Constants.FIRM_TYPE_SBTY){
                    ivCover.load(item.imgGame, R.drawable.bg_recent_play)
                }else{
                    ivCover.load(item.imgGame, R.drawable.ic_okgames_nodata)
                }
                tvName.text = item.gameName
            }
        }
        blurView
            .setupWith(root)
            .setFrameClearDrawable(root.background)
            .setBlurRadius(4f)
    }
}
