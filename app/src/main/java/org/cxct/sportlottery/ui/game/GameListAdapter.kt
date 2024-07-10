package org.cxct.sportlottery.ui.game

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemNewgameBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.GameCollectManager.showCollectAmount
import org.cxct.sportlottery.view.onClick

class GameListAdapter(private val onGameClick: (OKGameBean)-> Unit,
                      private val onFavoriteClick: (View, OKGameBean) -> Unit,
                      private val collectNumberEnable: Boolean = false): BindingAdapter<OKGameBean, ItemNewgameBinding>() {

    var imgWH = 115.dp

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

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BindingVH<ItemNewgameBinding> {
        val vh = super.onCreateDefViewHolder(parent, viewType)
        vh.vb.tvCollect.isVisible = collectNumberEnable
        vh.vb.root.layoutParams.width = imgWH
        vh.vb.ivCover.layoutParams.height = imgWH
        return vh
    }

    override fun onBinding(position: Int, binding: ItemNewgameBinding, item: OKGameBean) = binding.run {
        root.onClick { if (!tvCover.isVisible) { onGameClick(item) } }
        ivCover.load(item.imgGame, R.drawable.ic_okgames_nodata)
        tvName.text = item.gameName
        tvFirmName.text = item.firmName
        ivFav.isSelected = item.markCollect
        ivFav.isEnabled = !item.isMaintain()
        ivFav.onClick { onFavoriteClick(ivFav, item) }
        tvCover.isVisible = item.isMaintain()
        if (tvCollect.isVisible) {
            tvCollect.showCollectAmount(item.id)
        }

    }

    override fun onBinding(position: Int, binding: ItemNewgameBinding, item: OKGameBean, payloads: List<Any>) = binding.run {
        tvCover.isVisible = item.isMaintain()
        ivFav.isSelected = item.markCollect
        if (tvCollect.isVisible) {
            tvCollect.showCollectAmount(item.id)
        }
    }


}