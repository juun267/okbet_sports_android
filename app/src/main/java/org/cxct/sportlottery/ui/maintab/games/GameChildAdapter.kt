package org.cxct.sportlottery.ui.maintab.games

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemGameChildBinding
import org.cxct.sportlottery.net.games.data.OKGamesGroup
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter

class GameChildAdapter : BindingAdapter<OKGamesGroup, ItemGameChildBinding>() {
    override fun onBinding(position: Int, binding: ItemGameChildBinding, item: OKGamesGroup) {
        binding.apply {
            Glide.with(context)
                .load(item.imgGame)
                .apply(RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform())
                .into(ivCover)
            tvName.text = item.gameName
//            tvName.text =
//                if (item.gameNamemap == null) item.firmName else when (LanguageManager.getSelectLanguage(
//                    context)) {
//                    LanguageManager.Language.ZH -> item.gameNamemap.zh
//                    LanguageManager.Language.VI -> item.gameNamemap.vi
//                    LanguageManager.Language.PHI -> item.gameNamemap.ph
//                    else -> item.gameNamemap.en
//                }
            addChildClickViewIds(R.id.iv_fav)
            tvFirmName.text = item.firmCode
            ivFav.isSelected = item.markCollect
        }
    }
}

