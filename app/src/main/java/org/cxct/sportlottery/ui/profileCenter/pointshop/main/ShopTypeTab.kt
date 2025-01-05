package org.cxct.sportlottery.ui.profileCenter.pointshop.main

/**
 * @param isAllType 是否為「全部」分類
 * @param typeName 分類名稱[org.cxct.sportlottery.net.point.data.ProductCateVO.name]
 * @param typeCode 分類Id[org.cxct.sportlottery.net.point.data.ProductCateVO.id]
 *
 * @property isSelected 積分商城主頁 - 商品分類Tab是否選中
 */
data class ShopTypeTab(val typeName: String, val isAllType: Boolean = false, val typeCode: Int) {
    var isSelected: Boolean = false
}
