package org.cxct.sportlottery.ui.maintab.games.view

import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.inVisible
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemGamePageBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.view.onClick

class RecyclerGamePageAdapter:
    BindingAdapter<List<OKGameBean>, ItemGamePageBinding>()  {
    private var onFavoriteClick:(item:OKGameBean)->Unit={}
    private var onGameClick:(item:OKGameBean)->Unit={}
    private var onJumpToMore:()->Unit={}
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
    override fun onBinding(position: Int, binding: ItemGamePageBinding, item: List<OKGameBean>) {
        if(isSinglePage){
            binding.run {
                when(item.size){
                    1->{
                        initItem1(this,item[0])
                        cardGame2.gone()
                        cardGame3.gone()
                        cardGame4.gone()
                        cardGame5.gone()
                        cardGame6.gone()
                    }
                    2->{
                        initItem1(this,item[0])
                        initItem2(this,item[1])
                        cardGame3.gone()
                        cardGame4.gone()
                        cardGame5.gone()
                        cardGame6.gone()
                    }
                    3->{
                        initItem1(this,item[0])
                        initItem2(this,item[1])
                        initItem3(this,item[2])
                        cardGame4.gone()
                        cardGame5.gone()
                        cardGame6.gone()
                    }
                    4->{
                        initItem1(this,item[0])
                        initItem2(this,item[1])
                        initItem3(this,item[2])
                        initItem4(this,item[3])
                        cardGame5.gone()
                        cardGame6.gone()
                    }
                    5->{
                        initItem1(this,item[0])
                        initItem2(this,item[1])
                        initItem3(this,item[2])
                        initItem4(this,item[3])
                        initItem5(this,item[4])
                        cardGame6.gone()
                    }
                    6->{
                        initItem1(this,item[0])
                        initItem2(this,item[1])
                        initItem3(this,item[2])
                        initItem4(this,item[3])
                        initItem5(this,item[4])
                        initItem6(this,item[5],position)
                    }
                }
            }
        }else{
            binding.run {
                when(item.size){
                    1->{
                        initItem1(this,item[0])
                        cardGame2.inVisible()
                        cardGame3.inVisible()
                        cardGame4.inVisible()
                        cardGame5.inVisible()
                        cardGame6.inVisible()
                    }
                    2->{
                        initItem1(this,item[0])
                        initItem2(this,item[1])
                        cardGame3.inVisible()
                        cardGame4.inVisible()
                        cardGame5.inVisible()
                        cardGame6.inVisible()
                    }
                    3->{
                        initItem1(this,item[0])
                        initItem2(this,item[1])
                        initItem3(this,item[2])
                        cardGame4.inVisible()
                        cardGame5.inVisible()
                        cardGame6.inVisible()
                    }
                    4->{
                        initItem1(this,item[0])
                        initItem2(this,item[1])
                        initItem3(this,item[2])
                        initItem4(this,item[3])
                        cardGame5.inVisible()
                        cardGame6.inVisible()
                    }
                    5->{
                        initItem1(this,item[0])
                        initItem2(this,item[1])
                        initItem3(this,item[2])
                        initItem4(this,item[3])
                        initItem5(this,item[4])
                        cardGame6.inVisible()
                    }
                    6->{
                        initItem1(this,item[0])
                        initItem2(this,item[1])
                        initItem3(this,item[2])
                        initItem4(this,item[3])
                        initItem5(this,item[4])
                        initItem6(this,item[5],position)
                    }
                }
            }
        }


        binding.run {
            if(isShowCollect){
                ivFav1.visible()
                ivFav2.visible()
                ivFav3.visible()
                ivFav4.visible()
                ivFav5.visible()
                ivFav6.visible()
            }else{
                ivFav1.gone()
                ivFav2.gone()
                ivFav3.gone()
                ivFav4.gone()
                ivFav5.gone()
                ivFav6.gone()
            }
        }

    }


    private fun initItem1(binding: ItemGamePageBinding,item:OKGameBean){
        binding.run {
            cardGame1.visible()
            cardGame1.onClick {
                onGameClick(item)
            }
            ivCover1.load(item.imgGame, R.drawable.ic_okgames_nodata)
            tvName1.text = item.gameName
            tvFirmName1.text = item.firmName
            ivFav1.isSelected = item.markCollect
            //收藏点击
            ivFav1.onClick {
                ivFav1.animDuang(1.3f)
//                item.markCollect= !item.markCollect
                onFavoriteClick(item)
            }

            blurCard1.gone()
        }
    }



    private fun initItem2(binding: ItemGamePageBinding,item:OKGameBean){
        binding.run {
            cardGame2.visible()
            cardGame2.onClick {
                onGameClick(item)
            }
            ivCover2.load(item.imgGame, R.drawable.ic_okgames_nodata)
            tvName2.text = item.gameName
            tvFirmName2.text = item.firmName
            ivFav2.isSelected = item.markCollect
            //收藏点击
            ivFav2.onClick {
                ivFav2.animDuang(1.3f)
//                item.markCollect= !item.markCollect
                onFavoriteClick(item)
            }

            blurCard2.gone()
        }
    }



    private fun initItem3(binding: ItemGamePageBinding,item:OKGameBean){
        binding.run {
            cardGame3.visible()
            cardGame3.onClick {
                onGameClick(item)
            }
            ivCover3.load(item.imgGame, R.drawable.ic_okgames_nodata)
            tvName3.text = item.gameName
            tvFirmName3.text = item.firmName
            ivFav3.isSelected = item.markCollect
            //收藏点击
            ivFav3.onClick {
                ivFav3.animDuang(1.3f)
//                item.markCollect= !item.markCollect
                onFavoriteClick(item)
            }

            blurCard3.gone()
        }
    }





    private fun initItem4(binding: ItemGamePageBinding,item:OKGameBean){
        binding.run {
            cardGame4.visible()
            cardGame4.onClick {
                onGameClick(item)
            }
            ivCover4.load(item.imgGame, R.drawable.ic_okgames_nodata)
            tvName4.text = item.gameName
            tvFirmName4.text = item.firmName
            ivFav4.isSelected = item.markCollect
            //收藏点击
            ivFav4.onClick {
                ivFav4.animDuang(1.3f)
//                item.markCollect= !item.markCollect
                onFavoriteClick(item)
            }

            blurCard4.gone()
        }
    }



    private fun initItem5(binding: ItemGamePageBinding,item:OKGameBean){
        binding.run {
            cardGame5.visible()
            cardGame5.onClick {
                onGameClick(item)
            }
            ivCover5.load(item.imgGame, R.drawable.ic_okgames_nodata)
            tvName5.text = item.gameName
            tvFirmName5.text = item.firmName
            ivFav5.isSelected = item.markCollect
            //收藏点击
            ivFav5.onClick {
                ivFav5.animDuang(1.3f)
//                item.markCollect= !item.markCollect
                onFavoriteClick(item)
            }

            blurCard5.gone()
        }
    }



    private fun initItem6(binding: ItemGamePageBinding,item:OKGameBean,position:Int){
        binding.run {
            cardGame6.visible()
            cardGame6.onClick {
                onGameClick(item)
            }
            ivCover6.load(item.imgGame, R.drawable.ic_okgames_nodata)
            tvName6.text = item.gameName
            tvFirmName6.text = item.firmName
            ivFav6.isSelected = item.markCollect
            //收藏点击
            ivFav6.onClick {
                ivFav6.animDuang(1.3f)
//                item.markCollect= !item.markCollect
                onFavoriteClick(item)
            }

            if(position==2&&isMoreThan18){
                blurCard6.setupWith(binding.root)
                    .setFrameClearDrawable(binding.root.background)
                    .setBlurRadius(1.3f)
                blurCard6.visible()
            }else{
                blurCard6.gone()
            }
            blurCard6.onClick {
                onJumpToMore()
            }

        }
    }
}