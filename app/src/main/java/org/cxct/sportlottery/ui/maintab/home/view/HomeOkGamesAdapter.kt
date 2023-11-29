package org.cxct.sportlottery.ui.maintab.home.view

import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemHomeOkgameChrisBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.service.ServiceBroadcastReceiver

class HomeOkGamesAdapter : BindingAdapter<OKGameBean, ItemHomeOkgameChrisBinding>() {

    fun bindLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifecycleOwner.lifecycleScope) { gamesMaintain ->
            data.forEachIndexed { index, okGameBean ->
                if (okGameBean.isMaintain() != gamesMaintain.isMaintain() && (okGameBean.firmType == gamesMaintain.firmType)) {
                    okGameBean.maintain = gamesMaintain.maintain
                    notifyItemChanged(index, 1)
                }
            }
        }
    }
    override fun onBinding(position: Int, binding: ItemHomeOkgameChrisBinding, item: OKGameBean) {
        binding.tvGameName.text = item.gameName
        binding.tvGameType.text = "- ${item.firmName} -"
        binding.ivCover.load(item.imgGame, R.drawable.ic_okgames_nodata)
        binding.blurView
            .setupWith(binding.root)
            .setFrameClearDrawable(binding.root.background)
            .setBlurRadius(4f)
        binding.linMaintenance.isVisible = item.isMaintain()
    }

    override fun onBinding(
        position: Int,
        binding: ItemHomeOkgameChrisBinding,
        item: OKGameBean,
        payloads: List<Any>
    ) {
        binding.linMaintenance.isVisible = item.isMaintain()
    }

}