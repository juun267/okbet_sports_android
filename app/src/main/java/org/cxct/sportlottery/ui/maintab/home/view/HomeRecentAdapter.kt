package org.cxct.sportlottery.ui.maintab.home.view

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemHomeRecentBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.util.RecentRecord

class HomeRecentAdapter : BindingAdapter<RecentRecord, ItemHomeRecentBinding>() {

    override fun onBinding(
        position: Int,
        vb: ItemHomeRecentBinding,
        item: RecentRecord,
    ): Unit = vb.run {
        if (item.recordType==0){
            ivCover.load(GameType.getSportHomeImg(item.gameType))
            tvName.text = GameType.getGameTypeString(context,item.gameType)
        }else{
            item.gameBean?.let {
                ivCover.load(it.imgGame, R.drawable.ic_okgames_nodata)
                tvName.text = it.gameName
            }
        }
    }
}
