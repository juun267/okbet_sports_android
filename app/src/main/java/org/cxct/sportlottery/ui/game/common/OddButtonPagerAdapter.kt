package org.cxct.sportlottery.ui.game.common

import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_odd_btn_2x2_v6.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.SelectionType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.common.PlayCateMapItem
import org.cxct.sportlottery.ui.game.betList.receipt.DataItem.ParlayTitle.status
import org.cxct.sportlottery.ui.game.widget.OddsButton
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.BetPlayCateFunction.isCombination
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TextUtil
import timber.log.Timber


class OddButtonPagerAdapter :RecyclerView.Adapter<OddButtonPagerViewHolder>() {
    private var matchInfo: MatchInfo?= null
    private var oddsSort: String?= null
    private var playCateNameMap: MutableMap<String?, Map<String?, String?>?>?= null
    private var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?= null //遊戲名稱顯示用playCateNameMap，下注顯示用betPlayCateNameMap
    private var getPlaySelectedCodeSelectionType: Int? = null
    private var matchOdd: MatchOdd? = null

    fun setData(matchInfo: MatchInfo?, oddsSort: String?, playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
                betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?, getPlaySelectedCodeSelectionType: Int?,
                matchOdd: MatchOdd?
    ) {
        this.matchInfo = matchInfo

        if (!oddsSort.isNullOrEmpty())
            this.oddsSort = oddsSort
        this.playCateNameMap = playCateNameMap
        this.betPlayCateNameMap = betPlayCateNameMap
        this.getPlaySelectedCodeSelectionType = getPlaySelectedCodeSelectionType
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
                            getPlaySelectedCodeSelectionType == SelectionType.SELECTABLE.code -> {
                                gameListFilter = this.take(this.size + 1) as MutableList<String>
                            }
                            this.size > sizeCount(matchInfo?.gameType) -> {
                                gameListFilter = this.take(sizeCount(matchInfo?.gameType)) as MutableList<String>
                            }
                            else -> {
                                val maxCount = if(sizeCount(matchInfo?.gameType) < oddsSortCount) sizeCount(matchInfo?.gameType) else oddsSortCount
                                val count = if (sizeCount(matchInfo?.gameType) > this.size) maxCount - this.size else 0

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

    var nowRv: RecyclerView? = null

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
    // endregion

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        nowRv = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OddButtonPagerViewHolder {
        return OddButtonPagerViewHolder.from(parent, oddStateRefreshListener)
    }

    override fun onBindViewHolder(holder: OddButtonPagerViewHolder, position: Int) {
        Log.d("Hewie", "綁定(${matchInfo?.homeName})：賠率表($position)")
        holder.bind(
            matchInfo,
            playCateNameMap,
            betPlayCateNameMap,
            listOf(
                Pair(
                    data[position].getOrNull(0)?.first,
                    data[position].getOrNull(0)?.second
                )),
            oddsType,
            listener,
            matchType
        )
    }

    override fun onBindViewHolder(holder: OddButtonPagerViewHolder, position: Int, payloads: MutableList<Any>) {
        if(payloads.isNullOrEmpty()) { onBindViewHolder(holder, position) }
        else {
            Log.d("Hewie", "更新：賠率表($position)")
            val list = payloads.first() as ArrayList<Pair<String, List<Odd?>?>>
            holder.update(matchInfo, playCateNameMap, betPlayCateNameMap,
                listOf(Pair(list.first().first, list.first().second)),
                oddsType, matchType)
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

    private fun Map<String, List<Odd?>?>.sortMarketSort(): Map<String, List<Odd?>?> {
        return this.toList().sortedBy { it.second?.firstOrNull()?.marketSort }.toMap()
    }
}

@Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
class OddButtonPagerViewHolder private constructor(
    itemView: View,
    private val oddStateRefreshListener: OddStateChangeListener
) : OddStateViewHolder(itemView) {

    fun bind(
        matchInfo: MatchInfo?,
        playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        odds: List<Pair<String?, List<Odd?>?>>?,
        oddsType: OddsType,
        oddButtonListener: OddButtonListener?,
        matchType: MatchType?
    ) {
        setupOddsButton(
            itemView.odd_btn_row1_type,
            itemView.odd_btn_row1_home,
            itemView.odd_btn_row1_away,
            itemView.odd_btn_row1_draw,
            itemView.odd_btn_row1_other,
            matchInfo,
            playCateNameMap,
            betPlayCateNameMap,
            odds?.getOrNull(0), oddsType, oddButtonListener,
            matchType
        )
    }

    fun update(
        matchInfo: MatchInfo?,
        playCateNameMap: Map<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: Map<String?, Map<String?, String?>?>?,
        odds: List<Pair<String?, List<Odd?>?>>?,
        oddsType: OddsType,
        matchType: MatchType?
    ) {
        updateOddsButton(
            itemView.odd_btn_row1_type,
            itemView.odd_btn_row1_home,
            itemView.odd_btn_row1_away,
            itemView.odd_btn_row1_draw,
            itemView.odd_btn_row1_other,
            matchInfo,
            playCateNameMap,
            betPlayCateNameMap,
            odds?.getOrNull(0), oddsType,
            matchType
        )
    }

    private fun <K, V> Map<K, V>?.getPlayCateName(selectLanguage: LanguageManager.Language): String {
        return when (selectLanguage) {
            LanguageManager.Language.EN -> {
                this?.get(LanguageManager.Language.EN.key).toString()
            }
            LanguageManager.Language.VI -> {
                this?.get(LanguageManager.Language.VI.key).toString()
            }
            else -> {
                this?.get(LanguageManager.Language.ZH.key).toString()
            }
        }
    }

    private fun setupOddsButton(
        oddBtnType: TextView,
        oddBtnHome: OddsButton,
        oddBtnAway: OddsButton,
        oddBtnDraw: OddsButton,
        oddBtnOther: OddsButton,
        matchInfo: MatchInfo?,
        playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        odds: Pair<String?, List<Odd?>?>?,
        oddsType: OddsType,
        oddButtonListener: OddButtonListener?,
        matchType: MatchType?
    ) {

        if (matchInfo == null ||
            betPlayCateNameMap.isNullOrEmpty() || playCateNameMap.isNullOrEmpty() ||
            odds == null || odds.first == null || odds.second.isNullOrEmpty()
        ) {
            if(matchType == MatchType.CS && odds?.second?.size == 1){
                oddBtnHome.visibility = View.GONE
                oddBtnAway.visibility = View.GONE
                oddBtnDraw.visibility = View.GONE
                oddBtnOther.visibility = View.INVISIBLE
            }else{
                oddBtnType.visibility = View.INVISIBLE
                oddBtnHome.visibility = View.INVISIBLE
                oddBtnAway.visibility = View.INVISIBLE
                oddBtnDraw.visibility = View.INVISIBLE
                oddBtnOther.visibility = View.GONE
            }
            return
        } else {
            if(matchType == MatchType.CS && odds.second?.size == 1){
                oddBtnHome.visibility = View.GONE
                oddBtnAway.visibility = View.GONE
                oddBtnDraw.visibility = View.GONE
                oddBtnOther.visibility = View.VISIBLE
            }else{
                oddBtnType.visibility = View.VISIBLE
                oddBtnHome.visibility = View.VISIBLE
                oddBtnAway.visibility = View.VISIBLE
                oddBtnDraw.visibility = View.VISIBLE
                oddBtnOther.visibility = View.GONE
            }
        }
        if (matchInfo.status == null || matchInfo.status == 2 || odds.first.toString()
                .contains("EmptyData")
        ) {
            oddBtnType.text = "-"
            oddBtnHome.betStatus = BetStatus.DEACTIVATED.code
            oddBtnAway.betStatus = BetStatus.DEACTIVATED.code
            oddBtnDraw.betStatus = BetStatus.DEACTIVATED.code
            oddBtnOther.betStatus = BetStatus.DEACTIVATED.code
            return
        }
        val replaceScore = odds.second?.firstOrNull()?.replaceScore ?: ""

        var playCateName =
            playCateNameMap[odds.first].getPlayCateName(LanguageManager.getSelectLanguage(itemView.context))
                .replace(": ", " ").replace("||", "\n").replace("{S}", replaceScore).replace("{H}","${matchInfo.homeName}")?.replace("{C}","${matchInfo.awayName}")

        odds.second?.firstOrNull()?.replaceScore?.let { playCateName.replace("{S}", it) }
        if (playCateName == "null" || playCateName.isEmpty()){
            playCateName = "-"
        }

        val betPlayCateName = betPlayCateNameMap[odds.first].getPlayCateName(
            LanguageManager.getSelectLanguage(itemView.context)
        ).replace(": ", " ").replace("||", "\n")
        var playCateCode = odds.first ?: ""
        //去掉mappingCS playCateCode的後綴
        if (playCateCode.contains(PlayCate.CS.value) && playCateCode.contains("_")) {
            playCateCode = playCateCode.split("_")[0]
        }
//        Timber.e("playCateCode: $playCateCode")

        oddBtnType.text = when {
            (odds.second?.all { odd -> odd == null || odd.status == BetStatus.DEACTIVATED.code }
                ?: true) -> itemView.resources.getString(R.string.unknown_data)
            else -> playCateName.updatePlayCateColor()
        }
        if (playCateCode.isCSType()) {
            oddBtnType.visibility = View.INVISIBLE
        }

        if(matchType == MatchType.CS && odds?.second?.size == 1){
            oddBtnOther.apply otherButtonSettings@{
                setupOdd4hall(playCateCode,odds.second?.getOrNull(0), odds.second, oddsType, isOtherBtn = true)
                this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(0))
                setOnClickListener {
                    odds.second?.getOrNull(0)?.let { odd ->
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
        }else{
        oddBtnHome.apply homeButtonSettings@{
            setupOdd4hall(playCateCode,odds.second?.getOrNull(0), odds.second, oddsType)
            this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(0))
            setOnClickListener {
                odds.second?.getOrNull(0)?.let { odd ->
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

        oddBtnAway.apply awayButtonSettings@{
            setupOdd4hall(playCateCode,odds.second?.getOrNull(1), odds.second, oddsType)
            this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(1))
            setOnClickListener {
                odds.second?.getOrNull(1)?.let { odd ->
//                    it.isSelected = !it.isSelected
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

        oddBtnDraw.apply drawButtonSettings@{

            setupOdd4hall(playCateCode, odds.second?.getOrNull(2), odds.second, oddsType, isDrawBtn = true)

            this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(2))

            setOnClickListener {
                odds.second?.getOrNull(2)?.let { odd ->
//                    it.isSelected = !it.isSelected
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
        }
    }

    private fun updateOddsButton(
        oddBtnType: TextView,
        oddBtnHome: OddsButton,
        oddBtnAway: OddsButton,
        oddBtnDraw: OddsButton,
        oddBtnOther: OddsButton,
        matchInfo: MatchInfo?,
        playCateNameMap: Map<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: Map<String?, Map<String?, String?>?>?,
        odds: Pair<String?, List<Odd?>?>?,
        oddsType: OddsType,
        matchType: MatchType?
    ) {
        if (matchInfo == null ||
            betPlayCateNameMap.isNullOrEmpty() || playCateNameMap.isNullOrEmpty() ||
            odds == null || odds.first == null || odds.second.isNullOrEmpty()
        ) {
            oddBtnType.visibility = View.INVISIBLE
            oddBtnHome.visibility = View.INVISIBLE
            oddBtnAway.visibility = View.INVISIBLE
            oddBtnDraw.visibility = View.INVISIBLE
            oddBtnOther.visibility = View.GONE
            return
        } else {
            if(matchType == MatchType.CS && odds.second?.size == 1){
                oddBtnType.visibility = View.GONE
                oddBtnHome.visibility = View.GONE
                oddBtnAway.visibility = View.GONE
                oddBtnDraw.visibility = View.GONE
                oddBtnOther.visibility = View.VISIBLE
            }else{
                oddBtnType.visibility = View.VISIBLE
                oddBtnHome.visibility = View.VISIBLE
                oddBtnAway.visibility = View.VISIBLE
                oddBtnDraw.visibility = View.VISIBLE
                oddBtnOther.visibility = View.GONE
            }
        }
        if (matchInfo.status == null || matchInfo.status == 2 || odds.first.toString()
                .contains("EmptyData")
        ) {
            oddBtnType.text = "-"
            oddBtnHome.betStatus = BetStatus.DEACTIVATED.code
            oddBtnAway.betStatus = BetStatus.DEACTIVATED.code
            oddBtnDraw.betStatus = BetStatus.DEACTIVATED.code
            oddBtnOther.betStatus = BetStatus.DEACTIVATED.code
            return
        }
        val playCateName =
            playCateNameMap[odds.first].getPlayCateName(LanguageManager.getSelectLanguage(itemView.context))
                .replace(": ", " ").replace("||", "\n")
        val playCateCode = odds.first ?: ""
        oddBtnType.text = when {
            (odds.second?.all { odd -> odd == null || odd.status == BetStatus.DEACTIVATED.code }
                ?: true) -> itemView.resources.getString(R.string.unknown_data)
            else -> playCateName.updatePlayCateColor()
        }
        if (playCateCode.isCSType()) {
            oddBtnType.visibility = View.INVISIBLE
        }
        if(matchType == MatchType.CS && odds?.second?.size == 1){
            oddBtnOther.apply homeButtonSettings@{
                setupOdd4hall(playCateCode,odds.second?.getOrNull(0), odds.second, oddsType, isOtherBtn = true)
                this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(0))
            }
        } else {
            oddBtnHome.apply homeButtonSettings@{
                setupOdd4hall(playCateCode,odds.second?.getOrNull(0), odds.second, oddsType)
                this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(0))
            }
            oddBtnAway.apply awayButtonSettings@{
                setupOdd4hall(playCateCode,odds.second?.getOrNull(1), odds.second, oddsType)
                this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(1))
            }
            oddBtnDraw.apply drawButtonSettings@{
                setupOdd4hall(playCateCode, odds.second?.getOrNull(2), odds.second, oddsType, isDrawBtn = true)
                this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(2))
            }
        }
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
            else -> {
                this.playCateName
            }
        }
    }

    private fun String.updatePlayCateColor(): Spanned {
        val color =  if (MultiLanguagesApplication.isNightMode) "#a3a3a3"
        else "#666666"

        return Html.fromHtml(
            when {
                (this.contains("\n")) -> {
                    val strSplit = this.split("\n")
                    "<font color=$color>${strSplit.first()}</font><br><font color=#b73a20>${
                        strSplit.getOrNull(
                            1
                        )
                    }</font>"
                }
                else -> {
                    "<font color=$color>${this}</font>"
                }
            }
        )
    }

    override val oddStateChangeListener: OddStateChangeListener
        get() = oddStateRefreshListener

    companion object {
        fun from(
            parent: ViewGroup,
            oddStateRefreshListener: OddStateChangeListener
        ): OddButtonPagerViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater
                .inflate(R.layout.itemview_odd_btn_2x2_v6, parent, false)

            return OddButtonPagerViewHolder(view, oddStateRefreshListener)
        }
    }
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