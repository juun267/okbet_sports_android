package org.cxct.sportlottery.net.point

import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.net.point.api.PointApi
import org.cxct.sportlottery.net.point.data.PointBill
import org.cxct.sportlottery.net.point.data.PointRule
import org.cxct.sportlottery.net.point.data.Product
import org.cxct.sportlottery.net.point.data.ProductIndex
import org.cxct.sportlottery.net.point.data.ProductRedeem
import org.cxct.sportlottery.repository.sConfigData

object PointRepository {
    private val _pointRule = MutableStateFlow<PointRule?>(null)
    val pointRule = _pointRule.asStateFlow()

    private val _userPoint = MutableStateFlow<Int?>(null)
    val userPoint = _userPoint.asStateFlow()

    private val _blocked = MutableStateFlow<Int>(0)
    val blocked = _blocked.asStateFlow()

    private val pointApi: PointApi by lazy {
        RetrofitHolder.createApiService(PointApi::class.java)
    }

    suspend fun getPointBill(page: Int, pageSize: Int, pointQueryType:Int?, startTime: Long, endTime: Long): ApiResult<List<PointBill>> {
        val queryMap = mutableMapOf<String, Any>().apply {
            put("page",page)
            put("pageSize",pageSize)
            pointQueryType?.let { put("pointQueryType", it) }
            put("startTime",startTime)
            put("endTime",endTime)
        }
        return pointApi.getPointBill(queryMap)
    }
    suspend fun getPointDetail(productId: Int): ApiResult<Product> {
        return pointApi.getProductDetail(productId)
    }
    suspend fun getPointIndex(): ApiResult<ProductIndex> {
        return pointApi.getProductIndex(2) //後端要求該API帶2
    }
    suspend fun getPointGuestIndex(): ApiResult<ProductIndex> {
        return pointApi.getProductGuestIndex(2) //後端要求該API帶2
    }
    suspend fun postProductRedeem(productId: Int, customerName: String?, customerPhone: String?, customerAddress: String?, count: Int): ApiResult<String> {
        val params = JsonObject()
        params.addProperty("productId", productId)
        params.addProperty("customerAddress", customerAddress)
        params.addProperty("customerName", customerName)
        params.addProperty("customerPhone", customerPhone)
        params.addProperty("count", count)
        return pointApi.postProductRedeem(params)
    }
    suspend fun getProductRedeemList(page: Int,
                                     pageSize: Int,
                                     status: Int?=null,
                                     startTime: Long,
                                     endTime: Long
    ): ApiResult<List<ProductRedeem>> {
        val queryMap = mutableMapOf<String, Any>().apply {
            put("page",page)
            put("pageSize",pageSize)
            status?.let { put("status", it) }
            put("startTime",startTime)
            put("endTime",endTime)
        }
        return pointApi.getProductRedeemList(queryMap)
    }

    suspend fun postPointRule(pointRule: PointRule?) {
        _pointRule.emit(pointRule)
    }
    suspend fun postUserPoint(userPoint: Int?) {
        _userPoint.emit(userPoint)
    }
    suspend fun postBlocked(blocked: Int) {
        _blocked.emit(blocked)
    }

}