package org.cxct.sportlottery.ui.maintab.home.view

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemHomeRecentBinding
import org.cxct.sportlottery.network.common.ESportType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.util.RecentRecord

class HomeRecentAdapter : BindingAdapter<RecentRecord, ItemHomeRecentBinding>() {

    override fun onBinding(
        position: Int,
        vb: ItemHomeRecentBinding,
        item: RecentRecord,
    ): Unit = vb.run {

        if (item.recordType==0){
            val gameType = GameType.getGameType(item.gameType)
            when{
                gameType!=null-> {
                    if (item.gameType == GameType.ES.key){
                        val esportType = ESportType.getGameType(item.categoryCode)?:ESportType.OTHERS
                        ivCover.load(ESportType.getRecentImg(esportType.key))
                        tvName.text = context.getString(GameType.ES.string)
                    }else{
                        ivCover.load(GameType.getRecentImg(item.gameType))
                        tvName.text = GameType.getGameTypeString(context,item.gameType)
                    }
                }
                else ->{
                    ivCover.load(R.drawable.bg_recent_rocket)
                    tvName.text = ""
                }
            }
        }else{
            item.gameBean?.let {
                ivCover.load(it.imgGame, R.drawable.ic_okgames_nodata)
                tvName.text = it.gameName
            }
        }
        blurView
            .setupWith(root)
            .setFrameClearDrawable(root.background)
            .setBlurRadius(4f)
    }
}
