package org.cxct.sportlottery.net.point.data

import android.os.Parcelable
import com.stx.xhb.androidx.entity.BaseBannerInfo
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.profileCenter.pointshop.LimitedProductItem
import org.cxct.sportlottery.ui.profileCenter.pointshop.ProductItem
import org.cxct.sportlottery.ui.profileCenter.pointshop.ProductType
import org.cxct.sportlottery.ui.profileCenter.pointshop.ProductValueType

/**
 * 實作[BaseBannerInfo]介面, 用於積分商城主頁[PointShopActivity][org.cxct.sportlottery.ui.profileCenter.pointshop.PointShopActivity]限時搶購商品自動輪播
 * 實作[ProductItem]介面, 用於普通商品
 * 實作[LimitedProductItem]介面, 用於限時搶購商品
 */
@KeepMembers
@Parcelize
data class Product(
    val cateId: String, //產品分類,可以多個分類
    val desc: String,
    override val discount: Double,
    val id: Int,
    val imgList: List<Image>? = null,
    val isLimitSale: Int,
    val limitSaleEnd: Long,
    val limitSaleStart: Long,
    val memberLimit: Int,
    override val name: String,
    val points: Int, //點數 (原價)
    val price: Double, //價格 (彩金價值)
    val realPoints: Int, //實際點數 (打折後價格)
    val remark: String,
    val stock: Int,
    /**
     * 状态 1現金，2：實物
     */
    val type: Int,
    val validCheckPercentage: Int,//流水倍数
    val purchasedQuantity: Int,//已購買數量
    override val sort: Int
) : BaseBannerInfo, ProductItem, LimitedProductItem, Parcelable {
    override fun getXBannerUrl(): Any {
        return ""
    }

    override fun getXBannerTitle(): String {
        return ""
    }

    override val productType: ProductType?
        get() = ProductType.toEnum(type)
    override val valueType: ProductValueType
        get() = ProductValueType.POINT //目前只能以積分購買
    override val productPrice: Double
        get() = realPoints.toDouble()
    override val fundValue: Double
        get() = price
    override val originalPrice: Double
        get() = points.toDouble()
    override val storage: Int
        get() = stock
    override val imageUrl: String
        get() {
            val firstImagePath = imgList?.firstOrNull { it.path.isNotEmpty() }?.path
            return if (firstImagePath.isNullOrEmpty()) "" else "${sConfigData?.resServerHost}$firstImagePath"
        }
    override val startDate: Long
        get() = limitSaleStart
    override val endDate: Long
        get() = limitSaleEnd

    /**
     * 限时列表第一排序规则
     * 抢购中的商品>未开始抢购商品>无库存商品
     */
    val limitFirstSort
       get() = when{
            storage == 0 ->3
            limitSaleStart < System.currentTimeMillis() -> 1
            else -> 2
        }

    /**
     *  限时列表第2排序规则
     *  抢购中的商品: 倒计时越短，排序越靠前
     */
    val limitSecondSort
      get() = when{
        storage == 0 -> -1
        limitSaleStart < System.currentTimeMillis()-> System.currentTimeMillis()-limitSaleStart
        else -> limitSaleEnd - System.currentTimeMillis()
    }

    /**
     * 商品是否限额购买
     */
    fun isLimitBuy() = memberLimit in 1..purchasedQuantity


}