package org.cxct.sportlottery.ui.game

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
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
import org.cxct.sportlottery.util.isHalloweenStyle
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
            } else if (isHalloweenStyle()) { // 万圣节样式，与下面相同的if条件不能合并
                root.setBackgroundResource(R.drawable.img_game_item_bg_h)
                root.layoutParams.height = 167.dp
                ivCover.layoutParams.height = 105.dp
                5.dp.let { root.getChildAt(0).setPadding(it, it, it, 0) }
                tvCollect.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_collect_heart_h, 0,0 ,0)
                (tvCollect.layoutParams as MarginLayoutParams).let {
                    it.topMargin = 0
                    it.leftMargin = 5.dp
                }
            }

            if (isHalloweenStyle()) {
                ivFav.setImageResource(R.drawable.selector_game_fav_h)
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