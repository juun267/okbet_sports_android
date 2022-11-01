package org.cxct.sportlottery.ui.maintab.elec

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.util.LanguageManager

class HomeElecAdapter(data: List<QueryGameEntryData>) :
    BaseQuickAdapter<QueryGameEntryData, BaseViewHolder>(R.layout.item_home_elec, data) {
    override fun convert(helper: BaseViewHolder, item: QueryGameEntryData) {
        Glide.with(mContext)
            .load(item.entryImage)
            .apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontTransform())
            .into(helper.getView(R.id.iv_logo))

        item.gameNamemap?.let {
            helper.setText(R.id.tv_game_name, when (LanguageManager.getSelectLanguage(mContext)) {
                LanguageManager.Language.ZH -> it.zh
                LanguageManager.Language.VI -> it.vi
                LanguageManager.Language.EN -> it.en
                else -> it.en
            })
        }

    }
}

