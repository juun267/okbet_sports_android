package org.cxct.sportlottery.ui.maintab.games

import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.adapter.recyclerview.BindingAdapter
import org.cxct.sportlottery.adapter.recyclerview.BindingVH
import org.cxct.sportlottery.databinding.ItemHomeSlotBinding
import org.cxct.sportlottery.databinding.ItemHomeThirdGameBinding
import org.cxct.sportlottery.extentions.gone
import org.cxct.sportlottery.extentions.isEmptyStr
import org.cxct.sportlottery.extentions.load
import org.cxct.sportlottery.extentions.visible
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.util.DisplayUtil.dp


class ThirdGamesAdapter(val isLive: Boolean = true): BindingAdapter<GameFirmValues, ItemHomeThirdGameBinding>() {

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BindingVH<ItemHomeThirdGameBinding> {
        val holder = super.onCreateDefViewHolder(parent, viewType)
        holder.itemView.apply {
            val params = layoutParams as MarginLayoutParams
            params.leftMargin = 10.dp
            params.rightMargin = params.leftMargin
            params.topMargin = 5.dp
        }

        holder.vb.tvGameName.gone()
        return holder
    }

    override fun onBinding(position: Int, binding: ItemHomeThirdGameBinding, item: GameFirmValues) = binding.run {

        if (!item.iconUrl.isEmptyStr()) {
            ivPeople.load(item.iconUrl!!)
        } else if (isLive) {
            displayLiveImage(ivPeople, item)
        } else {
            displayCPImage(ivPeople, item)
        }

        tvFirmName.text = item.firmName

        if (item.isMaintenance()) {
            tvStatus.gone()
            ivRepair.visible()
            return@run
        }

        tvStatus.visible()
        ivRepair.gone()

        tvStatus.setText(if (item.isEnable()) R.string.new_games_new_discovery else R.string.comingsoon)
    }

    private fun displayCPImage(imageView: ImageView, item: GameFirmValues) {
        imageView.setImageResource(R.drawable.img_third_game_cgcp)
    }

//    "KY": {
//        "id": 18,
//        "firmName": "开元棋牌",
//        "firmCode": "KY",
//        "firmType": "KY",
//        "firmShowName": "开元棋牌",
//        "playCode": "KY",
//        "sysOpen": 2,
//        "iconUrl": null,
//        "pageUrl": null,
//        "enableDemo": 2,
//        "sort": 15.0,
//        "open": 1,
//        "platformId": null,
//        "certificate": 1
//    }

    private fun displayLiveImage(imageView: ImageView, item: GameFirmValues) {

        if ("CGLIVE" == item.playCode) {
            imageView.setImageResource(R.drawable.img_third_game_cg)
            return
        }

        if ("AGIN" == item.playCode) {
            imageView.setImageResource(R.drawable.img_third_game_ag)
            return
        }

//        if ("AWC" == item.playCode) {
            imageView.setImageResource(R.drawable.img_third_game_awc)
//            return
//        }

    }
}