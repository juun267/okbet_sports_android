package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.ItemHomeOkgameBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.ui.common.adapter.BindingAdapter

class RecyclerHomeOkGamesAdapter : BindingAdapter<OKGameBean, ItemHomeOkgameBinding>() {
    private var imageSize:Int=0

    override fun onBinding(position: Int, binding: ItemHomeOkgameBinding, item: OKGameBean) {
        binding.tvGameName.text=item.gameName
        binding.tvGameType.text=item.firmName
        setImageSize(binding.cardCover)
        binding.ivCover.load(item.imgGame, R.drawable.img_banner01)
    }

    fun setScreenWidth(screenWidth:Int){
        imageSize=screenWidth/3
    }

    private fun setImageSize(imageView: View){
        val layoutParams=imageView.layoutParams
        layoutParams.width=imageSize
        layoutParams.height=imageSize
        imageView.layoutParams=layoutParams
    }
}