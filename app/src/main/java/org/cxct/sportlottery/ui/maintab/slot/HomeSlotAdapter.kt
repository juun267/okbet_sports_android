package org.cxct.sportlottery.ui.maintab.slot

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData

class HomeSlotAdapter(data: MutableList<QueryGameEntryData>) :
    BaseQuickAdapter<QueryGameEntryData, BaseViewHolder>(
        R.layout.item_home_slot, data
    ) {

    override fun convert(helper: BaseViewHolder, item: QueryGameEntryData) {
        if (context!=null ){
            Glide.with(context)
                .load(item.entryImage)
                .apply(
                    RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontTransform()
                )
                .into(helper.getView(R.id.iv_people))
        }

        helper.setText(R.id.tv_firm_name, item.firmName)
            .setText(
                R.id.tv_status,
                if (item.gameCode == "TPG") {
                    R.string.new_games
                } else {
                    R.string.new_games_beta
                }
            )?.setGone(R.id.tv_game_name, true)
    }
}