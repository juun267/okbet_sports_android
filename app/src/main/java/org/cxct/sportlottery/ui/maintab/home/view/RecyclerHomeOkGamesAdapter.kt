package org.cxct.sportlottery.ui.maintab.home.view

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemHomeOkgameBinding
import org.cxct.sportlottery.net.games.data.OKGameBean

class RecyclerHomeOkGamesAdapter : BindingAdapter<OKGameBean, ItemHomeOkgameBinding>() {

    override fun onBinding(position: Int, binding: ItemHomeOkgameBinding, item: OKGameBean) {
        binding.tvGameName.text = item.gameName
        binding.tvGameType.text = item.firmName
        binding.ivCover.load(item.imgGame, R.drawable.ic_okgames_nodata)
    }

}