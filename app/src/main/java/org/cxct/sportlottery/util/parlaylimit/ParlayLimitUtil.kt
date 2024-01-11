package org.cxct.sportlottery.util.parlaylimit

import android.annotation.SuppressLint
import org.cxct.sportlottery.util.ArithUtil
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.function.Consumer

object ParlayLimitUtil {
    //    public static void main(String[] args) {
    //        // 最大中獎金額 最大投注限額api
    //        BigDecimal max = new BigDecimal(10000);
    //        // 最小下注额 最大投注限額api
    //        BigDecimal min = new BigDecimal(10);
    //        // 賠率列表
    //        List<BigDecimal> oddsList = initOddsList();
    //        // 賠率index
    //        int[] oddsIndex = new int[]{0, 1, 2, 3};
    //
    //        // 取得組合(類型、數量、組合列表)
    //        List<ParlayCom> parlayComList = getCom(oddsIndex);
    //        // 打印賠率組合
    ////        parlayComPrintln(parlayComList);
    //
    //        // 得到串关限额 排序各端請自理
    //        Map<String, ParlayBetLimit> parlayBetLimitMap = getParlayLimit(oddsList, parlayComList, max, min);
    //        System.out.println(parlayBetLimitMap);
    //    }
    /**
     * 得到串关限额
     *
     * @param oddsList
     * @param parlayComList
     * @param max
     * @param min
     */
    fun getParlayLimit(oddsList: List<Pair<BigDecimal?, Boolean>>, parlayComList: List<ParlayCom>, max: BigDecimal?, min: BigDecimal?): Map<String, ParlayBetLimit> {
        var max = max
        var min = min
        val result: MutableMap<String, ParlayBetLimit> = LinkedHashMap()
        max = max ?: BigDecimal.valueOf(999)
        min = min ?: BigDecimal.ONE
        parlayComList.forEachIndexed { index, parlayCom ->
            val parlayBetLimit = ParlayBetLimit()
            val oddsArray = getTotalOdds(oddsList, parlayCom.getComList())
            val maxOdds = oddsArray[0]
            val odds = oddsArray[1]
            // 香港盤 可以 odds-num
            val hkOddsArray = getTotalHkOdds(oddsList, parlayCom.getComList())
            val maxHkOdds = hkOddsArray[0]
            val hkOdds = hkOddsArray[1]
            // 投注限額 設定值/odds
            //val maxPayLimit = max!!.divide(hkOdds, 0, RoundingMode.DOWN)
            val maxPayLimit = ArithUtil.div(max!!, hkOdds, 0, RoundingMode.DOWN)
            parlayBetLimit.odds = odds
            parlayBetLimit.maxOdds = maxOdds
            parlayBetLimit.hdOdds = hkOdds
            parlayBetLimit.maxHdOdds = maxHkOdds
            parlayBetLimit.max = maxPayLimit
            parlayBetLimit.min = min
            parlayBetLimit.num = parlayCom.num
            parlayBetLimit.isOnlyEUType = oddsList[index].second
            result[parlayCom.parlayType] = parlayBetLimit
        }
        return result
    }

    /**
     * 获取连串组合的赔率总和
     *
     * @param oddsList
     * @return
     */
    private fun getTotalOdds(
        oddsList: List<Pair<BigDecimal?, Boolean>>,
        comList: List<IntArray>,
    ): List<BigDecimal> {
        var totalOdds = BigDecimal.ZERO
        var maxOdds = BigDecimal.ZERO
        // 取出每種排列組合 [0,1] [0,2] [0,1,2,3]
        for (oddsIndexArray in comList) {
            var odd = BigDecimal.ONE
            for (index in oddsIndexArray) {
                //  賠率相乘
                odd = odd.multiply(ArithUtil.toOddFormat(oddsList[index].first?.toDouble() ?: 1.0, 2).toBigDecimal())
            }
            maxOdds = maxOdds.max(odd)
            totalOdds = totalOdds.add(odd)
        }
        return listOf(maxOdds, totalOdds)
    }

    private fun getTotalHkOdds(
        oddsList: List<Pair<BigDecimal?, Boolean>>,
        comList: List<IntArray>,
    ): List<BigDecimal> {
        var totalOdds = BigDecimal.ZERO
        var maxOdds = BigDecimal.ZERO
        for (oddsIndexArray in comList) {
            var odd = BigDecimal.ONE
            for (index in oddsIndexArray) {
                odd = odd.multiply(oddsList[index].first)
            }
            maxOdds = maxOdds.max(OddsLadder.oddsEuToHk(odd))
            totalOdds = totalOdds.add(OddsLadder.oddsEuToHk(odd))
        }
        totalOdds = if (totalOdds == BigDecimal.ZERO) BigDecimal.ONE else totalOdds
        maxOdds = if (maxOdds == BigDecimal.ZERO) BigDecimal.ONE else maxOdds
        return listOf(maxOdds, totalOdds)
    }


    /**
     * 取得組合(類型、數量、組合列表)
     *
     * @param matchIdArray
     * @return
     */
    @SuppressLint("NewApi")
    fun getCom(matchIdArray: IntArray): List<ParlayCom> {
        val parlayComSOList: MutableList<ParlayCom> = ArrayList()
        if (matchIdArray.size == 1) {
            val parlayCom = ParlayCom()
            parlayCom.num = 1
            parlayCom.parlayType = "1C1"
            parlayCom.setComList(listOf(matchIdArray))
            parlayComSOList.add(parlayCom)
            return parlayComSOList
        }
        val all: MutableList<IntArray> = ArrayList()
        for (i in 2..matchIdArray.size) {
            all.addAll(combine(matchIdArray, i))
        }
        val map: MutableMap<Int, MutableList<IntArray>> = HashMap()
        for (item in all) {
            val list: MutableList<IntArray> = ArrayList()
            list.add(item)
            for (i in list.indices) {
                val oldValue = map[item.size]
                if (oldValue == null) {
                    map[item.size] = list
                } else {
                    if (map.containsKey(item.size)) {
                        oldValue.addAll(list)
                        map[item.size] = oldValue
                    } else {
                        map[item.size] = list
                    }
                }
            }
        }

        //N串1场景
        //Fix bug no.13421 目前OPPO R9s發現此問題,其餘手機正常
        val keyList: List<Int> = map.keys.sorted()

        for (key in keyList) {
            val list: List<IntArray> = map[key]!!
            val parlayCom = ParlayCom()
            parlayCom.num = list.size
            parlayCom.parlayType = list[0].size.toString() + "C1"

            parlayCom.setComList(list)
            parlayComSOList.add(parlayCom)
        }


        //region N串1排序
        //跟當前串關單數相同的, N串1移動至第一項, 其餘的按照N的順序排序
        //舉例: 5單串關則排序為 5串1 - 2串1 - 3串1 - 4串1
        parlayComSOList.sortWith { o1, o2 ->
//            when (matchIdArray.size) {
//                o1.parlayType.split("C")[0].toInt() -> -1
//                o2.parlayType.split("C")[0].toInt() -> 1
//                else -> {
//                    compareValuesBy(o1, o2) { it.parlayType.split("C")[0].toInt() }
//                }
//            }
            //N串1排序，按照N的值从大到小排序
            compareValuesBy(o2, o1) { it.parlayType.split("C")[0].toInt() }
        }
        //endregion

//        if (all.size > 2) {
//            //N串M场景
//            val nParlayM = ParlayCom()
//            nParlayM.num = all.size
//            nParlayM.setComList(all)
//            nParlayM.parlayType = matchIdArray.size.toString() + "C" + all.size
//            parlayComSOList.add(nParlayM)
//        }
        //截取前三个
        return if (parlayComSOList.size > 3) {
            parlayComSOList.subList(0, 3)
        } else {
            parlayComSOList
        }
    }

    /**
     * 組合公式 C n取m
     *
     * @param source n的列表 1,2,3,4,5
     * @param m      取幾
     * @return
     */
    private fun combine(source: IntArray, m: Int): MutableList<IntArray> {
        var result: MutableList<IntArray> = ArrayList()
        if (m == 1) {
            for (i in source.indices) {
                result.add(intArrayOf(source[i]))
            }
        } else if (source.size == m) {
            result.add(source)
        } else {
            val pSource = IntArray(source.size - 1)
            for (i in pSource.indices) {
                pSource[i] = source[i]
            }
            result = combine(pSource, m)
            val tmp: List<IntArray> = combine(pSource, m - 1)
            for (i in tmp.indices) {
                val rs = IntArray(m)
                for (j in 0 until m - 1) {
                    rs[j] = tmp[i][j]
                }
                rs[m - 1] = source[source.size - 1]
                result.add(rs)
            }
        }
        return result
    }


}