package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemHomeOkgameBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.util.showCollectAmount
import org.cxct.sportlottery.view.onClick

class HomeOkGamesAdapter(val onFavoriate: (View, OKGameBean) -> Unit) : BindingAdapter<OKGameBean, ItemHomeOkgameBinding>() {

    fun bindLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifecycleOwner.lifecycleScope) { gamesMaintain ->
            data.forEachIndexed { index, okGameBean ->
                if (okGameBean.maintain != gamesMaintain.maintain && (okGameBean.firmType == gamesMaintain.firmType)) {
                    okGameBean.maintain = gamesMaintain.maintain
                    notifyItemChanged(index, 1)
                }
            }
        }
    }
    override fun onBinding(position: Int, binding: ItemHomeOkgameBinding, item: OKGameBean)=binding.run {
        tvGameName.text = item.gameName
        tvGameType.text = "- ${item.firmName} -"
        ivCover.load(item.imgGame, R.drawable.ic_okgames_nodata)
        ivFav.isSelected = item.markCollect
        ivFav.isEnabled = !item.isMaintain()
        //收藏点击
        ivFav.clickDelay {
            onFavoriate(it,item)
        }
        tvCollect.showCollectAmount(item.favoriteCount)
        blurView
            .setupWith(root)
            .setFrameClearDrawable(root.background)
            .setBlurRadius(8f)
        linMaintenance.isVisible = item.isMaintain()
    }

    override fun onBinding(
        position: Int,
        binding: ItemHomeOkgameBinding,
        item: OKGameBean,
        payloads: List<Any>
    )=binding.run {
        linMaintenance.isVisible = item.isMaintain()
        tvCollect.showCollectAmount(item.favoriteCount)
        ivFav.isSelected = item.markCollect
    }

}