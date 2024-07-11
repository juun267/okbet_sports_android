package org.cxct.sportlottery.ui.game

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.hide
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.ItemNewgameBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.GameCollectManager.showCollectAmount
import org.cxct.sportlottery.view.onClick

class GameListAdapter(private val onGameClick: (OKGameBean)-> Unit,
                      private val onFavoriteClick: (View, OKGameBean) -> Unit,
                      private val isNewStyle: Boolean = true): BindingAdapter<OKGameBean, ItemNewgameBinding>() {

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
        with(vh.vb) {
            if (isNewStyle) {
                val dp108 = 108.dp
                root.layoutParams.width = dp108
                ivCover.layoutParams.height = dp108
                root.setCardBackgroundColor(Color.TRANSPARENT)
                tvName.gravity = Gravity.CENTER
                tvCollect.hide()
                tvFirmName.hide()
            }
        }
        return vh
    }

    override fun onBinding(position: Int, binding: ItemNewgameBinding, item: OKGameBean) = binding.run {
        root.onClick { if (!tvCover.isVisible) { onGameClick(item) } }
        ivCover.load(item.imgGame, R.drawable.ic_okgames_nodata)
        tvName.text = item.gameName
        ivFav.isSelected = item.markCollect
        ivFav.isEnabled = !item.isMaintain()
        ivFav.onClick { onFavoriteClick(ivFav, item) }
        tvCover.isVisible = item.isMaintain()
        if (tvCollect.isVisible) {
            tvCollect.showCollectAmount(item.id)
        }
        if (tvFirmName.isVisible) {
            tvFirmName.text = item.firmName
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