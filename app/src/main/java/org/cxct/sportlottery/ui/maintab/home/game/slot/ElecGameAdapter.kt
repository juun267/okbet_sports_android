package org.cxct.sportlottery.ui.maintab.home.game.slot

import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.inVisible
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemElecGameBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.util.TextUtil

class ElecGameAdapter: BindingAdapter<Pair<Int, OKGameBean>, ItemElecGameBinding>() {
    var firmList : List<OKGamesFirm>? = null
    override fun onBinding(position: Int, binding: ItemElecGameBinding, item: Pair<Int,OKGameBean>): Unit =binding.run {
        val bean = item.second
        blurviewMore.gone()
        linMaintenance.gone()
        cvContent.gone()
        cvJackpot.gone()
        ivCover.gone()
        root.inVisible()
        when{
            bean.isShowMore->{
                root.visible()
                blurviewMore.visible()
                blurviewMore
                    .setupWith(binding.root)
                    .setFrameClearDrawable(binding.root.background)
                    .setBlurRadius(4f)
            }
            bean.isShowBlank->{
                //显示空白
                root.inVisible()
            }
            bean.isMaintain()->{
                root.visible()
                ivCover.visible()
                ivCover.load(bean.imgGame, R.drawable.ic_okgames_nodata)
                linMaintenance.visible()
            }
            else -> {
                root.visible()
                cvContent.visible()
                tvGameName.text = bean.gameName
                tvGameType.text = bean.firmName
                ivCover.visible()
                ivCover.load(bean.imgGame, R.drawable.ic_okgames_nodata)
                val firmImg = firmList?.firstOrNull { bean.firmName == it.firmName }?.img
                ivGameIcon.isVisible = !firmImg.isNullOrEmpty()
                if (!firmImg.isNullOrEmpty()) {
                    ivGameIcon.load(firmImg, R.drawable.ic_okgames_nodata)
                }
                if (bean.jackpotOpen == 1) {
                    cvJackpot.visible()
                    binding.blurviewJackpot
                        .setupWith(binding.root)
                        .setFrameClearDrawable(binding.root.background)
                        .setBlurRadius(15f)
                    tvJackPot.text = "$showCurrencySign ${TextUtil.formatMoney(bean.jackpotAmount)}"
                }else{
                    cvJackpot.gone()
                }
            }
        }
    }
}