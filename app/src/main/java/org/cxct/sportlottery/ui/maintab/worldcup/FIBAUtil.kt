package org.cxct.sportlottery.ui.maintab.worldcup

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.net.sport.SportRepository
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LogUtil

object FIBAUtil {
    const val fibaCode = "sc:fiba"
    private var isLoading = false
    private var fibaItem: FIBAItem? = null
    init {
        LanguageManager.addLanguageChangedListener { _, _ ->
            isLoading = false
            fibaItem = null
            preloadLoad()
        }
    }

    fun preloadLoad() {

        if (!StaticData.worldCupOpened()
            || fibaItem != null
            || isLoading) {
            return
        }

        isLoading = true
        val language = LanguageManager.getSelectLanguage(MultiLanguagesApplication.appContext)
        GlobalScope.launch {
            callApi({ SportRepository.getCouponMenu() }) {
                isLoading = false
                if (language != LanguageManager.getSelectLanguage(MultiLanguagesApplication.appContext)) {
                    return@callApi
                }
                val list = it.getData()
                if (list.isNullOrEmpty()) {
                    return@callApi
                }

                list.find { fibaCode == it.couponCode }?.let {
                    if (!it.icon.isEmptyStr() && !it.couponCode.isEmptyStr() && !it.couponName.isEmptyStr()) {
                        fibaItem = FIBAItem(it.icon!!, it.couponCode!!, it.couponName!!, it.num, it.sort)
                        LogUtil.toJson(fibaItem)
                    }
                }
            }
        }
    }

    fun takeFIBAItem(): FIBAItem? {
        if (fibaItem == null) {
            preloadLoad()
        } else {
            return fibaItem
        }
        return null
    }
    fun enableFiba(): Boolean {
        return StaticData.worldCupOpened()&&fibaItem!=null
    }

}