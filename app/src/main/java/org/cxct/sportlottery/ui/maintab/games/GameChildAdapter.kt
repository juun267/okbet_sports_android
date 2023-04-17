package org.cxct.sportlottery.ui.maintab.games

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.util.LanguageManager

class GameChildAdapter(data: List<QueryGameEntryData>) :
    BaseQuickAdapter<QueryGameEntryData, BaseViewHolder>(R.layout.item_game_child,
        data?.toMutableList()) {
    override fun convert(helper: BaseViewHolder, item: QueryGameEntryData) {
        Glide.with(context)
            .load(item.entryImage)
            .apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontTransform())
            .into(helper.getView(R.id.iv_cover))
        if (item.gameNamemap == null) {
            helper.setText(R.id.tv_name, item.firmName)
        } else {
            item.gameNamemap?.let {
                helper.setText(R.id.tv_name,
                    when (LanguageManager.getSelectLanguage(context)) {
                        LanguageManager.Language.ZH -> it.zh
                        LanguageManager.Language.VI -> it.vi
                        LanguageManager.Language.PHI -> it.ph
                        else -> it.en
                    })
            }
        }
        addChildClickViewIds(R.id.iv_fav)
        helper.setText(R.id.tv_firm_name, item.firmCode)
        helper.getView<ImageView>(R.id.iv_fav).isSelected = item.markCollect
    }
}

