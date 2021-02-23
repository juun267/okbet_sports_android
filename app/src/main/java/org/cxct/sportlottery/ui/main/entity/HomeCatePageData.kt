package org.cxct.sportlottery.ui.main.entity

class HomeCatePageData(var category: MainCategory,
                       var tabPageDataList: MutableList<HomeTabPageData> = mutableListOf(),
                       var isShowTabLayout: Boolean = true) {

    var currentTabPosition = 0
}