package org.cxct.sportlottery.ui.maintab

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ScreenUtil

class HomeChessAdapter(data: MutableList<QueryGameEntryData>):
    BaseQuickAdapter<QueryGameEntryData, BaseViewHolder>(R.layout.item_poker_game,data) {

    override fun convert(helper: BaseViewHolder, item: QueryGameEntryData) {
        var itemWith = (ScreenUtil.getScreenWidth(mContext) - 30.dp - 10.dp) / 2
        helper.itemView.layoutParams.apply {
            width = itemWith
            helper.itemView.layoutParams = this
        }
        Glide.with(mContext)
            .load(item.entryImage)
            .apply(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform()
                    .placeholder(R.drawable.icon_chess_and_card))
            .into(helper.getView(R.id.iv_poker_bg))

    }

}
