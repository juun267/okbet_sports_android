package org.cxct.sportlottery.ui.sport

import android.app.Application
import androidx.lifecycle.LiveData
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.sport.SportRepository
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.ui.sport.list.SportListViewModel

class SportTabViewModel(
    androidContext: Application
) : SportListViewModel(
    androidContext
) {

    val sportMenuResult: LiveData<ApiResult<SportMenuData>>
        get() = SportRepository._sportMenuResultEvent

    private var lastMenuTag = 0L
    private var menuLoading = false
    fun getSportMenuData(isNew: Boolean?=null) {
        if (menuLoading || System.currentTimeMillis() - lastMenuTag < 10_000) {
            return
        }
        menuLoading = true
        callApi({
            SportRepository.getSportMenu(isNew)
        }) {
            if (it.succeeded()) {
                it.getData()?.sortSport()
//                it.getData()?.makeEsportCategoryItem()
                SportRepository._sportMenuResultEvent.postValue(it)     // 更新大廳上方球種數量、各MatchType下球種和數量
            }
            menuLoading = false
            lastMenuTag = System.currentTimeMillis()
        }
    }

    private fun SportMenuData.sortSport(): SportMenuData {
        this.menu.inPlay.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.today.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.early.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.cs.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.parlay.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.outright.items.sortedBy { sport ->
            sport.sortNum
        }
        this.atStart.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.eps?.items?.sortedBy { sport ->
            sport.sortNum
        }

        return this
    }
}