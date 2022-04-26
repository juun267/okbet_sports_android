package org.cxct.sportlottery.ui.game.common

import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.button_odd_detail.view.*
import kotlinx.android.synthetic.main.itemview_odd_btn_2x2_v6.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.SelectionType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.common.PlayCateMapItem
import org.cxct.sportlottery.ui.game.widget.OddsButton
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.QuickListManager
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds
import java.lang.Exception


class OddButtonPagerAdapter :RecyclerView.Adapter<OddButtonPagerViewHolder>() {
    private var matchInfo: MatchInfo?= null
    private var oddsSort: String?= null
    private var playCateNameMap: MutableMap<String?, Map<String?, String?>?>?= null
    private var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?= null //遊戲名稱顯示用playCateNameMap，下注顯示用betPlayCateNameMap
    private var getPlaySelectedCodeSelectionType: Int? = null

    fun setData(matchInfo: MatchInfo?, oddsSort: String?, playCateNameMap: MutableMap<String?, Map<String?, String?>?>?, betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?, getPlaySelectedCodeSelectionType: Int?) {
        this.matchInfo = matchInfo

        if (!oddsSort.isNullOrEmpty())
            this.oddsSort = oddsSort

        this.playCateNameMap = playCateNameMap
        this.betPlayCateNameMap = betPlayCateNameMap
        this.getPlaySelectedCodeSelectionType = getPlaySelectedCodeSelectionType
    }

    var odds: Map<String, List<Odd?>?> = mapOf()
        set(value) {
            this.playCateNameMap = playCateNameMap.addSplitPlayCateTranslation()
            val oddsSortCount = oddsSort?.split(",")?.size ?: 999 // 最大顯示數量
            field = value.sortScores().refactorPlayCode().sortOdds().filterOddsStatus().splitPlayCate()
                .filterPlayCateSpanned().sortPlayCate()
            val gameList =
                field.filterValues { !it.isNullOrEmpty() }
                    .plus(field.filterValues { !it.isNullOrEmpty() }
                        .filter { it.value?.getOrNull(0) == null }).map { it.key }.run {
                        val gameListFilter: MutableList<String>

                        when{
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
            listener
        )
    }

    override fun onBindViewHolder(holder: OddButtonPagerViewHolder, position: Int, payloads: MutableList<Any>) {
        if(payloads.isNullOrEmpty()) { onBindViewHolder(holder, position) }
        else {
            Log.d("Hewie", "更新：賠率表($position)")
            val list = payloads.first() as ArrayList<Pair<String, List<Odd?>?>>
            holder.update(matchInfo, playCateNameMap, betPlayCateNameMap,
                listOf(Pair(list.first().first, list.first().second)),
                oddsType)
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
    ) {
        setupOddsButton(
            itemView.odd_btn_row1_type,
            itemView.odd_btn_row1_home,
            itemView.odd_btn_row1_away,
            itemView.odd_btn_row1_draw,
            matchInfo,
            playCateNameMap,
            betPlayCateNameMap,
            odds?.getOrNull(0), oddsType, oddButtonListener
        )
    }

    fun update(
        matchInfo: MatchInfo?,
        playCateNameMap: Map<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: Map<String?, Map<String?, String?>?>?,
        odds: List<Pair<String?, List<Odd?>?>>?,
        oddsType: OddsType,
    ) {
        updateOddsButton(
            itemView.odd_btn_row1_type,
            itemView.odd_btn_row1_home,
            itemView.odd_btn_row1_away,
            itemView.odd_btn_row1_draw,
            matchInfo,
            playCateNameMap,
            betPlayCateNameMap,
            odds?.getOrNull(0), oddsType
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
        matchInfo: MatchInfo?,
        playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        odds: Pair<String?, List<Odd?>?>?,
        oddsType: OddsType,
        oddButtonListener: OddButtonListener?
    ) {
        if (matchInfo == null ||
            betPlayCateNameMap.isNullOrEmpty() || playCateNameMap.isNullOrEmpty() ||
            odds == null || odds.first == null || odds.second.isNullOrEmpty()
        ) {
            oddBtnType.visibility = View.INVISIBLE
            oddBtnHome.visibility = View.INVISIBLE
            oddBtnAway.visibility = View.INVISIBLE
            oddBtnDraw.visibility = View.INVISIBLE
            return
        } else {
            oddBtnType.visibility = View.VISIBLE
            oddBtnHome.visibility = View.VISIBLE
            oddBtnAway.visibility = View.VISIBLE
            oddBtnDraw.visibility = View.VISIBLE
        }

        if (matchInfo.status == null || matchInfo.status == 2 || odds.first.toString()
                .contains("EmptyData")
        ) {
            oddBtnType.text = "-"
            oddBtnHome.betStatus = BetStatus.DEACTIVATED.code
            oddBtnAway.betStatus = BetStatus.DEACTIVATED.code
            oddBtnDraw.betStatus = BetStatus.DEACTIVATED.code
            return
        }

        val replaceScore = odds.second?.firstOrNull()?.replaceScore ?: ""

        var playCateName =
            playCateNameMap[odds.first].getPlayCateName(LanguageManager.getSelectLanguage(itemView.context))
                .replace(": ", " ").replace("||", "\n").replace("{S}", replaceScore)

        odds.second?.firstOrNull()?.replaceScore?.let { playCateName.replace("{S}", it) }

        if (playCateName == "null" || playCateName.isNullOrEmpty()){
            playCateName = "-"
        }

        val betPlayCateName = betPlayCateNameMap[odds.first].getPlayCateName(
            LanguageManager.getSelectLanguage(itemView.context)
        ).replace(": ", " ").replace("||", "\n")

        val playCateCode = odds.first ?: ""

        oddBtnType.text = when {
            (odds.second?.all { odd -> odd == null || odd.status == BetStatus.DEACTIVATED.code }
                ?: true) -> itemView.resources.getString(R.string.unknown_data)

            else -> playCateName.updatePlayCateColor()
        }

        oddBtnHome.apply homeButtonSettings@{
            when {
                (odds.second == null || odds.second?.all { odd -> odd == null } == true) -> {
                    betStatus = BetStatus.DEACTIVATED.code
                    return@homeButtonSettings
                }
                (odds.second?.size ?: 0 < 2 || odds.second?.getOrNull(0)?.odds ?: 0.0 <= 0.0) -> {
                    betStatus = BetStatus.LOCKED.code
                    return@homeButtonSettings
                }
                else -> {
                    betStatus = odds.second?.getOrNull(0)?.status
                }
            }

            tv_name.apply {
                visibility = when {
                    playCateCode.isOUType() || playCateCode.isOEType() || playCateCode.isBTSType() || playCateCode.isNOGALType() -> View.VISIBLE
                    else -> {
//                        when (!odds.second?.getOrNull(0)?.spread.isNullOrEmpty()) {
//                            true -> View.INVISIBLE
//                            false -> View.GONE
//                        }
                        View.GONE
                    }
                }

                text = when {
                    playCateCode.isOUType() || playCateCode.isOEType() || playCateCode.isBTSType() -> {
                        (odds.second?.getOrNull(0)?.nameMap?.get(
                            LanguageManager.getSelectLanguage(
                                context
                            ).key
                        ) ?: odds.second?.getOrNull(0)?.name)?.abridgeOddsName()
                    }
                    playCateCode.isNOGALType() -> {
                        when (LanguageManager.getSelectLanguage(this.context)) {
                            LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> {
                                "第" + odds.second?.getOrNull(0)?.nextScore.toString()
                            }
                            else -> {
                                getOrdinalNumbers(odds.second?.getOrNull(0)?.nextScore.toString())
                            }
                        }
                    }
                    else -> ""
                }
            }

            tv_spread.apply {
                visibility = when (!odds.second?.getOrNull(0)?.spread.isNullOrEmpty()) {
                    true -> View.VISIBLE
                    false -> {
                        when {
                            playCateCode.isOUType() -> View.INVISIBLE
                            else -> View.GONE
                        }
                    }
                }

                text = odds.second?.getOrNull(0)?.spread ?: ""
            }

            tv_odds.text = getOddByType(odds.second?.getOrNull(0), oddsType)
            tv_odds.setTextColor(oddColorStateList(odds.second?.getOrNull(0), oddsType))

            this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(0))

//            isSelected = odds.second?.getOrNull(0)?.isSelected ?: false

            //isSelected = QuickListManager.getQuickSelectedList()?.contains(odds.second?.getOrNull(0)?.id) ?: false
            isSelected = QuickListManager.getQuickSelectedList()?.contains(odds.second?.getOrNull(0)?.id) ?: false

            setOnClickListener {
                odds.second?.getOrNull(0)?.let { odd ->
                    it.isSelected = !it.isSelected
                    oddButtonListener?.onClickBet(
                        matchInfo,
                        odd,
                        odds.first ?: "",
                        playCateName,
                        betPlayCateName
                    )
                }
            }
        }

        oddBtnAway.apply awayButtonSettings@{
            when {
                (odds.second == null || odds.second?.all { odd -> odd == null } == true) -> {
                    betStatus = BetStatus.DEACTIVATED.code
                    return@awayButtonSettings
                }
                (odds.second?.size ?: 0 < 2 || odds.second?.getOrNull(1)?.odds ?: 0.0 <= 0.0) -> {
                    betStatus = BetStatus.LOCKED.code
                    return@awayButtonSettings
                }
                else -> {
                    betStatus = odds.second?.getOrNull(1)?.status
                }
            }

            tv_name.apply {
                visibility = when {
                    playCateCode.isOUType() || playCateCode.isOEType() || playCateCode.isBTSType() || playCateCode.isNOGALType() -> View.VISIBLE
                    else -> {
//                        when (!odds.second?.getOrNull(1)?.spread.isNullOrEmpty()) {
//                            true -> View.INVISIBLE
//                            false -> View.GONE
//                        }
                        View.GONE
                    }
                }

                text = when {
                    playCateCode.isOUType() || playCateCode.isOEType() || playCateCode.isBTSType() -> {
                        (odds.second?.getOrNull(1)?.nameMap?.get(
                            LanguageManager.getSelectLanguage(
                                context
                            ).key
                        ) ?: odds.second?.getOrNull(1)?.name)?.abridgeOddsName()
                    }
                    playCateCode.isNOGALType() -> {
                        when (LanguageManager.getSelectLanguage(this.context)) {
                            LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> {
                                "第" + odds.second?.getOrNull(1)?.nextScore.toString()
                            }
                            else -> {
                                getOrdinalNumbers(odds.second?.getOrNull(1)?.nextScore.toString())
                            }
                        }
                    }
                    else -> ""
                }
            }

            tv_spread.apply {
                visibility = when (!odds.second?.getOrNull(1)?.spread.isNullOrEmpty()) {
                    true -> View.VISIBLE
                    false -> {
                        when {
                            playCateCode.isOUType() -> View.INVISIBLE
                            else -> View.GONE
                        }
                    }
                }

                text = odds.second?.getOrNull(1)?.spread ?: ""
            }

            tv_odds.text = getOddByType(odds.second?.getOrNull(1), oddsType)
            tv_odds.setTextColor(oddColorStateList(odds.second?.getOrNull(1), oddsType))

            if (getOdds(odds.second?.getOrNull(1), oddsType) < 0.0) {
                tv_odds.setTextColor(
                    ContextCompat.getColorStateList(
                        context,
                        R.color.selector_button_odd_bottom_text_red
                    )
                )
            } else {
                tv_odds.setTextColor(
                    ContextCompat.getColorStateList(
                        context,
                        R.color.selector_button_odd_bottom_text
                    )
                )
            }

            this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(1))

            isSelected = QuickListManager.getQuickSelectedList()?.contains(odds.second?.getOrNull(1)?.id) ?: false

            setOnClickListener {
                odds.second?.getOrNull(1)?.let { odd ->
                    it.isSelected = !it.isSelected
                    oddButtonListener?.onClickBet(
                        matchInfo,
                        odd,
                        odds.first ?: "",
                        playCateName,
                        betPlayCateName
                    )
                }
            }
        }

        oddBtnDraw.apply drawButtonSettings@{
            when{
                (odds.second?.size ?: 0 > 2) -> {
                    visibility = View.VISIBLE
                }
                (odds.second?.size ?: 0 < 3) -> {
                    visibility = View.INVISIBLE
                }
            }
            when {
                odds.second?.all { odd -> odd == null } == true -> {
                    betStatus = BetStatus.DEACTIVATED.code
                    return@drawButtonSettings
                }
                (odds.second?.getOrNull(2)?.odds ?: 0.0 <= 0.0) -> {
                    betStatus = BetStatus.LOCKED.code
                    return@drawButtonSettings
                }
                else -> {
                    betStatus = odds.second?.getOrNull(2)?.status
                }
            }

            tv_name.apply {
                visibility = View.VISIBLE

                text = when {
                    playCateCode.isNOGALType() -> {
                        when (LanguageManager.getSelectLanguage(this.context)) {
                            LanguageManager.Language.ZH, LanguageManager.Language.ZHT -> {
                                "无"
                            }
                            else -> {
                                "None"
                            }
                        }
                    }
                    playCateCode.isCombination() -> {
                        (odds.second?.getOrNull(2)?.nameMap?.get(
                            LanguageManager.getSelectLanguage(context).key
                        ) ?: odds.second?.getOrNull(2)?.name)?.split("-")?.firstOrNull() ?: ""
                    }
                    !playCateCode.isCombination() -> {
                        odds.second?.getOrNull(2)?.nameMap?.get(
                            LanguageManager.getSelectLanguage(context).key
                        ) ?: odds.second?.getOrNull(2)?.name
                    }
                    else -> ""
                }
            }

            tv_spread.apply {
                visibility = when (!odds.second?.getOrNull(2)?.spread.isNullOrEmpty()) {
                    true -> View.VISIBLE
                    false -> {
                        when {
                            playCateCode.isOUType() -> View.INVISIBLE
                            else -> View.GONE
                        }
                    }
                }

                text = odds.second?.getOrNull(2)?.spread ?: ""

            }

            tv_odds.text = getOddByType(odds.second?.getOrNull(2), oddsType)
            tv_odds.setTextColor(oddColorStateList(odds.second?.getOrNull(2), oddsType))

            this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(2))

            isSelected = QuickListManager.getQuickSelectedList()?.contains(odds.second?.getOrNull(2)?.id) ?: false

            setOnClickListener {
                odds.second?.getOrNull(2)?.let { odd ->
                    it.isSelected = !it.isSelected
                    oddButtonListener?.onClickBet(
                        matchInfo,
                        odd,
                        odds.first ?: "",
                        playCateName,
                        betPlayCateName
                    )
                }
            }
        }
    }

    private fun updateOddsButton(
        oddBtnType: TextView,
        oddBtnHome: OddsButton,
        oddBtnAway: OddsButton,
        oddBtnDraw: OddsButton,
        matchInfo: MatchInfo?,
        playCateNameMap: Map<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: Map<String?, Map<String?, String?>?>?,
        odds: Pair<String?, List<Odd?>?>?,
        oddsType: OddsType,
    ) {
        if (matchInfo == null ||
            betPlayCateNameMap.isNullOrEmpty() || playCateNameMap.isNullOrEmpty() ||
            odds == null || odds.first == null || odds.second.isNullOrEmpty()
        ) {
            oddBtnType.visibility = View.INVISIBLE
            oddBtnHome.visibility = View.INVISIBLE
            oddBtnAway.visibility = View.INVISIBLE
            oddBtnDraw.visibility = View.INVISIBLE
            return
        } else {
            oddBtnType.visibility = View.VISIBLE
            oddBtnHome.visibility = View.VISIBLE
            oddBtnAway.visibility = View.VISIBLE
            oddBtnDraw.visibility = View.VISIBLE
        }
        if (matchInfo.status == null || matchInfo.status == 2 || odds.first.toString()
                .contains("EmptyData")
        ) {
            oddBtnType.text = "-"
            oddBtnHome.betStatus = BetStatus.DEACTIVATED.code
            oddBtnAway.betStatus = BetStatus.DEACTIVATED.code
            oddBtnDraw.betStatus = BetStatus.DEACTIVATED.code
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
        oddBtnHome.apply homeButtonSettings@{
            when {
                (odds.second == null || odds.second?.all { odd -> odd == null } == true) -> {
                    betStatus = BetStatus.DEACTIVATED.code
                    return@homeButtonSettings
                }
                (odds.second?.size ?: 0 < 2 || odds.second?.getOrNull(0)?.odds ?: 0.0 <= 0.0) -> {
                    betStatus = BetStatus.LOCKED.code
                    return@homeButtonSettings
                }
                else -> {
                    betStatus = odds.second?.getOrNull(0)?.status
                }
            }
            tv_name.apply {
                visibility = when {
                    playCateCode.isOUType() || playCateCode.isOEType() || playCateCode.isBTSType() || playCateCode.isNOGALType() -> View.VISIBLE
                    else -> {
                        when (!odds.second?.getOrNull(0)?.spread.isNullOrEmpty()) {
                            true -> View.INVISIBLE
                            false -> View.GONE
                        }
                    }
                }
                text = when {
                    playCateCode.isOUType() || playCateCode.isOEType() || playCateCode.isBTSType() -> {
                        (odds.second?.getOrNull(0)?.nameMap?.get(
                            LanguageManager.getSelectLanguage(
                                context
                            ).key
                        ) ?: odds.second?.getOrNull(0)?.name)?.abridgeOddsName()
                    }
                    playCateCode.isNOGALType() -> {
                        "第" + odds.second?.getOrNull(0)?.nextScore.toString()
                    }
                    else -> ""
                }
            }
            tv_spread.apply {
                visibility = when (!odds.second?.getOrNull(0)?.spread.isNullOrEmpty()) {
                    true -> View.VISIBLE
                    false -> {
                        when {
                            playCateCode.isOUType() -> View.INVISIBLE
                            else -> View.GONE
                        }
                    }
                }
                text = odds.second?.getOrNull(0)?.spread ?: ""
            }
            tv_odds.text = getOddByType(odds.second?.getOrNull(0), oddsType)
            tv_odds.setTextColor(oddColorStateList(odds.second?.getOrNull(0), oddsType))
            this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(0))
            isSelected = odds.second?.getOrNull(0)?.isSelected ?: false
        }
        oddBtnAway.apply awayButtonSettings@{
            when {
                (odds.second == null || odds.second?.all { odd -> odd == null } == true) -> {
                    betStatus = BetStatus.DEACTIVATED.code
                    return@awayButtonSettings
                }
                (odds.second?.size ?: 0 < 2 || odds.second?.getOrNull(1)?.odds ?: 0.0 <= 0.0) -> {
                    betStatus = BetStatus.LOCKED.code
                    return@awayButtonSettings
                }
                else -> {
                    betStatus = odds.second?.getOrNull(1)?.status
                }
            }
            tv_name.apply {
                visibility = when {
                    playCateCode.isOUType() || playCateCode.isOEType() || playCateCode.isBTSType() || playCateCode.isNOGALType() -> View.VISIBLE
                    else -> {
//                        when (!odds.second?.getOrNull(1)?.spread.isNullOrEmpty()) {
//                            true -> View.INVISIBLE
//                            false -> View.GONE
//                        }
                        View.GONE
                    }
                }

                text = when {
                    playCateCode.isOUType() || playCateCode.isOEType() || playCateCode.isBTSType() -> {
                        (odds.second?.getOrNull(1)?.nameMap?.get(
                            LanguageManager.getSelectLanguage(
                                context
                            ).key
                        ) ?: odds.second?.getOrNull(1)?.name)?.abridgeOddsName()
                    }
                    playCateCode.isNOGALType() -> {
                        "第" + odds.second?.getOrNull(1)?.nextScore.toString()
                    }
                    else -> ""
                }
            }
            tv_spread.apply {
                visibility = when (!odds.second?.getOrNull(1)?.spread.isNullOrEmpty()) {
                    true -> View.VISIBLE
                    false -> {
                        when {
                            playCateCode.isOUType() -> View.INVISIBLE
                            else -> View.GONE
                        }
                    }
                }

                text = odds.second?.getOrNull(1)?.spread ?: ""
            }
            tv_odds.text = getOddByType(odds.second?.getOrNull(1), oddsType)
            tv_odds.setTextColor(oddColorStateList(odds.second?.getOrNull(1), oddsType))
            if (getOdds(odds.second?.getOrNull(1), oddsType) < 0.0) {
                tv_odds.setTextColor(
                    ContextCompat.getColorStateList(
                        context,
                        R.color.selector_button_odd_bottom_text_red
                    )
                )
            } else {
                tv_odds.setTextColor(
                    ContextCompat.getColorStateList(
                        context,
                        R.color.selector_button_odd_bottom_text
                    )
                )
            }
            this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(1))
            isSelected = odds.second?.getOrNull(1)?.isSelected ?: false
        }
        oddBtnDraw.apply drawButtonSettings@{
            when{
                (odds.second?.size ?: 0 > 2) -> {
                    visibility = View.VISIBLE
                }
                (odds.second?.size ?: 0 < 3) -> {
                    visibility = View.INVISIBLE
                }
            }

            when {
                odds.second?.all { odd -> odd == null } == true -> {
                    betStatus = BetStatus.DEACTIVATED.code
                    return@drawButtonSettings
                }
                (odds.second?.getOrNull(2)?.odds ?: 0.0 <= 0.0) -> {
                    betStatus = BetStatus.LOCKED.code
                    return@drawButtonSettings
                }
                else -> {
                    betStatus = odds.second?.getOrNull(2)?.status
                }
            }
            tv_name.apply {
                visibility = View.VISIBLE

                text = when {
                    playCateCode.isNOGALType() -> "无"
                    playCateCode.isCombination() -> {
                        (odds.second?.getOrNull(2)?.nameMap?.get(
                            LanguageManager.getSelectLanguage(context).key
                        ) ?: odds.second?.getOrNull(2)?.name)?.split("-")?.firstOrNull() ?: ""
                    }
                    !playCateCode.isCombination() -> {
                        odds.second?.getOrNull(2)?.nameMap?.get(
                            LanguageManager.getSelectLanguage(context).key
                        ) ?: odds.second?.getOrNull(2)?.name
                    }
                    else -> ""
                }
            }
            tv_spread.apply {
                visibility = when (!odds.second?.getOrNull(2)?.spread.isNullOrEmpty()) {
                    true -> View.VISIBLE
                    false -> {
                        when {
                            playCateCode.isOUType() -> View.INVISIBLE
                            else -> View.GONE
                        }
                    }
                }

                text = odds.second?.getOrNull(2)?.spread ?: ""

            }
            tv_odds.text = getOddByType(odds.second?.getOrNull(2), oddsType)
            tv_odds.setTextColor(oddColorStateList(odds.second?.getOrNull(2), oddsType))
            this@OddButtonPagerViewHolder.setupOddState(this, odds.second?.getOrNull(2))
            isSelected = odds.second?.getOrNull(2)?.isSelected ?: false
        }
    }

    private fun getOrdinalNumbers(number:String):String {
        return when (number) {
            "1" -> "1st"
            "2" -> "2nd"
            "3" -> "3rd"
            else -> "${number}th"
        }
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

    private fun OddsButton.oddColorStateList(
        odd: Odd?,
        oddsType: OddsType
    ) = if (getOdds(odd, oddsType) < 0.0) {
        ContextCompat.getColorStateList(
            context,
            R.color.selector_button_odd_bottom_text_red
        )
    } else {
        ContextCompat.getColorStateList(
            context,
            R.color.selector_button_odd_bottom_text
        )
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

    private fun String.isOUType(): Boolean {
        return this.contains(PlayCate.OU.value) && !this.isCombination()
    }

    private fun String.isOEType(): Boolean {
        return (this.contains(PlayCate.OE.value) || this.contains(PlayCate.Q_OE.value)) && !this.isCombination()
    }

    private fun String.isBTSType(): Boolean {
        return this.contains(PlayCate.BTS.value) && !this.isCombination()
    }

    private fun String.isCombination(): Boolean {
        return this.contains(PlayCate.SINGLE_OU.value) || this.contains(PlayCate.SINGLE_BTS.value)
    }

    private fun String.isNOGALType(): Boolean {
        return (this.contains(PlayCate.NGOAL.value) || this.contains(PlayCate.NGOAL_OT.value)) && !this.isCombination()
    }

    /**
     * 後端回傳文字需保留完整文字, 文字顯示縮減由前端自行處理
     */
    private fun String.abridgeOddsName(): String {
        return this.replace("Over", "O").replace("Under", "U")
    }

    private fun String.updatePlayCateColor(): Spanned {
        return Html.fromHtml(
            when {
                (this.contains("\n")) -> {
                    val strSplit = this.split("\n")
                    "<font color=#666666>${strSplit.first()}</font><br><font color=#b73a20>${
                        strSplit.getOrNull(
                            1
                        )
                    }</font>"
                }
                else -> {
                    "<font color=#666666>${this}</font>"
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