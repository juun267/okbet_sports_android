package org.cxct.sportlottery.ui.maintab.games

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemGameChildBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter

class GameChildAdapter : BindingAdapter<OKGameBean, ItemGameChildBinding>() {
    init {
        addChildClickViewIds(R.id.iv_fav)
    }

    override fun onBinding(position: Int, binding: ItemGameChildBinding, item: OKGameBean) {
        binding.apply {
            ivCover.load(item.imgGame)
            tvName.text = item.gameName
//            tvName.text =
//                if (item.gameNamemap == null) item.firmName else when (LanguageManager.getSelectLanguage(
//                    context)) {
//                    LanguageManager.Language.ZH -> item.gameNamemap.zh
//                    LanguageManager.Language.VI -> item.gameNamemap.vi
//                    LanguageManager.Language.PHI -> item.gameNamemap.ph
//                    else -> item.gameNamemap.en
//                }
            tvFirmName.text = item.firmCode
            ivFav.isSelected = item.markCollect
        }
    }
}

