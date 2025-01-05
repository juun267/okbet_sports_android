package org.cxct.sportlottery.ui.profileCenter.pointshop

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.toast
import org.cxct.sportlottery.net.point.PointRepository
import kotlinx.coroutines.launch
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.point.data.*
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.profileCenter.pointshop.announcement.AnnouncementItem
import org.cxct.sportlottery.ui.profileCenter.pointshop.main.ShopMainViewEvent
import org.cxct.sportlottery.ui.profileCenter.pointshop.main.ShopSortedNormalTab
import org.cxct.sportlottery.ui.profileCenter.pointshop.main.ShopSortedPricesTab
import org.cxct.sportlottery.ui.profileCenter.pointshop.main.ShopTypeFilterTabImpl
import org.cxct.sportlottery.ui.profileCenter.pointshop.main.ShopTypeTab
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.encodeUserName

class PointShopViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {

    private val _expiredPoints = MutableStateFlow<ExpiredPoints?>(null)
    val expiredPoints = _expiredPoints.asStateFlow()

    private val _bannerImageList = MutableStateFlow<List<Image>>(listOf())
    val bannerImageList = _bannerImageList.asStateFlow()

    /**
     * 積分商城公告物件[AnnouncementItem]列表
     */
    private val _announcementItemList = MutableStateFlow<List<AnnouncementItem>>(listOf())
    val announcementItem = _announcementItemList.asStateFlow()

    /**
     * 限時搶購商品物件列表
     */
    private val _limitedProductItemList = MutableStateFlow<List<Product>>(listOf())
    val limitedProductItemList = _limitedProductItemList.asStateFlow()

    /**
     * 積分商城主頁View事件[ShopMainViewEvent]通知
     */
    private val _shopMainViewEvent = MutableSharedFlow<ShopMainViewEvent>()
    val shopMainViewEvent = _shopMainViewEvent.asSharedFlow()

    /**
     * 積分商城分類Tab[ShopTypeTab]清單
     */
    private val _shopTypeTabList = MutableStateFlow<List<ShopTypeTab>>(listOf())
    val shopTypeTabList = _shopTypeTabList.asStateFlow()

    private val _shopTypeFilterTabList = MutableStateFlow(
        ShopTypeFilterType.values().map {
            when (it) {
                ShopTypeFilterType.RECOMMEND -> ShopSortedNormalTab(tabType = it, isSelected = true)
                ShopTypeFilterType.PRICES -> ShopSortedPricesTab(tabType = it)
                else -> ShopSortedNormalTab(tabType = it)
            }
        }
    )
    val shopTypeFilterTabList = _shopTypeFilterTabList.asStateFlow()

    /**
     * 商品總清單
     */
    private var mShopProductItemList: List<Product> = listOf()

    /**
     * 當前選中的商城類別, 篩選器過濾後的商品清單
     */
    private val _shopProductItemList = MutableStateFlow<List<Product>>(listOf())
    val shopProductItemList = _shopProductItemList.asStateFlow()

    private val _redeemResult = MutableSharedFlow<ApiResult<String>>()
    val redeemResult = _redeemResult.asSharedFlow()

    private val _redeemList = MutableSharedFlow<ApiResult<List<ProductRedeem>>>()
    val redeemList = _redeemList.asSharedFlow()

    private val _pointBillList = MutableSharedFlow<ApiResult<List<PointBill>>>()
    val pointBillList = _pointBillList.asSharedFlow()

    /**
     * 限時搶購排序
     * 抢购中的商品>未开始抢购商品>无库存商品
     * 抢购中的商品: 倒计时越短，排序越靠前
     * 若前兩項皆相同, 排序值[ProductItem.sort]越小，排序越靠前
     */
    private fun List<Product>.sortedLimitedProductItemList(): List<Product> {
        return this.sortedWith(compareBy<Product> {
            it.limitFirstSort
        }.thenBy {
            it.limitSecondSort
        }.thenBy {
            it.sort
        })
    }

    /**
     * 積分商城主頁API
     */
    fun getProductIndex() {
        callApi({
            if(LoginRepository.isLogined())
                PointRepository.getPointIndex()
            else
                PointRepository.getPointGuestIndex()
        }) {
            if (it.succeeded()) {
                val apiData = it.getData()
                //是否黑名单
                viewModelScope.launch {
                    PointRepository.postBlocked(apiData?.blocked?: 0)
                }
                //用戶積分
                viewModelScope.launch {
                    PointRepository.postUserPoint(apiData?.points?: 0)
                }

                //過期點數資訊
                viewModelScope.launch {
                    _expiredPoints.emit(apiData?.expiredPoints)
                }

                //積分規則設定
                viewModelScope.launch {
                    PointRepository.postPointRule(apiData?.pointRule)
                }

                //輪播圖
                viewModelScope.launch {
                    _bannerImageList.emit(apiData?.images?.sortedBy { image -> image.sort }
                        ?: listOf())
                }

                //region 商品分類列
                //預設添加分類「全部」
                val newShopTypeTabList = mutableListOf(ShopTypeTab(
                    typeName = "",
                    isAllType = true,
                    typeCode = -1
                ).apply {
                    isSelected = true
                })

                newShopTypeTabList.addAll(apiData?.productCateVO?.map { productCate ->
                    ShopTypeTab(
                        typeName = productCate.name,
                        typeCode = productCate.id
                    )
                } ?: listOf())
                viewModelScope.launch {
                    _shopTypeTabList.emit(newShopTypeTabList)
                }
                //endregion 商品分類列
                //限時商品
                viewModelScope.launch {
                    _limitedProductItemList.emit(apiData?.limitProducts?: listOf())
                }

                //公告欄
                viewModelScope.launch {
                    _announcementItemList.emit(
                        apiData?.redeemNotifies?.map { notify ->
                            AnnouncementItem(
                                userName = notify.userName.encodeUserName(),
                                exchangeContent = "${notify.productName}"
                            )
                        } ?: listOf()
                    )
                }

                //暫存總商品清單
                mShopProductItemList = apiData?.products ?: listOf()
                updateSelectedTypeProduct(newShopTypeTabList.firstOrNull())
            } else {
                toast(it.msg)
            }
        }
    }

    /**
     * 選中商店類別[ShopTypeTab]
     */
    fun selectShopTypeTab(tab: ShopTypeTab) {
        viewModelScope.launch {
            shopTypeTabList.value.forEach {
                it.isSelected = it == tab
            }

            _shopMainViewEvent.emit(ShopMainViewEvent.ShopTypeTabSelectedUpdate)
        }

        updateSelectedTypeProduct(selectedTab = tab)
    }

    /**
     * 選中商店類別篩選器[ShopTypeFilterType]
     */
    fun selectShopTypeFilterTab(filterTabType: ShopTypeFilterType) {
        val nowSelectedFilterTab = shopTypeFilterTabList.value.firstOrNull { it.isSelected }
        when (filterTabType) {
            ShopTypeFilterType.CAN_BUY,
            ShopTypeFilterType.RECOMMEND -> {
                shopTypeFilterTabList.value.forEach {
                    it.isSelected = it.tabType == filterTabType
                }
            }

            ShopTypeFilterType.PRICES -> {
                when (nowSelectedFilterTab) {
                    is ShopSortedPricesTab -> {
                        nowSelectedFilterTab.isAscending = !nowSelectedFilterTab.isAscending
                    }

                    else -> {
                        shopTypeFilterTabList.value.forEach {
                            it.isSelected = it.tabType == filterTabType
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            _shopMainViewEvent.emit(
                ShopMainViewEvent.ShopTypeFilterTabSelectedUpdate(
                    shopTypeFilterTabList.value
                )
            )

            updateSelectedTypeProduct()
        }
    }


    /**
     * 根據當前選中的商品類別進行過濾商品清單[mShopProductItemList]
     * 並根據類別篩選器[ShopTypeFilterType]進行排序
     */
    private fun updateSelectedTypeProduct(
        selectedTab: ShopTypeTab? = shopTypeTabList.value.firstOrNull { it.isSelected },
        selectedFilterType: ShopTypeFilterTabImpl? = shopTypeFilterTabList.value.firstOrNull { it.isSelected }
    ) {
        val typeProductList =
            if (selectedTab?.isAllType == true) {
                mShopProductItemList
            } else {
                mShopProductItemList.filter {
                    it.cateId.split(",").contains(selectedTab?.typeCode?.toString())
                }
            }.toMutableList()

        when (selectedFilterType) {
            //價格排序
            is ShopSortedPricesTab -> {
                if (selectedFilterType.isAscending) {
                    typeProductList.sortBy { it.realPoints }
                } else {
                    typeProductList.sortByDescending { it.realPoints }
                }
            }

            //其他排序
            is ShopSortedNormalTab -> {
                //我能購買篩選過濾
                if (selectedFilterType.tabType == ShopTypeFilterType.CAN_BUY) {
                    //移除 无库存/余额不足/超过限购 的商品
                    typeProductList.removeIf { it.stock <= 0
                            || it.realPoints > (PointRepository.userPoint.value?: 0)
                            || it.isLimitBuy() }
                }
            }
        }
        viewModelScope.launch {
            _shopProductItemList.emit(typeProductList)
        }
    }

    /**
     * 积分兑换
     */
    fun redeemProduct(productId: Int, customerName: String?, customerPhone: String?, customerAddress: String?, count: Int){
        callApi({PointRepository.postProductRedeem(productId,customerName,customerPhone,customerAddress,count)}){
            viewModelScope.launch {
                _redeemResult.emit(it)
            }
        }
    }

    /**
     * 获取兑换记录
     */
    fun getRedeemList(page: Int, status: Int?=null){
        val startTime = TimeUtil.getDefaultTimeStamp(30).startTime!!.toLong()
        val endTime = TimeUtil.getTodayEndTimeStamp()
        callApi({PointRepository.getProductRedeemList(page, 20 ,status = status,startTime, endTime)}){
            viewModelScope.launch {
                it.page = page
                _redeemList.emit(it)
            }
        }
    }
    /**
     * 获取积分历史纪录
     */
    fun getPointBill(page: Int, pointQueryType: Int?=null){
        val startTime = TimeUtil.getDefaultTimeStamp(30).startTime!!.toLong()
        val endTime = TimeUtil.getTodayEndTimeStamp()
        callApi({PointRepository.getPointBill(page, 20 ,pointQueryType = pointQueryType, startTime, endTime)}){
            viewModelScope.launch {
                it.page = page
                _pointBillList.emit(it)
            }
        }
    }

    /**
     * 清除已結束限時搶購商品
     * @see Product.isFinished
     */
    fun clearFinishedLimitedProduct() {
        viewModelScope.launch {
            val newLimitedProductItemList = limitedProductItemList.value.toMutableList()
            newLimitedProductItemList.removeIf { it.isFinished }
            _limitedProductItemList.emit(newLimitedProductItemList)
        }
    }


}