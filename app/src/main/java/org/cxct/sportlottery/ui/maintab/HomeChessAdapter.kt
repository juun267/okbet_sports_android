package org.cxct.sportlottery.ui.maintab

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.home_recommend_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData

class HomeChessAdapter(data: MutableList<QueryGameEntryData>):
    BaseQuickAdapter<QueryGameEntryData, BaseViewHolder>(R.layout.item_poker_game,data) {

    override fun convert(helper: BaseViewHolder, item: QueryGameEntryData) {
        Glide.with(mContext)
            .load(item.entryImage)
            .apply(
                RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontTransform()
                .placeholder(R.drawable.icon_chess_and_card))
            .into(helper.getView(R.id.iv_poker_bg))
        helper.setText(R.id.tv_poker_name,item.chineseName)
        helper.setText(R.id.tv_sub_poker_name,item.englishName)
    }

}
