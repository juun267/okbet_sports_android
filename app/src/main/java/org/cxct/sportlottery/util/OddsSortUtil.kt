package org.cxct.sportlottery.util

import org.cxct.sportlottery.ui.game.home.recommend.RecommendGameEntity

object OddsSortUtil {
    /**
     * 首頁推薦賽事賠率排序
     */
    fun MutableList<RecommendGameEntity>.recommendSortOddsMap() {
        this.forEach { RecommendGameEntity ->
            RecommendGameEntity.oddBeans.forEach { OddBeans ->
                OddBeans.oddList.sortBy { it?.marketSort }
            }
        }
    }
}