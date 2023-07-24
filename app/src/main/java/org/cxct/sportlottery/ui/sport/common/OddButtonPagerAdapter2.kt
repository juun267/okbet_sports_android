package org.cxct.sportlottery.ui.sport.common

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.sport.list.adapter.ODDS_ITEM_TYPE
import org.cxct.sportlottery.ui.sport.list.adapter.OnOddClickListener
import org.cxct.sportlottery.ui.sport.oddsbtn.PlayCateView
import org.cxct.sportlottery.util.LanguageManager


class OddButtonPagerAdapter2(val context: Context,
                             private val onOddClick: OnOddClickListener)
    :RecyclerView.Adapter<OddButtonPagerViewHolder2>() {

    private var oddsSort: String?= null
    private var playCateNameMap: MutableMap<String?, Map<String?, String?>?> = mutableMapOf()
    private var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?> = mutableMapOf() //遊戲名稱顯示用playCateNameMap，下注顯示用betPlayCateNameMap
    private lateinit var matchOdd: MatchOdd
    /**
     * 2023/02/28
     * manta需求，最大赔率数量统一改成6
     */
    private val sizeCount = { gameType: String? -> 3 }
    private var oddsType: OddsType = OddsType.EU
    private var matchType: MatchType = MatchType.IN_PLAY
    private var data: List<List<Pair<String, List<Odd>>>> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OddButtonPagerViewHolder2 {
        return OddButtonPagerViewHolder2(PlayCateView(parent.context))
    }

    override fun onViewRecycled(holder: OddButtonPagerViewHolder2) {
        super.onViewRecycled(holder)
        holder.oddBtnList.recyclerAll()
    }

    override fun getItemViewType(position: Int): Int {
        return ODDS_ITEM_TYPE
    }

    fun setupData(matchType: MatchType, item: MatchOdd, oddsType: OddsType) {
        this.matchType = matchType
        this.oddsType = oddsType
        this.matchOdd = item
        this.playCateNameMap = item.playCateNameMap ?: mutableMapOf()
        this.betPlayCateNameMap = item.betPlayCateNameMap ?: mutableMapOf()
        if (!item.oddsSort.isNullOrEmpty()) { this.oddsSort = item.oddsSort }

//        val oddsMap = mutableMapOf<String, MutableList<Odd>>()
//        item.oddsMap?.forEach { entry-> entry.value?.let { oddsMap[entry.key] = it }}
//        setOddValues(oddsMap, item)

        setOddValues(item.oddsMap ?: mutableMapOf(), item)
    }

    private fun setOddValues(value: MutableMap<String, MutableList<Odd>?>, matchOdd: MatchOdd) {
        this.playCateNameMap = playCateNameMap.addSplitPlayCateTranslation()
        val oddsSortCount = oddsSort?.split(",")?.size ?: 999 // 最大顯示數量
        val matchInfo = matchOdd.matchInfo

        val odds = value.sortScores()
            .refactorPlayCode(matchInfo?.gameType)
            .sortOdds()
            .mappingCSList(matchOdd)
            .filterOddsStatus()
            .splitPlayCate()
            .filterPlayCateSpanned(matchInfo?.gameType)
            .sortPlayCate()

        val nonNullValues = odds.filterValues { !it.isNullOrEmpty() }
        val gameList = nonNullValues.plus(nonNullValues.filter { it.value?.getOrNull(0) == null })
            .map { it.key }.take(sizeCount(matchInfo?.gameType)).toMutableList()
        data = gameList.withIndex().groupBy {
            it.index / 1
        }.map {
            it.value.map { it.value }
        }.map {
            it.map { playCate: String ->
                if (playCate.contains("EmptyData"))
                    Pair(playCate, listOf())
                else
                    Pair(playCate, odds[playCate]?.filterNotNull() ?: listOf())
            }
        }
    }


    override fun onBindViewHolder(holder: OddButtonPagerViewHolder2, position: Int) {
//        Log.d("Hewie", "綁定(${matchInfo?.homeName})：賠率表($position)")
        val item = data[position].getOrNull(0)
        holder.setupOddsButton2(
            matchOdd?.matchInfo,
            playCateNameMap,
            betPlayCateNameMap,
            Pair(item?.first ?: "", item?.second ?: listOf()),
            oddsType,
            onOddClick,
            matchType,

        )
    }

    override fun onBindViewHolder(holder: OddButtonPagerViewHolder2, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
            return
        }

        val last = payloads.last() as ArrayList<Pair<String, List<Odd>?>>
        val data = last.first()
        holder.update(matchOdd.matchInfo,
            playCateNameMap,
            betPlayCateNameMap,
            Pair(data.first, data.second),
            oddsType,
            matchType)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    /**
     * FT: 盤口組合 玩法需特殊處理
     * */
    private fun Map<String, List<Odd?>?>.splitPlayCate(): Map<String, List<Odd?>?> {
        val splitMap = mutableMapOf<String, List<Odd?>?>()

        this.forEach { oddsMap ->
            when (oddsMap.key) {
                PlayCate.SINGLE_OU.value -> {

                    splitMap[PlayCate.SINGLE_OU_O.value] =
                        listOf(
                            oddsMap.value?.getOrNull(0),
                            oddsMap.value?.getOrNull(2),
                            oddsMap.value?.getOrNull(4)
                        )

                    splitMap[PlayCate.SINGLE_OU_U.value] =
                        listOf(
                            oddsMap.value?.getOrNull(1),
                            oddsMap.value?.getOrNull(3),
                            oddsMap.value?.getOrNull(5)
                        )
                }

                PlayCate.SINGLE_BTS.value -> {

                    splitMap[PlayCate.SINGLE_BTS_Y.value] =
                        listOf(
                            oddsMap.value?.getOrNull(0),
                            oddsMap.value?.getOrNull(2),
                            oddsMap.value?.getOrNull(4)
                        )

                    splitMap[PlayCate.SINGLE_BTS_N.value] =
                        listOf(
                            oddsMap.value?.getOrNull(1),
                            oddsMap.value?.getOrNull(3),
                            oddsMap.value?.getOrNull(5)
                        )
                }

                else -> {
                    splitMap[oddsMap.key] = oddsMap.value
                }
            }
        }

        return splitMap
    }

    /**
     * 冒號後面的分數整理 需要取代翻譯的 {S}
     * FT: NOGAL(下個進球) 玩法需特殊處理
     * */
    private fun Map<String, List<Odd?>?>.sortScores(): Map<String, List<Odd?>?> {

        val rgzMap = this.filter { (key, _) -> key.contains(":") }
        if (rgzMap.isEmpty()) {
            return this
        }

        val splitMap = this.toMutableMap()
        rgzMap.forEach { rgzMap ->
            splitMap[rgzMap.key]?.forEach { odd ->
                if (rgzMap.key.contains("${PlayCate.NGOAL.value}:")) {
                    odd?.nextScore = rgzMap.key.split("${PlayCate.NGOAL.value}:")[1] //nextScore 下個進球的分數會放在Key值的冒號後面
                } else if (rgzMap.key.contains("${PlayCate.NGOAL_OT.value}:")) {
                    odd?.nextScore = rgzMap.key.split("${PlayCate.NGOAL_OT.value}:")[1] //nextScore 下個進球的分數會放在Key值的冒號後面
                } else {
                    odd?.replaceScore = rgzMap.key.split(":")[1] //翻譯裡面要顯示{S}的分數會放在Key值的冒號後面
                }
            }
        }

        return splitMap
    }

    /**
     * 有些playCateCode後面會給： 要特別做處理
     * */
    private fun Map<String, List<Odd?>?>.refactorPlayCode(gameType: String?): Map<String, List<Odd?>?> {
        return try {
            val oddsMap: MutableMap<String, List<Odd?>?>
            val rgzMap = this.filter { (key, _) -> key.contains(":") }

            //網球玩法特殊處理:网球的特定第几局的玩法(1X2_SEG3_GAMES:1~6) 之前应该是当有两个数字的时候 取大的显示 目前看小金改为取小的显示了 这边再跟著调整一下取小的显示在大厅上
            //網球玩法特殊處理:网球的特定第几局的玩法(1X2_SEG3_GAMES:1~6) 2022/4/26 跟IOS H5同步 顯示最小的
            if (rgzMap.isNotEmpty() && gameType == GameType.TN.key && (rgzMap.iterator().next().key.contains("1X2_SEG") && rgzMap.iterator().next().key.contains("_GAMES"))) {
                oddsMap = this.filter { !it.key.contains(":") }.toMutableMap()
                val mutableListIterator = rgzMap.iterator()
//                var iteratorMap: Map.Entry<String, List<Odd?>?>? = null
//                    while (mutableListIterator.hasNext()) {
//                        iteratorMap = mutableListIterator.next()
//                    }
                val iteratorMap = mutableListIterator.next()
                if (iteratorMap != null) {
                    val playKeyFilter = iteratorMap.key.split(":")[0]
                    oddsMap[playKeyFilter] = iteratorMap.value
                }
                return oddsMap
            }

            if (rgzMap.isNotEmpty()) {
                oddsMap = this.filter { !it.key.contains(":") }.toMutableMap()
                rgzMap.forEach { map ->
                    val playKeyFilter = map.key.split(":")[0]
                    oddsMap[playKeyFilter] = map.value
                }

                return oddsMap
            }

            return this
        } catch (e: Exception) {
            e.printStackTrace()
            this
        }
    }

    private fun Map<String, List<Odd?>?>.mappingCSList(matchOdd: MatchOdd?): Map<String, List<Odd?>?> {
        if (matchType != MatchType.CS) return this
        var oddsMap = mapOf<String, List<Odd?>?>()
        val csList = this[matchOdd?.csTabSelected?.value] ?: return oddsMap

        val homeList: MutableList<Odd> = mutableListOf()
        val drawList: MutableList<Odd> = mutableListOf()
        val awayList: MutableList<Odd> = mutableListOf()
        val otherList: MutableList<Odd?> = mutableListOf()
        for (odd in csList) {
            if (odd?.name?.contains(" - ") == true) {
                val stringArray: List<String> = odd.name.split(" - ")
                if (stringArray[0].toInt() > stringArray[1].toInt()) {
                    homeList.add(odd)
                }
                if (stringArray[0].toInt() == stringArray[1].toInt()) {
                    drawList.add(odd)
                }
                if (stringArray[0].toInt() < stringArray[1].toInt()) {
                    awayList.add(odd)
                }
            } else {
                otherList.add(odd)
            }
        }

        homeList.sortBy {
            it.name?.split(" - ")?.get(1)?.toInt()
        }
        homeList.sortBy {
            it.name?.split(" - ")?.get(0)?.toInt()
        }

        awayList.sortBy {
            it.name?.split(" - ")?.get(0)?.toInt()
        }
        awayList.sortBy {
            it.name?.split(" - ")?.get(1)?.toInt()
        }

        val newList: MutableList<MutableList<Odd?>> = mutableListOf()
        homeList.forEachIndexed { index, _ ->
            if (index > drawList.size - 1)
                newList.add(mutableListOf(homeList[index], awayList[index]))
            else
                newList.add(mutableListOf(homeList[index], awayList[index], drawList[index]))
        }
        if (otherList.size > 0) {
            newList.add(newList.size, otherList)
        }
        val csMap = newList.associateBy(keySelector = {
            "${matchOdd?.csTabSelected?.value}_${
                newList.indexOf(it)
            }"
        }, valueTransform = { it })

        return csMap
    }

    /**
     * 根據賽事的oddsSort將盤口重新排序
     */
    private fun Map<String, List<Odd?>?>.sortOdds(): Map<String, List<Odd?>?> {
        if (oddsSort.isNullOrEmpty()) {
            return this
        }

        val sortOrder = oddsSort?.split(",")
        val filterOdds = this.filter { sortOrder?.contains(it.key.split(":")[0]) == true }
        val oddsMap = filterOdds.toSortedMap(compareBy<String> {
            val oddsIndex = sortOrder?.indexOf(it.split(":")[0])
            oddsIndex
        }.thenBy { it })
        return oddsMap
    }

    /**
     * 把賠率狀態為 2(不可用，不可见也不可投注) 的狀況過濾掉
     * */
    private fun Map<String, List<Odd?>?>.filterOddsStatus(): Map<String, List<Odd?>?> {
        return filterValues { it?.firstOrNull()?.status != BetStatus.DEACTIVATED.code }
    }

    //SINGLE_OU、SINGLE_BTS兩種玩法要特殊處理，後端API沒給翻譯
    private fun MutableMap<String?, Map<String?, String?>?>?.addSplitPlayCateTranslation(): MutableMap<String?, Map<String?, String?>?> {
        val translationMap = mutableMapOf<String?, Map<String?, String?>?>()

        this?.let { translationMap.putAll(it) }

        val ou_o_Map: MutableMap<String, String> = mutableMapOf()
        val ou_u_Map: MutableMap<String, String> = mutableMapOf()
        val bts_y_Map: MutableMap<String, String> = mutableMapOf()
        val bts_n_Map: MutableMap<String, String> = mutableMapOf()
        for (language in LanguageManager.Language.values()) {
            ou_o_Map[language.key] = context.getString(R.string.J801)
            ou_u_Map[language.key] = context.getString(R.string.J802)
            bts_y_Map[language.key] = context.getString(R.string.J803)
            bts_n_Map[language.key] = context.getString(R.string.J804)
        }
        translationMap[PlayCate.SINGLE_OU_O.value] = ou_o_Map.toMap()
        translationMap[PlayCate.SINGLE_OU_U.value] = ou_u_Map.toMap()
        translationMap[PlayCate.SINGLE_BTS_Y.value] = bts_y_Map.toMap()
        translationMap[PlayCate.SINGLE_BTS_N.value] = bts_n_Map.toMap()
        return translationMap
    }

    private fun Map<String, List<Odd?>?>.filterPlayCateSpanned(gameType: String?): Map<String, List<Odd?>?> = this.mapValues { map ->
        val playCateNum = when { //根據IOS給的規則判斷顯示數量

            map.value?.size ?: 0 < 3 -> 2

            (gameType == GameType.TT.key || gameType == GameType.BM.key) && map.key.contains(PlayCate.SINGLE.value) -> 2 //乒乓球獨贏特殊判斷 羽球獨贏特殊判斷

            map.key.contains(PlayCate.HDP.value) || (map.key.contains(PlayCate.OU.value) && !map.key.contains(PlayCate.SINGLE_OU.value)) || map.key.contains(
                PlayCate.CORNER_OU.value
            ) -> 2

            map.key.contains(PlayCate.SINGLE.value) || map.key.contains(PlayCate.NGOAL.value) || map.key.contains(PlayCate.NGOAL_OT.value) -> 3

            else -> 3
        }

        map.value?.filterIndexed { index, _ ->
            index < playCateNum
        }
    }

    private fun Map<String, List<Odd?>?>.sortPlayCate(): Map<String, List<Odd?>?> {
        val sortMap = mutableMapOf<String, List<Odd?>?>()

        this.forEach { oddsMap ->

            when {
                oddsMap.key.contains(PlayCate.SINGLE.value)
                        || oddsMap.key.contains(PlayCate.MOST_SIX.value)
                        || oddsMap.key.contains(PlayCate.MOST_FOUR.value)
                        || oddsMap.key.contains(PlayCate.HOP.value) -> {
                    val oddList = oddsMap.value?.filterNotNull()?.toMutableList()
                    oddList?.indexOf(oddList.find {
                        it?.nameMap?.get(LanguageManager.Language.EN.key)
                            ?.split("-")
                            ?.getOrNull(0)?.contains("Draw")
                            ?: false
                    }
                    )?.let {
                        if (it >= 0) {
                            oddList.add(oddList.removeAt(it))
                        }
                    }

                    sortMap[oddsMap.key] = oddList
                }
                oddsMap.key.contains(PlayCate.NGOAL.value) || oddsMap.key.contains(PlayCate.NGOAL_OT.value)-> {
                    val oddList = oddsMap.value?.filterNotNull()?.toMutableList()

                    oddList?.indexOf(oddList.find {
                        it?.nameMap?.get(LanguageManager.Language.EN.key)
                            ?.split("-")
                            ?.getOrNull(0)?.contains("No Goal")
                            ?: false
                    }
                    )?.let {
                        if (it >= 0) {
                            oddList.add(oddList.removeAt(it))
                        }
                    }

                    sortMap[oddsMap.key] = oddList
                }
                else -> {
                    sortMap[oddsMap.key] = oddsMap.value
                }
            }
        }

        return sortMap
    }

}
