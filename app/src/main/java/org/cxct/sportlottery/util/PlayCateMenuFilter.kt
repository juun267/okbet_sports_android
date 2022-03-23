package org.cxct.sportlottery.util

import org.cxct.sportlottery.network.sport.SportMenuFilter

object PlayCateMenuFilter {
    var filterList : MutableMap<String?, MutableMap<String?, SportMenuFilter>?>? = mutableMapOf()

    fun filterOddsSort(sportType: String?, playCateMenuCode: String?): String? {
       return filterList?.get(sportType)?.get(playCateMenuCode)?.oddsSort
    }

    fun filterPlayCateNameMap(sportType: String?, playCateMenuCode: String?): MutableMap<String?, Map<String?, String?>?>? {
        return filterList?.get(sportType)?.get(playCateMenuCode)?.playCateNameMap
    }

    fun filterSelectablePlayCateNameMap(sportType: String?, playSelectedCode: String?, playCateMenuCode: String?): MutableMap<String?, Map<String?, String?>?>? {
        return filterList?.get(sportType)?.get(playSelectedCode)?.playCateMap?.get(playCateMenuCode)?.playCateNameMap
    }

}