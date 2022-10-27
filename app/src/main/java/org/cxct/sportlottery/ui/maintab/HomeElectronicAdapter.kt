package org.cxct.sportlottery.ui.maintab

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData

class HomeElectronicAdapter(data: MutableList<QueryGameEntryData>):
    BaseQuickAdapter<QueryGameEntryData, BaseViewHolder>(R.layout.item_electronics_game,data) {

    override fun convert(helper: BaseViewHolder, item: QueryGameEntryData) {

        Glide.with(mContext)
            .load(item.entryImage)
            .apply(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform())
            .into(helper.getView(R.id.iv_electronics))
    }

}
