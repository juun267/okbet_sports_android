package org.cxct.sportlottery.ui.main.entity

//imageStyleLayout: true: 只顯示圖片 false:舊的風格(白底，有格線)
class HomeTabPageData(val tabTitle: String?,
                      var gameList: MutableList<HomeGameItemData>?,
                      var imageStyleLayout: Boolean = false,
                      val iconUrl: String? = null) {// val iconCode: String? = null

    fun clone(): HomeTabPageData {
        val cloneList = mutableListOf<HomeGameItemData>()
        gameList?.forEach { cloneList.add(it) }
        return HomeTabPageData(tabTitle, cloneList, imageStyleLayout, iconUrl)
    }
}