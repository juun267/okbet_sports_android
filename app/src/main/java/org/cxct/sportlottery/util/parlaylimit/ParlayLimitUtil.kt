package org.cxct.sportlottery.util.parlaylimit

import android.annotation.SuppressLint
import org.cxct.sportlottery.util.ArithUtil
import timber.log.Timber
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
    fun getParlayLimit(oddsList: List<BigDecimal?>, parlayComList: List<ParlayCom>, max: BigDecimal?, min: BigDecimal?): Map<String, ParlayBetLimit> {
        var max = max
        var min = min
        val result: MutableMap<String, ParlayBetLimit> = LinkedHashMap()
        max = max ?: BigDecimal.valueOf(999)
        min = min ?: BigDecimal.ONE
        for (parlayCom in parlayComList) {
            val parlayBetLimit = ParlayBetLimit()
            val odds = getTotalOdds(oddsList, parlayCom.getComList())
            // 香港盤 可以 odds-num
            val hkOdds = getTotalHkOdds(oddsList, parlayCom.getComList())
            // 投注限額 設定值/odds
            //val maxPayLimit = max!!.divide(hkOdds, 0, RoundingMode.DOWN)

            val maxPayLimit = ArithUtil.div(max!!, hkOdds, 0, RoundingMode.DOWN)

            parlayBetLimit.odds = odds
            parlayBetLimit.hdOdds = hkOdds
            parlayBetLimit.max = maxPayLimit
            parlayBetLimit.min = min
            parlayBetLimit.num = parlayCom.num
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
    private fun getTotalOdds(oddsList: List<BigDecimal?>, comList: List<IntArray>): BigDecimal {
        var totalOdds = BigDecimal.ZERO

        // 取出每種排列組合 [0,1] [0,2] [0,1,2,3]
        for (oddsIndexArray in comList) {
            var odd = BigDecimal.ONE
            for (index in oddsIndexArray) {
                //  賠率相乘
                odd = odd.multiply(oddsList[index])
            }
            totalOdds = totalOdds.add(odd)
        }
        return totalOdds
    }

    private fun getTotalHkOdds(oddsList: List<BigDecimal?>, comList: List<IntArray>): BigDecimal {
        var totalOdds = BigDecimal.ZERO
        for (oddsIndexArray in comList) {
            var odd = BigDecimal.ONE
            for (index in oddsIndexArray) {
                odd = odd.multiply(oddsList[index])
            }
            totalOdds = totalOdds.add(OddsLadder.oddsEuToHk(odd))
        }
        return if (totalOdds == BigDecimal.ZERO) BigDecimal.ONE else totalOdds
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


        parlayComSOList.apply {
            sortBy { it.parlayType.split("C")[1].toInt() }
            reverse()
        }

        parlayComSOList.reverse()

        if (all.size > 2) {
            //N串M场景
            val nParlayM = ParlayCom()
            nParlayM.num = all.size
            nParlayM.setComList(all)
            nParlayM.parlayType = matchIdArray.size.toString() + "C" + all.size
            parlayComSOList.add(nParlayM)
        }

        return parlayComSOList
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

    private fun initOddsList(): List<BigDecimal> {
        val oddsList = ArrayList<BigDecimal>()
        oddsList.add(BigDecimal("2.23"))
        oddsList.add(BigDecimal("1.6"))
        oddsList.add(BigDecimal("1.7"))
        oddsList.add(BigDecimal("1.85"))
        return oddsList
    }

    /**
     * 單純印出
     *
     * @param parlayComList
     */
    @SuppressLint("NewApi")
    private fun parlayComPrintln(parlayComList: List<ParlayCom>) {
        for (parlayCom in parlayComList) {
            println("parlayType: " + parlayCom.parlayType)
            println("num: " + parlayCom.num)
            println("comList: ")
            parlayCom.getComList().forEach(Consumer { i: IntArray? -> println(Arrays.toString(i)) })
            println("-----------------")
        }
    }
}