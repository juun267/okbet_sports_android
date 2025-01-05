package org.cxct.sportlottery.net.point.api

import com.google.gson.JsonObject
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.point.data.PointBill
import org.cxct.sportlottery.net.point.data.Product
import org.cxct.sportlottery.net.point.data.ProductIndex
import org.cxct.sportlottery.net.point.data.ProductRedeem
import org.cxct.sportlottery.network.Constants
import retrofit2.http.*


@JvmSuppressWildcards
interface PointApi {

    @GET(Constants.POINT_BILL)
    suspend fun getPointBill(@QueryMap queryMap: Map<String,Any>
    ): ApiResult<List<PointBill>>

    @GET(Constants.PRODUCT_DETAIL)
    suspend fun getProductDetail(@Query("productId") productId: Int
    ): ApiResult<Product>

    @GET(Constants.PRODUCT_INDEX)
    suspend fun getProductIndex(@Query("productType") device: Int
    ): ApiResult<ProductIndex>

    @GET(Constants.PRODUCT_GUEST_INDEX)
    suspend fun getProductGuestIndex(@Query("productType") device: Int
    ): ApiResult<ProductIndex>

    @POST(Constants.PRODUCT_REDEEM)
    suspend fun postProductRedeem(@Body params: JsonObject
    ): ApiResult<String>

    @GET(Constants.PRODUCT_REDEEM_LIST)
    suspend fun getProductRedeemList(@QueryMap queryMap: Map<String,Any>
    ): ApiResult<List<ProductRedeem>>
}