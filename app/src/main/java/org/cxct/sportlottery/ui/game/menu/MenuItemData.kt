package org.cxct.sportlottery.ui.game.menu

data class MenuItemData(
    val imgId: Int,
    val title: String,
    val gameType: String,
    var isSelected: Int //0:沒被置頂 ; 1:置頂
) {
    var selectedSortNum = 0
}
