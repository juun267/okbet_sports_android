package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemHomeOkgameBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH

class RecyclerHomeOkGamesAdapter : BindingAdapter<OKGameBean, ItemHomeOkgameBinding>() {
    private var imageSize:Int=0

    override fun onCreateDefViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingVH<ItemHomeOkgameBinding> {
        val vh = super.onCreateDefViewHolder(parent, viewType)
        setImageSize(vh.vb.cardCover)
        return vh
    }

    override fun onBinding(position: Int, binding: ItemHomeOkgameBinding, item: OKGameBean) {
        binding.tvGameName.text = item.gameName
        binding.tvGameType.text = item.firmName
        binding.ivCover.load(item.imgGame, R.drawable.ic_okgames_nodata)
    }

    fun setScreenWidth(screenWidth:Int){
        imageSize = screenWidth/3
    }

    private fun setImageSize(imageView: View){
        val layoutParams = imageView.layoutParams
        layoutParams.width = imageSize
        layoutParams.height = imageSize
        imageView.layoutParams = layoutParams
    }
}