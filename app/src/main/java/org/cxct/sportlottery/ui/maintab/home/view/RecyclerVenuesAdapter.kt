package org.cxct.sportlottery.ui.maintab.home.view

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemHomeVenuesBinding
import org.cxct.sportlottery.network.index.config.HomeGameBean
import org.cxct.sportlottery.util.getSportEnterIsClose

class RecyclerVenuesAdapter : BindingAdapter<HomeGameBean, ItemHomeVenuesBinding>()  {
    override fun onBinding(position: Int, binding: ItemHomeVenuesBinding, item: HomeGameBean) {
        binding.run {
            when(item.uniqueName){
                //体育
                HomeTopView.OkSport->{
                    ivSportCover.setImageResource(R.drawable.img_sports)
                    //判断体育维护是否开启
                    if(getSportEnterIsClose()){
                        //展示维护中
                        tvSportClose.visible()
                        tvSportClose.text=context.getString(R.string.N257)
                    }else{
                        tvSportClose.gone()
                    }
                }
                //okgame
                HomeTopView.OkGame->{
                    ivSportCover.setImageResource(R.drawable.img_okgames)
                    tvSportClose.gone()
                }
                //bingo
                HomeTopView.OkBingo->{
                    ivSportCover.setImageResource(R.drawable.img_okbingo)
                    tvSportClose.visible()
                    tvSportClose.text=context.getString(R.string.N700)
                }
                //oklive
                HomeTopView.OkLive->{
                    ivSportCover.setImageResource(R.drawable.img_oklive)
                    tvSportClose.visible()
                    tvSportClose.text=context.getString(R.string.N700)
                }
            }
        }
    }
}