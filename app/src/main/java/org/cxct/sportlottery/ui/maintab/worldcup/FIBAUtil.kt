package org.cxct.sportlottery.ui.maintab.worldcup

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.net.sport.SportRepository
import org.cxct.sportlottery.repository.StaticData

object FIBAUtil {

    private val fibaCode = "sc:fiba"
    private var isLoading = false
    private var fibaItem: FIBAItem? = null

    fun preloadLoad() {

        if (!StaticData.worldCupOpened()
            || fibaItem != null
            || isLoading) {
            return
        }

        isLoading = true
        GlobalScope.launch {
            callApi({ SportRepository.getCouponMenu() }) {
                isLoading = false
                val list = it.getData()
                if (list.isNullOrEmpty()) {
                    return@callApi
                }

                list.find { fibaCode == it.couponCode }?.let {
                    if (!it.icon.isEmptyStr() && !it.couponCode.isEmptyStr() && !it.couponName.isEmptyStr()) {
                        fibaItem = FIBAItem(it.icon!!, it.couponCode!!, it.couponName!!, it.num, it.sort)
                    }
                }
            }
        }
    }

    fun takeFIBAItem(): FIBAItem? {
        if (fibaItem == null) {
            preloadLoad()
        }
        return fibaItem
    }

}