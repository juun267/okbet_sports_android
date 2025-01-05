package org.cxct.sportlottery.ui.profileCenter.pointshop.main

/**
 * 積分商城主頁View互動事件
 */
sealed class ShopMainViewEvent {
    /**
     * 商城分類Tab選中更新
     */
    object ShopTypeTabSelectedUpdate : ShopMainViewEvent()

    class ShopTypeFilterTabSelectedUpdate(val tabList: List<ShopTypeFilterTabImpl>) :
        ShopMainViewEvent()
}