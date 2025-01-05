package org.cxct.sportlottery.net.point.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class ProductRedeem(
    val addTime: Long,
    val basicRate: Int,
    val count: Int,
    val courier: String?,
    val courierOrder: String?,
    val currency: String?,
    val customerAddress: String?,
    val customerName: String?,
    val customerPhone: String?,
    val discount: Int?,
    val id: Int,
    val platformId: Int,
    val points: Int,
    val price: Int,
    val priceBasic: Int,
    val productId: Int,
    val productName: String,
    val productType: Int,
    val remark: String?,
    val status: Int,//状态 0：待審核，1：審核通過, 2：審核未通過 3：未發貨 4：已發貨
    val totalPoints: Int,
    val updateTime: String?,
    val updateUser: String?,
    val imageUrl: String?,
    val orderNo: String?,
):Parcelable