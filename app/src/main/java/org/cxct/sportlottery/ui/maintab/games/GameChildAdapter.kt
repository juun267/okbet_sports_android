package org.cxct.sportlottery.ui.maintab.games

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemGameChildBinding
import org.cxct.sportlottery.network.third_game.third_games.QueryGameEntryData
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter
import org.cxct.sportlottery.util.LanguageManager

class GameChildAdapter : BindingAdapter<QueryGameEntryData, ItemGameChildBinding>() {
    override fun onBinding(position: Int, binding: ItemGameChildBinding, item: QueryGameEntryData) {
        binding.apply {
            Glide.with(context)
                .load(item.entryImage)
                .apply(RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform())
                .into(ivCover)
            tvName.text =
                if (item.gameNamemap == null) item.firmName else when (LanguageManager.getSelectLanguage(
                    context)) {
                    LanguageManager.Language.ZH -> item.gameNamemap.zh
                    LanguageManager.Language.VI -> item.gameNamemap.vi
                    LanguageManager.Language.PHI -> item.gameNamemap.ph
                    else -> item.gameNamemap.en
                }
            addChildClickViewIds(R.id.iv_fav)
            tvFirmName.text = item.firmCode
            ivFav.isSelected = item.markCollect
        }
    }
}

