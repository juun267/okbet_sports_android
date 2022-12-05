package org.cxct.sportlottery.ui.game.common

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.common.PlayCateMapItem
import org.cxct.sportlottery.ui.game.common.view.OddBtnList
import org.cxct.sportlottery.ui.game.common.view.OddsButton2
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.sport.SportLeagueAdapter
import org.cxct.sportlottery.util.BetPlayCateFunction.isCombination
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TextUtil


class OddButtonPagerAdapter :RecyclerView.Adapter<OddButtonPagerViewHolder>() {
    private var matchInfo: MatchInfo?= null
    private var oddsSort: String?= null
    private var playCateNameMap: MutableMap<String?, Map<String?, String?>?>?= null
    private var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?= null //遊戲名稱顯示用playCateNameMap，下注顯示用betPlayCateNameMap
    private var matchOdd: MatchOdd? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OddButtonPagerViewHolder {
        return OddButtonPagerViewHolder(OddBtnList(parent.context), oddStateRefreshListener)
    }

    override fun onViewRecycled(holder: OddButtonPagerViewHolder) {
        super.onViewRecycled(holder)
        holder.oddBtnList.recyclerAll()
    }

    override fun getItemViewType(position: Int): Int {
        return SportLeagueAdapter.ItemType.ITEM.ordinal
    }

    fun setData(matchInfo: MatchInfo?, oddsSort: String?, playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
                betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?, getPlaySelectedCodeSelectionType: Int?,
                matchOdd: MatchOdd?
    ) {
        this.matchInfo = matchInfo

        if (!oddsSort.isNullOrEmpty())
            this.oddsSort = oddsSort
        this.playCateNameMap = playCateNameMap
        this.betPlayCateNameMap = betPlayCateNameMap
        this.matchOdd = matchOdd
    }

    var odds: Map<String, List<Odd?>?> = mapOf()
        set(value) {
            this.playCateNameMap = playCateNameMap.addSplitPlayCateTranslation()
            val oddsSortCount = oddsSort?.split(",")?.size ?: 999 // 最大顯示數量
            field = value.sortScores().refactorPlayCode().sortOdds().mappingCSList(matchOdd).filterOddsStatus().splitPlayCate()
                .filterPlayCateSpanned().sortPlayCate()
            val gameList =
                field.filterValues { !it.isNullOrEmpty() }
                    .plus(field.filterValues { !it.isNullOrEmpty() }
                        .filter { it.value?.getOrNull(0) == null }).map { it.key }.run {
                        val gameListFilter: MutableList<String>

                        when{
                            //波膽玩法不限制個數
                            matchType == MatchType.CS -> {
                                if (field.values.isEmpty()) {
                                    //加入假資料
                                    gameListFilter = mutableListOf()
                                    for (i in 1..8) {
                                        gameListFilter.add("EmptyData${i}")
                                    }
                                } else {
                                    gameListFilter = this.toMutableList()
                                }
                            }

                            this.isNullOrEmpty() ->{
                                gameListFilter = mutableListOf()
                                gameListFilter.add("EmptyData1")
                            }
                            this.size > sizeCount(matchInfo?.gameType) -> {
                                gameListFilter = this.take(sizeCount(matchInfo?.gameType)) as MutableList<String>
                            }
                            else -> {
                                val maxCount = if(sizeCount(matchInfo?.gameType) < oddsSortCount) sizeCount(matchInfo?.gameType) else oddsSortCount
                                val count =
                                    if (sizeCount(matchInfo?.gameType) > this.size) maxCount - this.size else 0

                                gameListFilter = this.take(this.size + 1).toMutableList()
                                for (i in 1..count) {
                                    gameListFilter.add("EmptyData${i}")
                                }
                            }
                        }
                        gameListFilter
                    }
            data = gameList.withIndex().groupBy {
                it.index / 1
            }.map {
                it.value.map { it.value }
            }.map {
                it.map { playCate ->
                    if (playCate.contains("EmptyData"))
                        playCate to listOf<Odd?>(null, null)
                    else
                        playCate to field[playCate]
                }
            }
        }

    val sizeCount = { gameType: String? ->
        when (gameType) {
            GameType.BM.key -> 4
            GameType.TT.key -> 4
            GameType.IH.key -> 4
            GameType.BX.key -> 2
            GameType.CB.key -> 6
            GameType.CK.key -> 4
            GameType.RB.key -> 4
            GameType.AFT.key -> 6
            GameType.BK.key -> 8
            GameType.VB.key -> 4
            GameType.FT.key -> 8
            GameType.TN.key -> 6
            else -> 8
        }
    }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                //notifyDataSetChanged()
            }
        }

    var matchType: MatchType? = null

    private var data: List<List<Pair<String, List<Odd?>?>>> = listOf()
        set(value) {
            Log.d("Hewie10", "${value}")
            field = value
            notifyDataSetChanged()
        }

    var listener: OddButtonListener? = null

    private val oddStateRefreshListener by lazy {
        object : OddStateViewHolder.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(data.indexOf(data.find { item ->
                    item.any { it ->
                        it.second?.any {
                            it == odd
                        } ?: false
                    }
                }))
            }
        }
    }

    // region update functions
    fun update() {
        data.forEachIndexed { index, list -> notifyItemChanged(index, list) }
    }


    override fun onBindViewHolder(holder: OddButtonPagerViewHolder, position: Int) {
        Log.d("Hewie", "綁定(${matchInfo?.homeName})：賠率表($position)")
        val item = data[position].getOrNull(0)
        holder.setupOddsButton2(
            position,
            itemCount,
            matchInfo,
            playCateNameMap,
            betPlayCateNameMap,
            Pair(item?.first, item?.second),
            oddsType,
            listener,
            matchType
        )
    }

    override fun onBindViewHolder(holder: OddButtonPagerViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val last = payloads.last() as ArrayList<Pair<String, List<Odd?>?>>
            val data = last.first()
            holder.update(position, itemCount, matchInfo, playCateNameMap, betPlayCateNameMap,
                Pair(data.first, data.second),
                oddsType, matchType
            )
        }
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

        val splitMap: MutableMap<String, List<Odd?>?>
        val rgzMap = this.filter { (key, _) -> key.contains(":") }

        when {
            rgzMap.isNotEmpty() -> {
                splitMap = this.toMutableMap()
                rgzMap.forEach { rgzMap ->
                    splitMap[rgzMap.key]?.forEach { odd ->
                        when {
                            rgzMap.key.contains("${PlayCate.NGOAL.value}:") -> {
                                odd?.nextScore =
                                    rgzMap.key.split("${PlayCate.NGOAL.value}:")[1] //nextScore 下個進球的分數會放在Key值的冒號後面
                            }
                            rgzMap.key.contains("${PlayCate.NGOAL_OT.value}:") -> {
                                odd?.nextScore =
                                    rgzMap.key.split("${PlayCate.NGOAL_OT.value}:")[1] //nextScore 下個進球的分數會放在Key值的冒號後面
                            }
                            else -> odd?.replaceScore =
                                rgzMap.key.split(":")[1] //翻譯裡面要顯示{S}的分數會放在Key值的冒號後面
                        }
                    }
                }
            }
            else -> return this
        }

        return splitMap
    }

    /**
     * 有些playCateCode後面會給： 要特別做處理
     * */
    private fun Map<String, List<Odd?>?>.refactorPlayCode(): Map<String, List<Odd?>?> {
        return try {
            val oddsMap: MutableMap<String, List<Odd?>?>
            val rgzMap = this.filter { (key, _) -> key.contains(":") }

            //網球玩法特殊處理:网球的特定第几局的玩法(1X2_SEG3_GAMES:1~6) 之前应该是当有两个数字的时候 取大的显示 目前看小金改为取小的显示了 这边再跟著调整一下取小的显示在大厅上
            //網球玩法特殊處理:网球的特定第几局的玩法(1X2_SEG3_GAMES:1~6) 2022/4/26 跟IOS H5同步 顯示最小的
            when {
                rgzMap.isNotEmpty() && matchInfo?.gameType == GameType.TN.key && (rgzMap.iterator().next().key.contains("1X2_SEG") && rgzMap.iterator().next().key.contains("_GAMES"))-> {
                    oddsMap = this.filter { !it.key.contains(":") }.toMutableMap()
                    val mutableListIterator = rgzMap.iterator()
                    var iteratorMap: Map.Entry<String, List<Odd?>?>? = null
//                    while (mutableListIterator.hasNext()) {
//                        iteratorMap = mutableListIterator.next()
//                    }
                    iteratorMap = mutableListIterator.next()
                    if (iteratorMap != null) {
                        val playKeyFilter = iteratorMap.key.split(":")[0]
                        oddsMap[playKeyFilter] = iteratorMap.value
                    }
                    oddsMap
                }
                rgzMap.isNotEmpty() -> {
                    oddsMap = this.filter { !it.key.contains(":") }.toMutableMap()
                    rgzMap.forEach { map ->
                        val playKeyFilter = map.key.split(":")[0]
                        oddsMap[playKeyFilter] = map.value
                    }
                    oddsMap
                }
                else -> this
            }
        } catch (e: Exception) {
            e.printStackTrace()
            this
        }
    }

    private fun Map<String, List<Odd?>?>.mappingCSList(matchOdd: MatchOdd?): Map<String, List<Odd?>?> {
        if (matchType != MatchType.CS) return this
        var oddsMap = mapOf<String, List<Odd?>?>()
        val csList = this[matchOdd?.csTabSelected?.value]
        val homeList: MutableList<Odd> = mutableListOf()
        val drawList: MutableList<Odd> = mutableListOf()
        val awayList: MutableList<Odd> = mutableListOf()
        if (csList != null) {
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
                if(index > drawList.size -1)
                    newList.add(mutableListOf(homeList[index], awayList[index]))
                else
                    newList.add(mutableListOf(homeList[index], awayList[index], drawList[index]))
            }
            newList.add(newList.size, mutableListOf(csList[csList.lastIndex]))
            val csMap = newList.associateBy(keySelector = { "${matchOdd?.csTabSelected?.value}_${newList.indexOf(it)}" }, valueTransform = { it })
            oddsMap = csMap
        }
        return oddsMap
    }

    /**
     * 根據賽事的oddsSort將盤口重新排序
     */
    private fun Map<String, List<Odd?>?>.sortOdds(): Map<String, List<Odd?>?> {
        var oddsMap = mutableMapOf<String, List<Odd?>?>()

        val sortOrder = oddsSort?.split(",")
        val filterOdds = this.filter { sortOrder?.contains(it.key.split(":")[0]) == true }
        oddsMap = filterOdds.toSortedMap(compareBy<String> {
            val oddsIndex = sortOrder?.indexOf(it.split(":")[0])
            oddsIndex
        }.thenBy { it })
        return if(oddsSort.isNullOrEmpty()) this else oddsMap
    }

    /**
     * 把賠率狀態為 2(不可用，不可见也不可投注) 的狀況過濾掉
     * */
    private fun Map<String, List<Odd?>?>.filterOddsStatus(): Map<String, List<Odd?>?> {
        return filterValues { it?.firstOrNull()?.status != BetStatus.DEACTIVATED.code }
    }

    //SINGLE_OU、SINGLE_BTS兩種玩法要特殊處理，後端API沒給翻譯
    private fun MutableMap<String?, Map<String?, String?>?>?.addSplitPlayCateTranslation(): MutableMap<String?, Map<String?, String?>?>? {
        val translationMap = mutableMapOf<String?, Map<String?, String?>?>()

        this?.let { translationMap.putAll(it) }

        val ou_o_Map: Map<String?, String?> =
            mapOf("zh" to "独赢&大", "en" to "1 X 2 & Goals Over", "vi" to "1 X 2 & Trên")

        val ou_u_Map: Map<String?, String?> =
            mapOf("zh" to "独赢&小", "en" to "1 X 2 & Goals Under", "vi" to "1 X 2 & Dưới")

        val bts_y_Map: Map<String?, String?> = mapOf(
            "zh" to "独赢&双方球队进球-是",
            "en" to "1 X 2 & Both to Score Y",
            "vi" to "1 X 2 & Hai Đội Ghi Bàn - Có"
        )

        val bts_n_Map: Map<String?, String?> = mapOf(
            "zh" to "独赢&双方球队进球-否",
            "en" to "1 X 2 & Both to Score N",
            "vi" to "1 X 2 & Hai Đội Ghi Bàn - Không"
        )

        translationMap[PlayCate.SINGLE_OU_O.value] = ou_o_Map
        translationMap[PlayCate.SINGLE_OU_U.value] = ou_u_Map
        translationMap[PlayCate.SINGLE_BTS_Y.value] = bts_y_Map
        translationMap[PlayCate.SINGLE_BTS_N.value] = bts_n_Map

        return translationMap
    }

    private fun Map<String, List<Odd?>?>.filterPlayCateSpanned(): Map<String, List<Odd?>?> {
        return this.mapValues { map ->
            val playCateNum =
                when { //根據IOS給的規則判斷顯示數量
                    map.value?.size ?: 0 < 3 -> 2

                    (matchInfo?.gameType == GameType.TT.key || matchInfo?.gameType == GameType.BM.key) && map.key.contains(PlayCate.SINGLE.value) -> 2 //乒乓球獨贏特殊判斷 羽球獨贏特殊判斷

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
    }

    private fun Map<String, List<Odd?>?>.sortPlayCate(): Map<String, List<Odd?>?> {
        val sortMap = mutableMapOf<String, List<Odd?>?>()

        this.forEach { oddsMap ->

            when {
                oddsMap.key.contains(PlayCate.SINGLE.value) -> {
                    val oddList = oddsMap.value?.toMutableList()

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
                    val oddList = oddsMap.value?.toMutableList()

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

class OddButtonPagerViewHolder constructor(
    val oddBtnList: OddBtnList,
    private val oddStateRefreshListener: OddStateChangeListener
) : OddStateViewHolder(oddBtnList) {

    fun update(
        position: Int,
        itemCount: Int,
        matchInfo: MatchInfo?,
        playCateNameMap: Map<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: Map<String?, Map<String?, String?>?>?,
        odds: Pair<String?, List<Odd?>?>,
        oddsType: OddsType,
        matchType: MatchType?
    ) {

        updateOddsButton2(
            position,
            itemCount,
            matchInfo,
            playCateNameMap,
            betPlayCateNameMap,
            odds,
            oddsType,
            matchType
        )

    }

    private fun <K, V> Map<K, V>?.getPlayCateName(selectLanguage: LanguageManager.Language): String {
        val playCateName = this?.get<Any?, V>(selectLanguage.key) ?: this?.get<Any?, V>(
            LanguageManager.Language.EN.key)
        return playCateName.toString()
    }

    fun setupOddsButton2(
        position: Int,
        itemCount: Int,
        matchInfo: MatchInfo?,
        playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        odds: Pair<String?, List<Odd?>?>?,
        oddsType: OddsType,
        oddButtonListener: OddButtonListener?,
        matchType: MatchType?,
    ) {
        if (setOddsButtonStatu(
                position,
                itemCount,
                matchInfo,
                playCateNameMap,
                betPlayCateNameMap,
                odds,
                matchType)) {
            return
        }

        val replaceScore = odds!!.second?.firstOrNull()?.replaceScore ?: ""
        val language = LanguageManager.getSelectLanguage(itemView.context)
        var playCateName = playCateNameMap!![odds.first]?.getPlayCateName(language)
                ?.replace(": ", " ")
                ?.replace("||", "\n")
                ?.replace("{S}", replaceScore)
                ?.replace("{H}", "${matchInfo!!.homeName}")
                ?.replace("{C}", "${matchInfo.awayName}") ?: ""

        odds.second?.firstOrNull()?.replaceScore?.let { playCateName.replace("{S}", it) }
        if (playCateName == "null" || playCateName.isEmpty()){
            playCateName = "-"
        }

        val betPlayCateName =
            betPlayCateNameMap!![odds.first]?.getPlayCateName(language)?.replace(": ", " ")?.replace("||", "\n") ?: ""
        var playCateCode = odds.first ?: ""
        //去掉mappingCS playCateCode的後綴
        if (playCateCode.contains(PlayCate.CS.value) && playCateCode.contains("_")) {
            playCateCode = playCateCode.split("_")[0]
        }
//        Timber.e("playCateCode: $playCateCode")


        oddBtnList.oddBtnType.text = playCateName.updatePlayCateColor()
        val isDeactivated = (odds.second == null || odds.second!!.all { it == null })

        if (matchType == MatchType.CS && odds?.second?.size == 1) {
            val oddBtnOther = oddBtnList.getOtherOddsBtn()
            odds.second?.getOrNull(0).let {
                bindOddBtn(oddBtnOther, isDeactivated, playCateCode, it, odds.second, oddsType, isOtherBtn = true)
                bindOddClick(oddBtnOther, it, oddButtonListener, matchInfo, playCateCode, playCateName, betPlayCateName)
            }
            return
        }

        odds.second?.getOrNull(0).let {
            val oddBtnHome = oddBtnList.oddBtnHome
            bindOddBtn(oddBtnHome, isDeactivated, playCateCode, it, odds.second, oddsType)
            bindOddClick(oddBtnHome, it, oddButtonListener, matchInfo, playCateCode, playCateName, betPlayCateName)
        }

        odds.second?.getOrNull(1).let {
            val oddBtnAway = oddBtnList.oddBtnAway
            bindOddBtn(oddBtnAway, isDeactivated, playCateCode, it, odds.second, oddsType)
            bindOddClick(oddBtnAway, it, oddButtonListener, matchInfo, playCateCode, playCateName, betPlayCateName)
        }

        val drawOdd = odds.second?.getOrNull(2)
        if (odds.second?.size?: 0 > 2 && drawOdd != null) {
            val oddBtnDraw = oddBtnList.getDrawOddsBtn()
            bindOddBtn(oddBtnDraw, isDeactivated, playCateCode, drawOdd, odds.second, oddsType, isDrawBtn = true)
            bindOddClick(oddBtnDraw, drawOdd, oddButtonListener, matchInfo, playCateCode, playCateName, betPlayCateName)
        } else {
            oddBtnList.disableDrawBtn()
        }
    }

    private fun bindOddClick(oddsButton: OddsButton2,
                             itemOdd: Odd?,
                             oddButtonListener: OddButtonListener?,
                             matchInfo: MatchInfo?,
                             playCateCode: String,
                             playCateName: String,
                             betPlayCateName: String) {

        oddsButton.setOnClickListener {
            itemOdd?.let { odd ->
                //it.isSelected = !it.isSelected
                oddButtonListener?.onClickBet(
                    matchInfo,
                    odd,
                    playCateCode,
                    playCateName,
                    betPlayCateName
                )
            }
        }
    }

    private fun setOddsButtonStatu(
        position: Int,
        itemCount: Int,
        matchInfo: MatchInfo?,
        playCateNameMap: Map<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: Map<String?, Map<String?, String?>?>?,
        odds: Pair<String?, List<Odd?>?>?,
        matchType: MatchType?
    ): Boolean {

        if (matchInfo == null
            || betPlayCateNameMap.isNullOrEmpty()
            || playCateNameMap.isNullOrEmpty()
            || odds == null
            || odds.first == null
            || odds.second.isNullOrEmpty()) {
            oddBtnList.setOddsInvisiable()
            return true
        }

        if (matchType == MatchType.CS && odds.second?.size == 1) {
            oddBtnList.enableOtherOddsBtn()
        } else{
            oddBtnList.enableAllOddsBtn(odds.second?.size?: 0 > 2)
        }

        if (matchInfo.status == null || matchInfo.status == 2 || odds.first.toString().contains("EmptyData")) {
            oddBtnList.setOddsDeactivated()
            return true
        }

        oddBtnList.setBtnTypeVisiable(matchType != MatchType.CS)
        if (odds!!.second?.all { odd -> odd == null || odd.status == BetStatus.DEACTIVATED.code } != false) {
            oddBtnList.setOddsDeactivated()
            return true
        }

        return false
    }

    private fun updateOddsButton2(
        position: Int,
        itemCount: Int,
        matchInfo: MatchInfo?,
        playCateNameMap: Map<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: Map<String?, Map<String?, String?>?>?,
        odds: Pair<String?, List<Odd?>?>?,
        oddsType: OddsType,
        matchType: MatchType?) {

        if (setOddsButtonStatu(
                position,
                itemCount,
                matchInfo,
                playCateNameMap,
                betPlayCateNameMap,
                odds,
                matchType)) {
            return
        }

        val playCateName = playCateNameMap!![odds!!.first]
            ?.getPlayCateName(LanguageManager.getSelectLanguage(itemView.context))
            ?.replace(": ", " ")
            ?.replace("||", "\n") ?: ""

        val playCateCode = odds.first ?: ""
        oddBtnList.oddBtnType.text = playCateName.updatePlayCateColor()

        val isDeactivated = (odds.second == null || odds.second!!.all { it == null })

        if (matchType == MatchType.CS && odds?.second?.size == 1) {
            val oddBtnOther = oddBtnList.getOtherOddsBtn()
            bindOddBtn(oddBtnOther, isDeactivated, playCateCode, odds.second?.getOrNull(0), odds.second, oddsType, isOtherBtn = true)
            return
        }

        bindOddBtn(oddBtnList.oddBtnHome, isDeactivated, playCateCode, odds.second?.getOrNull(0), odds.second, oddsType)
        bindOddBtn(oddBtnList.oddBtnAway, isDeactivated, playCateCode, odds.second?.getOrNull(1), odds.second, oddsType)

        if (odds.second?.size?: 0 > 2) {
            bindOddBtn(oddBtnList.getDrawOddsBtn(), isDeactivated, playCateCode, odds.second?.getOrNull(2), odds.second, oddsType, isDrawBtn = true)
        } else {
            oddBtnList.disableDrawBtn()
        }
    }

    private fun bindOddBtn(oddsButton: OddsButton2,
                           isDeactivated: Boolean,
                           playCateCode: String,
                           itemOdd: Odd?,
                           oddList: List<Odd?>?,
                           oddsType: OddsType,
                           isDrawBtn: Boolean = false,
                           isOtherBtn: Boolean = false) {

        val betStatus = when {
            isDeactivated -> {
                BetStatus.DEACTIVATED.code
            }
            ((oddList?.size?: 0 < 2 || itemOdd?.odds ?: 0.0 <= 0.0) && !isOtherBtn) -> {
                BetStatus.LOCKED.code
            }
            else -> {
                itemOdd?.status
            }
        }

        oddsButton.setupOdd4hall(playCateCode, itemOdd, betStatus, oddsType, isDrawBtn)

    }

    fun String.isCSType(): Boolean {
        return this.contains(PlayCate.CS.value) && !this.isCombination()
    }

    private fun getOddByType(
        odd: Odd?,
        oddsType: OddsType
    ) = if (odd?.isOnlyEUType == true) {
        TextUtil.formatForOdd(odd.odds ?: 1)
    } else {
        when (oddsType) {
            OddsType.EU -> TextUtil.formatForOdd(odd?.odds ?: 1)
            OddsType.HK -> TextUtil.formatForOdd(odd?.hkOdds ?: 0)
            OddsType.MYS -> TextUtil.formatForOdd(odd?.malayOdds ?: 0)
            OddsType.IDN -> TextUtil.formatForOdd(odd?.indoOdds ?: 0)
        }
    }

    private fun PlayCateMapItem.getPlayCateName(l: LanguageManager.Language): String {
        return when (l) {
            LanguageManager.Language.EN -> {
                this.playCateNameEn
            }
            LanguageManager.Language.VI -> {
                this.playCateNameVi
            }
            LanguageManager.Language.TH -> {
                this.playCateName
            }
            else -> {
                this.playCateName
            }
        }
    }

    private val textSpanned by lazy {
        ForegroundColorSpan(Color.parseColor(if (MultiLanguagesApplication.isNightMode) "#a3a3a3" else "#6C7BA8"))
    }
    private val colorSpanned = ForegroundColorSpan(Color.parseColor("#b73a20"))

    private fun String.updatePlayCateColor(): Spanned {

        val spanned = SpannableStringBuilder(this)
        val index = this.indexOf("\n")
        if (index < 0) {
            spanned.setSpan(textSpanned, 0, this.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        } else {
            spanned.setSpan(textSpanned, 0, index, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            spanned.setSpan(colorSpanned, index, this.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }
        return spanned
    }

    override val oddStateChangeListener: OddStateChangeListener
        get() = oddStateRefreshListener

}

class OddButtonListener(
    val clickListenerBet: (matchInfo: MatchInfo?, odd: Odd, playCateCode: String, playCateName: String, betPlayCateName: String) -> Unit
) {

    fun onClickBet(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String = "",
        betPlayCateName: String = "",
    ) = clickListenerBet(matchInfo, odd, playCateCode, playCateName, betPlayCateName)
}