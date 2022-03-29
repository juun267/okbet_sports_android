package org.cxct.sportlottery.util

import org.cxct.sportlottery.network.sport.SportMenuFilter

object PlayCateMenuFilterUtils {
    var filterList : MutableMap<String?, MutableMap<String?, SportMenuFilter>?>? = mutableMapOf()

    //篩選不可下拉的玩法排序。可下拉的直接用PlayCateMenuCode，馬克說只會有一筆
    fun filterOddsSort(sportType: String?, playCateMenuCode: String?): String? {
       return filterList?.get(sportType)?.get(playCateMenuCode)?.oddsSort
    }

    //篩選不可下拉的翻譯
    fun filterPlayCateNameMap(sportType: String?, playCateMenuCode: String?): MutableMap<String?, Map<String?, String?>?>? {
        return filterList?.get(sportType)?.get(playCateMenuCode)?.playCateNameMap
    }

    //篩選可下拉的翻譯
    fun filterSelectablePlayCateNameMap(sportType: String?, playSelectedCode: String?, playCateMenuCode: String?): MutableMap<String?, Map<String?, String?>?>? {
        return filterList?.get(sportType)?.get(playSelectedCode)?.playCateMap?.get(playCateMenuCode)?.playCateNameMap
    }

}