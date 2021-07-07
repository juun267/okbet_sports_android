package org.cxct.sportlottery.ui.game.menu

data class MenuItemData(
    val imgId:Int,
    val title:String,
    val sportType:String,
    var isSelected:Int //0:沒被置頂 ; 1:置頂
)
