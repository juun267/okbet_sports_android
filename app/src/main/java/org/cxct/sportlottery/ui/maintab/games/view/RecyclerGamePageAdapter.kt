package org.cxct.sportlottery.ui.maintab.games.view

import android.R.attr.endColor
import android.R.attr.startColor
import android.graphics.LinearGradient
import android.graphics.Shader
import android.opengl.ETC1.getHeight
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import eightbitlab.com.blurview.BlurView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ItemGamePageBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.view.onClick
import org.cxct.sportlottery.view.setTextColorGradient


class RecyclerGamePageAdapter:
    BindingAdapter<List<OKGameBean>, ItemGamePageBinding>()  {

    private var onFavoriteClick: (item:OKGameBean) -> Unit = {}
    private var onGameClick: (item:OKGameBean) -> Unit = {}
    private var onJumpToMore: () -> Unit = {}
    var isSinglePage=false
    private var isMoreThan18:Boolean=false
    //是否显示收藏
    private var isShowCollect:Boolean=true

    //是否显示收藏按钮
    fun setIsShoeCollect(flag:Boolean){
        isShowCollect=flag
    }
    //点击收藏
    fun setOnFavoriteClick(block:(item:OKGameBean)->Unit){
        onFavoriteClick=block
    }
    //点击更多
    fun setOnJumpToMore(block:()->Unit){
        onJumpToMore=block
    }
    //点击游戏
    fun setOnGameClick(block:(item:OKGameBean)->Unit){
        onGameClick=block
    }

    fun setIsMoreThan(flag:Boolean){
        isMoreThan18=flag
    }

    fun bindLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        ServiceBroadcastReceiver.thirdGamesMaintain.collectWith(lifecycleOwner.lifecycleScope) { gamesMaintain ->
            data.forEachIndexed { index, okGameBeans ->
                val changedPosition = mutableListOf<Pair<Int, OKGameBean>>()

                okGameBeans.forEachIndexed { position, gameBean->
                    if (gameBean.isMaintain() != (gamesMaintain.maintain.toInt() == 1) && (gameBean.firmType == gamesMaintain.firmType)) {
                        gameBean.maintain = gamesMaintain.maintain.toInt()
                        changedPosition.add(Pair(position, gameBean))
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
        binding: ItemGamePageBinding,
        item: List<OKGameBean>,
        payloads: List<Any>
    ) {
        payloads.forEach {
            if (it is MutableList<*>) {
                it.forEach { pair->
                    val index = (pair as Pair<Int, OKGameBean>).first
                    val okGameBean = pair.second
                    when(index) {
                        0 -> {
                            binding.tvCover1.isVisible = okGameBean.isMaintain()
                        }

                        1 -> {
                            binding.tvCover3.isVisible = okGameBean.isMaintain()
                        }

                        2 -> {
                            binding.tvCover5.isVisible = okGameBean.isMaintain()
                        }

                        3 -> {
                            binding.tvCover2.isVisible = okGameBean.isMaintain()
                        }

                        4 -> {
                            binding.tvCover4.isVisible = okGameBean.isMaintain()
                        }

                        5 -> {
                            val isMoreItem = position == dataCount() - 1 && isMoreThan18
                            binding.tvCover6.isVisible = !isMoreItem && okGameBean.isMaintain()
                        }
                    }
                }
            }
        }
    }

    override fun onBinding(position: Int, binding: ItemGamePageBinding, item: List<OKGameBean>) = binding.run {

        if (isShowCollect) {
            ivFav1.visible()
            ivFav2.visible()
            ivFav3.visible()
            ivFav4.visible()
            ivFav5.visible()
            ivFav6.visible()
        } else {
            ivFav1.gone()
            ivFav2.gone()
            ivFav3.gone()
            ivFav4.gone()
            ivFav5.gone()
            ivFav6.gone()
        }
        setPotView(binding,item)
        when(item.size){
            1-> {
                bindItem(item[0], cardGame1, ivCover1, tvName1, tvFirmName1, ivFav1, tvCover1)
                cardGame2.gone()
                cardGame3.gone()
                cardGame4.gone()
                cardGame5.gone()
                cardGame6.gone()
            }

            2-> {
                bindItem(item[0], cardGame1, ivCover1, tvName1, tvFirmName1, ivFav1, tvCover1)
                bindItem(item[1], cardGame3, ivCover3, tvName3, tvFirmName3, ivFav3, tvCover3)
                cardGame2.gone()
                cardGame4.gone()
                cardGame5.gone()
                cardGame6.gone()
            }

            3-> {
                bindItem(item[0], cardGame1, ivCover1, tvName1, tvFirmName1, ivFav1, tvCover1)
                bindItem(item[1], cardGame3, ivCover3, tvName3, tvFirmName3, ivFav3, tvCover3)
                bindItem(item[2], cardGame5, ivCover5, tvName5, tvFirmName5, ivFav5, tvCover5)
                cardGame2.gone()
                cardGame4.gone()
                cardGame6.gone()
            }

            4-> {
                bindItem(item[0], cardGame1, ivCover1, tvName1, tvFirmName1, ivFav1, tvCover1)
                bindItem(item[1], cardGame3, ivCover3, tvName3, tvFirmName3, ivFav3, tvCover3)
                bindItem(item[2], cardGame5, ivCover5, tvName5, tvFirmName5, ivFav5, tvCover5)
                bindItem(item[3], cardGame2, ivCover2, tvName2, tvFirmName2, ivFav2, tvCover2)
                cardGame4.gone()
                cardGame6.gone()
            }

            5-> {
                bindItem(item[0], cardGame1, ivCover1, tvName1, tvFirmName1, ivFav1, tvCover1)
                bindItem(item[1], cardGame3, ivCover3, tvName3, tvFirmName3, ivFav3, tvCover3)
                bindItem(item[2], cardGame5, ivCover5, tvName5, tvFirmName5, ivFav5, tvCover5)
                bindItem(item[3], cardGame2, ivCover2, tvName2, tvFirmName2, ivFav2, tvCover2)
                bindItem(item[4], cardGame4, ivCover4, tvName4, tvFirmName4, ivFav4, tvCover4)
                cardGame6.gone()
            }

            6-> {
                val isMoreItem = position == dataCount() - 1 && isMoreThan18
                bindItem(item[0], cardGame1, ivCover1, tvName1, tvFirmName1, ivFav1, tvCover1)
                bindItem(item[1], cardGame3, ivCover3, tvName3, tvFirmName3, ivFav3, tvCover3)
                bindItem(item[2], cardGame5, ivCover5, tvName5, tvFirmName5, ivFav5, tvCover5)
                bindItem(item[3], cardGame2, ivCover2, tvName2, tvFirmName2, ivFav2, tvCover2)
                bindItem(item[4], cardGame4, ivCover4, tvName4, tvFirmName4, ivFav4, tvCover4)
                bindItem(item[5], cardGame6, ivCover6, tvName6, tvFirmName6, ivFav6, tvCover6, isMoreItem)
                if(isMoreItem) {
                    blurCard6.visible()
                    blurCard6.setupWith(binding.root)
                        .setFrameClearDrawable(binding.root.background)
                        .setBlurRadius(1.3f)
                } else {
                    blurCard6.gone()
                }
                blurCard6.onClick { onJumpToMore() }
            }
        }

    }




    private fun bindItem(item: OKGameBean,
                         cardGame: View,
                         ivCover: ImageView,
                         tvName: TextView,
                         tvFirmName: TextView,
                         ivFav: ImageView,
                         tvCover: View,
                         moreItem: Boolean = false) {

        cardGame.visible()
        cardGame.onClick { if (!tvCover.isVisible) { onGameClick(item) } }
        ivCover.load(item.imgGame, R.drawable.ic_okgames_nodata)
        tvName.text = item.gameName
        tvFirmName.text = item.firmName
        ivFav.isSelected = item.markCollect
        ivFav.isEnabled = !item.isMaintain()
        //收藏点击
        ivFav.onClick {
            ivFav.animDuang(1.3f)
            onFavoriteClick(item)
        }
        tvCover.isVisible = !moreItem && item.isMaintain()
    }



    private fun setPotView(binding: ItemGamePageBinding,item: List<OKGameBean>){
//        binding.blurBottom1.gone()
//        binding.blurBottom2.gone()
//        binding.blurBottom3.gone()
//        binding.blurBottom4.gone()
//        binding.blurBottom5.gone()
//        binding.blurBottom6.gone()
//        binding.tvPot1.gone()
//        binding.tvPot2.gone()
//        binding.tvPot3.gone()
//        binding.tvPot4.gone()
//        binding.tvPot5.gone()
//        binding.tvPot6.gone()
//        bindItem(item[1], cardGame3, ivCover3, tvName3, tvFirmName3, ivFav3, tvCover3)
//        bindItem(item[2], cardGame5, ivCover5, tvName5, tvFirmName5, ivFav5, tvCover5)
//        bindItem(item[3], cardGame2, ivCover2, tvName2, tvFirmName2, ivFav2, tvCover2)
//        bindItem(item[4], cardGame4, ivCover4, tvName4, tvFirmName4, ivFav4, tvCover4)
        when(item.size){
            1->{
                initPotData(binding.blurBottom1,binding.tvPot1,binding,item[0])
            }
            2->{
                initPotData(binding.blurBottom1,binding.tvPot1,binding,item[0])
                initPotData(binding.blurBottom3,binding.tvPot3,binding,item[1])
            }
            3->{
                initPotData(binding.blurBottom1,binding.tvPot1,binding,item[0])
                initPotData(binding.blurBottom3,binding.tvPot3,binding,item[1])
                initPotData(binding.blurBottom5,binding.tvPot5,binding,item[2])
            }
            4->{
                initPotData(binding.blurBottom1,binding.tvPot1,binding,item[0])
                initPotData(binding.blurBottom3,binding.tvPot3,binding,item[1])
                initPotData(binding.blurBottom5,binding.tvPot5,binding,item[2])
                initPotData(binding.blurBottom2,binding.tvPot2,binding,item[3])
            }
            5->{
                initPotData(binding.blurBottom1,binding.tvPot1,binding,item[0])
                initPotData(binding.blurBottom3,binding.tvPot3,binding,item[1])
                initPotData(binding.blurBottom5,binding.tvPot5,binding,item[2])
                initPotData(binding.blurBottom2,binding.tvPot2,binding,item[3])
                initPotData(binding.blurBottom4,binding.tvPot4,binding,item[4])
            }
            6->{
                initPotData(binding.blurBottom1,binding.tvPot1,binding,item[0])
                initPotData(binding.blurBottom3,binding.tvPot3,binding,item[1])
                initPotData(binding.blurBottom5,binding.tvPot5,binding,item[2])
                initPotData(binding.blurBottom2,binding.tvPot2,binding,item[3])
                initPotData(binding.blurBottom4,binding.tvPot4,binding,item[4])
                initPotData(binding.blurBottom6,binding.tvPot6,binding,item[5])
            }
        }

    }

    private  fun  initPotData(blur:BlurView,textView: TextView,binding: ItemGamePageBinding,item: OKGameBean){
        //关闭显示 ==0
       if(item.jackpotOpen==0){
           blur.gone()
           textView.gone()
       }else{
           //开启显示  ==1
           blur.visible()
           textView.visible()
       }

        blur.setupWith(binding.root)
            .setFrameClearDrawable(binding.root.background)
            .setBlurRadius(1.3f)
        textView.setTextColorGradient()
        textView.text="$showCurrencySign ${TextUtil.formatMoney(item.jackpotAmount)}"
    }
}