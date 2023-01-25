package org.cxct.sportlottery.ui.maintab.games

import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import org.cxct.sportlottery.adapter.recyclerview.BindingAdapter
import org.cxct.sportlottery.adapter.recyclerview.BindingVH
import org.cxct.sportlottery.databinding.ItemHomeThirdGameBinding
import org.cxct.sportlottery.extentions.gone
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ScreenUtil


class ThirdGamesAdapter2: BindingAdapter<ThirdGames, ItemHomeThirdGameBinding>() {

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BindingVH<ItemHomeThirdGameBinding> {
        val holder = super.onCreateDefViewHolder(parent, viewType)
        holder.itemView.apply {
            val params = layoutParams as MarginLayoutParams
            params.leftMargin = 10.dp
            params.height = ((ScreenUtil.getScreenWidth(parent.context) - 20.dp) * 160f / 362).toInt()
            params.rightMargin = params.leftMargin
            params.topMargin = 5.dp
        }

        (holder.itemView as ViewGroup).getChildAt(0).gone()
        holder.vb.ivPeople.gone()
        return holder
    }

    override fun onBinding(position: Int, binding: ItemHomeThirdGameBinding, item: ThirdGames) = binding.run {

        if (item.isMaintenance()) {
            root.isEnabled = false
            root.setBackgroundResource(item.maintenanceImg)
            return
        }

        if (item.isEnable()) {
            root.isEnabled = true
            root.setBackgroundResource(item.enableImg)
            return
        }

        root.isEnabled = false
        root.setBackgroundResource(item.disableImg)
    }

    fun update(newDatas: MutableList<GameFirmValues>) {
        var update = false
        data.forEach { item->
            newDatas.find { it.playCode == item.playCode }?.let {
                item.open = it.open ?: 0
                item.sysOpen = it.sysOpen ?: 0
                update = true
            }
        }

        if (update) {
            notifyDataSetChanged()
        }
    }
}