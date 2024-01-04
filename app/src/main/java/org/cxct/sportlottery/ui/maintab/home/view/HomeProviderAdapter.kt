package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.inVisible
import org.cxct.sportlottery.common.extentions.roundOf
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemHomeProviderPageBinding
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.util.DisplayUtil.dp

class HomeProviderAdapter(private val itemClick: (OKGamesFirm) -> Unit) : BindingAdapter<List<OKGamesFirm>, ItemHomeProviderPageBinding>() {

    fun bindLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifecycleOwner.lifecycleScope) { gamesMaintain ->
            data.forEachIndexed { index, okGamesFirms ->
                val changedPosition = mutableListOf<Pair<Int, OKGamesFirm>>()

                okGamesFirms.forEachIndexed { position, gameFirm->
                    if (gameFirm.maintain != gamesMaintain.maintain && gameFirm.firmName == gamesMaintain.firmName) {
                        gameFirm.maintain = gamesMaintain.maintain
                        changedPosition.add(Pair(position, gameFirm))
                    }
                }
                if (changedPosition.isNotEmpty()) {
                    notifyItemChanged(index, changedPosition)
                }
            }
        }
    }
    override fun onBinding(
        position: Int,
        vb: ItemHomeProviderPageBinding,
        item: List<OKGamesFirm>,
    ) = vb.run {
        (vb.root.layoutParams as ViewGroup.MarginLayoutParams).apply {
            rightMargin = if (position==itemCount-1) 52.dp else 24.dp
        }
        root.children.forEachIndexed { index, view ->
            if (index>=item.size){
                view.inVisible()
            }else{
                val itemChild = item[index]
                view.visible()
               setUpItemView(view,itemChild)
            }
        }
    }
    private fun setUpItemView(view: View, item: OKGamesFirm){
        val isMaintenance = item.isMaintain()
        view.findViewById<ImageView>(R.id.ivLogo).apply {
                roundOf(item.imgMobile,8.dp,R.drawable.ic_okgames_nodata)
               alpha = if(isMaintenance) 0.5f else 1f
        }
        view.findViewById<TextView>(R.id.tvName).apply {
            text = item.firmShowName?:item.firmName
            setTextColor(resources.getColor(if(isMaintenance) R.color.color_BEC7DC  else R.color.color_0D2245))
        }
        view.findViewById<TextView>(R.id.tvDesp).apply {
            text = item.remark
            setTextColor(resources.getColor(if(isMaintenance) R.color.color_BEC7DC  else R.color.color_0D2245))
        }
        view.findViewById<TextView>(R.id.tvPlay).apply {
            isVisible = !isMaintenance
            setOnClickListener { itemClick(item) }
        }
//        view.setBackgroundResource(if(isMaintenance) R.drawable.bg_gray_radius_8_f9fafd else R.color.color_FFFFFF)
        view.setBackgroundResource(if(isMaintenance) R.drawable.bg_gray_radius_8_eef3fc else R.color.transparent)
    }
}
