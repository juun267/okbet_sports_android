package org.cxct.sportlottery.ui.sport.detail.adapter2

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LineHeightSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.getSpans
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.content_odds_detail_list_team.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.detail.FGLGType
import org.cxct.sportlottery.ui.sport.detail.OddsDetailListData
import org.cxct.sportlottery.ui.sport.detail.adapter.*
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.view.IndicatorView
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import java.util.*

class OddsDetailVH (
    private val oddsAdapter: OddsDetailListAdapter2,
    private val viewType: Int,
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    private val tvGameName: TextView? = itemView.findViewById(R.id.tv_game_name)
    private val oddsDetailPin: ImageView? = itemView.findViewById(R.id.odd_detail_pin)
    private val clItem: ConstraintLayout? = itemView.findViewById(R.id.cl_item)

    val rvBet: RecyclerView? = itemView.findViewById(R.id.rv_bet)

    private val odd_detail_fold = itemView.findViewById<View>(R.id.odd_detail_fold)

    //cs
    private val rvHome: RecyclerView? = itemView.findViewById(R.id.rv_home)
    private val rvDraw: RecyclerView? = itemView.findViewById(R.id.rv_draw)
    private val rvAway: RecyclerView? = itemView.findViewById(R.id.rv_away)

    //FGLG
    private val tvFg: TextView? = itemView.findViewById(R.id.tv_fg)
    private val tvLg: TextView? = itemView.findViewById(R.id.tv_lg)

    //SCO, CS
    private val tvHomeName: TextView? = itemView.findViewById(R.id.tv_home_name)
    private val tvAwayName: TextView? = itemView.findViewById(R.id.tv_away_name)

    private fun bindGameName(oddsDetail: OddsDetailListData) {
        /**
         * tvGameName比賽狀態顯示細體的規則：
         * 籃球：玩法有 -SEG("第N盤") -1ST("上半場") -2ST("下半場")
         * 足球：玩法有 -1ST("上半場") -2ST("下半場")，-SEG會是時間
         * 網球：玩法有 -SEG("第N盤") -1ST("上半場") -2ST("下半場")，如果有 -SEG要判斷不能有CHAMP,CS-SEG1(第1盘正确比分)也是例外
         * 排球：玩法有 -SEG("第N盤")
         * */

        if (tvGameName == null) {
            return
        }

        val context = itemView.context
        if (oddsAdapter.sportCode == GameType.BK) {
            tvGameName.text = when {
                oddsDetail.gameType.isEndScoreType() -> {
                    getTitleNormal(oddsDetail)
                }

                oddsDetail.gameType.contains("-SEG")
                        || oddsDetail.gameType.contains("-1ST")
                        || oddsDetail.gameType.contains("-2ST")
                -> {
                    getTitle(context, oddsDetail)
                }

                else -> {
                    getTitleNormal(oddsDetail)
                }
            }
            return
        }

        if (oddsAdapter.sportCode == GameType.FT) {
            tvGameName.text = when {/*PlayCate.needShowCurrentCorner(oddsDetail.gameType) -> {
                        getTotalCornerTitle(oddsDetail)
                    }*/

                oddsDetail.gameType.contains("-1ST") || oddsDetail.gameType.contains("-2ST") -> {
                    val titleSpannableStringBuilder = getTitle(context, oddsDetail)
                    if (PlayCate.needShowCurrentCorner(oddsDetail.gameType)) {
                        getTotalCornerTitle(titleSpannableStringBuilder)
                    } else {
                        titleSpannableStringBuilder
                    }
                }

                else -> {
                    val titleSpannableStringBuilder = getTitleNormal(oddsDetail)
                    if (PlayCate.needShowCurrentCorner(oddsDetail.gameType)) {
                        getTotalCornerTitle(titleSpannableStringBuilder)
                    } else {
                        titleSpannableStringBuilder
                    }
                }
            }

            return
        }

        if (oddsAdapter.sportCode == GameType.TN) {
            tvGameName.text = if ((oddsDetail.gameType.contains("-SEG") && !oddsDetail.gameType.contains("CHAMP") && !oddsDetail.gameType.contains("CS-SEG"))
                || oddsDetail.gameType.contains("-1ST") || oddsDetail.gameType.contains("-2ST")
            ) {
                getTitle(context, oddsDetail)
            } else {
                getTitleNormal(oddsDetail)
            }
            return
        }

        if (oddsAdapter.sportCode == GameType.VB) {
            tvGameName.text = if(oddsDetail.gameType.contains("-SEG")) {
                getTitle(context, oddsDetail)
            } else {
                getTitleNormal(oddsDetail)
            }
            return
        }

        if (oddsAdapter.sportCode == GameType.CK) {
            val odd = oddsDetail.oddArrayList.first()
            val extInfoStr = odd?.extInfoMap?.get(LanguageManager.getSelectLanguage().key)?: odd?.extInfo
            tvGameName.text = if (oddsDetail.gameType.contains("-SEG")) {
                getTitle(context, oddsDetail)
            } else {
                getTitleNormal(oddsDetail)
            }
            if (!extInfoStr.isNullOrEmpty()) {
                tvGameName.text = tvGameName.text.toString().replace("{E}", extInfoStr)
            }
            return
        }

        tvGameName.text = getTitleNormal(oddsDetail)
    }

    fun bindModel(oddsDetail: OddsDetailListData, payloads: MutableList<Any>? = null) {


        bindGameName(oddsDetail)

        oddsDetailPin?.apply {
            isActivated = oddsDetail.isPin
            setOnClickListener { oddsAdapter.oddsDetailListener?.invoke(oddsDetail.gameType) }
        }

        odd_detail_fold.isSelected = oddsDetail.isExpand
        clItem?.setOnClickListener {
            oddsDetail.isExpand = !oddsDetail.isExpand
            oddsAdapter.notifyItemChanged(absoluteAdapterPosition)
            odd_detail_fold.isSelected = oddsDetail.isExpand

        }
        rvBet?.isVisible = oddsDetail.isExpand
        itemView.lin_match?.isVisible = oddsDetail.isExpand && PlayCate.needShowHomeAndAway(oddsDetail.gameType)
        val gameType = oddsDetail.gameType

        if (viewType == oddsAdapter.SINGLE) {
            forSingle(oddsDetail, 3, payloads)
            return
        }

        if (viewType == oddsAdapter.SINGLE_2_ITEM) {
            forSingle(oddsDetail, 2, payloads)
            return
        }

        if (viewType == oddsAdapter.SINGLE_2_CS) {
            forSingleCS(oddsDetail)
            return
        }

        if (viewType == oddsAdapter.EPS) {
            forEPS(oddsDetail)
            return
        }

        // 以上是通用完玩法样式数据绑定
        if (oddsAdapter.sportCode == GameType.FT) {
            bindFTGameTypeOdds(gameType, oddsDetail)
            return
        }

        oneList(oddsDetail)

    }

    private fun bindFTGameTypeOdds(gameType: String, oddsDetail: OddsDetailListData) {
        when (viewType) {

            oddsAdapter.CS -> {
                if (gameType == PlayCate.CS.value
                    || gameType == PlayCate.CS_OT.value
                    || gameType == PlayCate.CS_1ST_SD.value) {
                    forCS(oddsDetail)
                } else if (gameType == PlayCate.CS_OT.value) {
                    forLCS(oddsDetail)
                }
            }

            oddsAdapter.FG_LG -> {
                forFGLG(oddsDetail)
            }

            oddsAdapter.SCO -> {
                forSCO(oddsDetail, bindingAdapterPosition)
            }

            oddsAdapter.GROUP_6 -> {
                if (gameType == PlayCate.SINGLE_OU.value || gameType == PlayCate.SINGLE_BTS.value || gameType == PlayCate.SINGLE_FLG.value) {
                    group6Item(oddsDetail)
                } else if (gameType == PlayCate.DC_OU.value || gameType == PlayCate.DC_BTS.value || gameType == PlayCate.DC_FLG.value) {
                    group6ItemForDC(oddsDetail)
                }
            }

            oddsAdapter.GROUP_4 -> {
                if (gameType == PlayCate.OU_BTS.value) {
                    group4ItemForOuBts(oddsDetail)
                } else if (gameType == PlayCate.OU_OE.value || gameType == PlayCate.OU_TTS1ST.value) {
                    group4ItemForOuTag(oddsDetail)
                }
            }

            else -> {
                oneList(oddsDetail)
            }
        }
    }

    private fun getTitle(context: Context, oddsDetail: OddsDetailListData): SpannableStringBuilder {

        val textColor = ContextCompat.getColor(context, R.color.color_909090_666666)
        val gameTitleContentBuilder = SpannableStringBuilder()
        val nameLanguaged = oddsDetail.nameMap?.get(LanguageManager.getSelectLanguage().key)
        val statusWordLast = nameLanguaged?.split("-", "–")?.last() ?: ""
        val playName = nameLanguaged?.replace("-${statusWordLast}", "")?.replace("–${statusWordLast}", "")
        val stWordSpan = SpannableString(statusWordLast)
        if (statusWordLast.isNotEmpty()) {
            val length = statusWordLast.length
            stWordSpan.setSpan(StyleSpan(Typeface.NORMAL), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            stWordSpan.setSpan(ForegroundColorSpan(textColor), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            stWordSpan.setSpan(AbsoluteSizeSpan(14, true), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val playNameSpan = SpannableString("$playName ")
        playName?.length?.let {
            playNameSpan.setSpan(StyleSpan(Typeface.NORMAL), 0, it, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        gameTitleContentBuilder.append(playNameSpan).append(stWordSpan)
        return gameTitleContentBuilder
    }

    private fun getTitleNormal(oddsDetail: OddsDetailListData): SpannableStringBuilder {
        val gameTitleContentBuilder = SpannableStringBuilder()
        val title = oddsDetail.nameMap?.get(LanguageManager.getSelectLanguage().key)
        val playNameSpan = SpannableString("$title")
        title?.length?.let {
            playNameSpan.setSpan(StyleSpan(Typeface.NORMAL), 0, it, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        gameTitleContentBuilder.append(playNameSpan)
        return gameTitleContentBuilder
    }

    private fun getTotalCornerTitle(gameTitle: SpannableStringBuilder): SpannableStringBuilder {
        val spans = gameTitle.getSpans<Any>(0, gameTitle.length)
        val upperCaseSpannableString = SpannableString(gameTitle.toString().uppercase(Locale.getDefault()))
        spans.forEach {
            upperCaseSpannableString.setSpan(it, gameTitle.getSpanStart(it), gameTitle.getSpanEnd(it), 0)
        }

        val cornerTitleContentBuilder = SpannableStringBuilder()

        //若沒有角球資料或為null時不需顯示當前總角球列
        val showSubCornerTitle = oddsAdapter.homeCornerKicks != null && oddsAdapter.awayCornerKicks != null

        if (!showSubCornerTitle) {
            return cornerTitleContentBuilder.append(upperCaseSpannableString)
        }


        //region 當前總角球數 (角球副標題)
        val totalCorner = "${oddsAdapter.homeCornerKicks}-${oddsAdapter.awayCornerKicks}"
        val subCornerTitle = "${itemView.context.getString(R.string.current_corner)} $totalCorner"
        val subCornerTitleTextColor = ContextCompat.getColor(itemView.context, R.color.color_FF9143_cb7c2e)
        val subCornerTitleSpan = SpannableString(subCornerTitle)

        with(subCornerTitleSpan) {
            val endIndex = subCornerTitle.length
            //文字體
            setSpan(StyleSpan(Typeface.NORMAL), 0, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            //文字顏色
            setSpan(ForegroundColorSpan(subCornerTitleTextColor), 0, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            //文字大小
            setSpan(AbsoluteSizeSpan(12, true), 0, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            //行高
            //TODO 低版本問題: 低於VERSION_CODES.Q無法使用LineHeightSpan, setLineSpacing會調整到每一行的距離, 有些本身Title就是兩行的也會跟著被調整
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setSpan(LineHeightSpan.Standard(16.dp), 0, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                tvGameName?.setLineSpacing(4f, 1f)
            }
        }

        //endregion
        return cornerTitleContentBuilder
            .append(upperCaseSpannableString)
            .append("\n")
            .append(subCornerTitleSpan)
    }

    private val epsAdapter by lazy { TypeEPSAdapter() }

    private fun forEPS(oddsDetail: OddsDetailListData) {
        val vpEps = itemView.findViewById<ViewPager2>(R.id.vp_eps)
        vpEps?.apply {
            adapter = epsAdapter
            epsAdapter.setData(oddsDetail, oddsAdapter.onOddClickListener, oddsAdapter.oddsType)
            getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER //移除漣漪效果
            OverScrollDecoratorHelper.setUpOverScroll(getChildAt(0) as RecyclerView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
            setCurrentItem(oddsDetail.oddArrayList.indexOf(oddsAdapter.onOddClickListener.clickOdd), false)
        }

        itemView.findViewById<IndicatorView>(R.id.idv_eps).setupWithViewPager2(vpEps)
    }

    private fun oneList(oddsDetail: OddsDetailListData) {
        val moreClickListener = if (oddsDetail.oddArrayList.size > 5) {
            object : TypeOneListAdapter.OnMoreClickListener {
                override fun click() {
                    oddsDetail.isMoreExpand = !oddsDetail.isMoreExpand
                    oddsAdapter.notifyItemChanged(bindingAdapterPosition)
                }
            }
        } else {
            null
        }

        oddsDetail.needShowItem = oddsDetail.oddArrayList

        if (moreClickListener != null) {
            if (oddsDetail.isMoreExpand) {
                oddsDetail.needShowItem = oddsDetail.oddArrayList
            } else {
                oddsDetail.needShowItem = oddsDetail.oddArrayList.take(5).toMutableList()
            }
        }

        rvBet?.apply {
            adapter = TypeOneListAdapter(oddsDetail, oddsAdapter.onOddClickListener, oddsAdapter.oddsType, onMoreClickListener = moreClickListener)
            layoutManager = LinearLayoutManager(itemView.context)
        }
    }

    private fun forCS(oddsDetail: OddsDetailListData) {

        itemView.tv_draw?.isVisible = true
        itemView.findViewById<LinearLayout>(R.id.ll_content).isVisible = oddsDetail.isExpand

        val homeList: MutableList<Odd> = mutableListOf()
        val drawList: MutableList<Odd> = mutableListOf()
        val awayList: MutableList<Odd> = mutableListOf()
        val onOddClickListener = oddsAdapter.onOddClickListener
        val oddsType = oddsAdapter.oddsType

        for (odd in oddsDetail.oddArrayList) {
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
                val list: MutableList<Odd?> = mutableListOf()
                list.add(odd)
                val od = OddsDetailListData(
                    oddsDetail.gameType,
                    oddsDetail.typeCodes,
                    oddsDetail.name,
                    list,
                    oddsDetail.nameMap,
                    oddsDetail.rowSort
                )
                od.needShowItem = list

                rvBet?.apply {
                    adapter = TypeOneListAdapter(
                        od, onOddClickListener, oddsType
                    )
                    layoutManager = LinearLayoutManager(itemView.context)
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

        rvHome?.apply {
            adapter = TypeCSAdapter(oddsDetail, homeList, onOddClickListener, oddsType)
            layoutManager = LinearLayoutManager(itemView.context)
        }

        rvDraw?.apply {
            adapter = TypeCSAdapter(oddsDetail, drawList, onOddClickListener, oddsType)
            layoutManager = LinearLayoutManager(itemView.context)
        }

        rvAway?.apply {
            adapter = TypeCSAdapter(oddsDetail, awayList, onOddClickListener, oddsType)
            layoutManager = LinearLayoutManager(itemView.context)
        }

        if (drawList.size == 0) {
            rvDraw?.visibility = View.GONE
            itemView.findViewById<TextView>(R.id.tv_draw).visibility = View.GONE
        }
    }

    private fun forLCS(oddsDetail: OddsDetailListData) {
        itemView.tv_draw.isVisible = true
        itemView.findViewById<LinearLayout>(R.id.ll_content).isVisible = oddsDetail.isExpand

        val homeList: MutableList<Odd> = mutableListOf()
        val drawList: MutableList<Odd> = mutableListOf()
        val awayList: MutableList<Odd> = mutableListOf()
        val onOddClickListener = oddsAdapter.onOddClickListener
        val oddsType = oddsAdapter.oddsType

        for (odd in oddsDetail.oddArrayList) {
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
                val list: MutableList<Odd?> = mutableListOf()
                list.add(odd)
                val od = OddsDetailListData(
                    oddsDetail.gameType,
                    oddsDetail.typeCodes,
                    oddsDetail.name,
                    list,
                    oddsDetail.nameMap,
                    oddsDetail.rowSort
                )

                od.needShowItem = list
                rvBet?.apply {
                    adapter = TypeOneListAdapter(od, onOddClickListener, oddsType, isOddPercentage = true)
                    layoutManager = LinearLayoutManager(itemView.context)
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

        rvHome?.apply {
            adapter = TypeCSAdapter(oddsDetail, homeList, onOddClickListener, oddsType, isOddPercentage = true)
            layoutManager = LinearLayoutManager(itemView.context)
        }

        rvDraw?.apply {
            adapter = TypeCSAdapter(oddsDetail, drawList, onOddClickListener, oddsType, isOddPercentage = true)
            layoutManager = LinearLayoutManager(itemView.context)
        }

        rvAway?.apply {
            adapter = TypeCSAdapter(oddsDetail, awayList, onOddClickListener, oddsType, isOddPercentage = true)
            layoutManager = LinearLayoutManager(itemView.context)
        }

        if (drawList.size == 0) {
            rvDraw?.visibility = View.GONE
            itemView.findViewById<TextView>(R.id.tv_draw).visibility = View.GONE
        }
    }

    private fun forSingle(oddsDetail: OddsDetailListData, spanCount: Int, payloads: MutableList<Any>?) {

        if (rvBet == null) {
            if (oddsDetail.gameType.isEndScoreType()) {
                //如果赔率odd里面有队名，赔率按钮就不显示队名，否则就要在头部显示队名
                itemView.lin_match.isVisible = false
            }
            return
        }


        if (oddsDetail.gameType.isEndScoreType()) {
            //如果赔率odd里面有队名，赔率按钮就不显示队名，否则就要在头部显示队名
            itemView.lin_match.isVisible = false
            rvBet.setBackgroundResource(R.color.color_F9FAFD)
            val nonAdapter = rvBet.adapter == null
            if (rvBet.itemDecorationCount == 0) {
                rvBet.addItemDecoration(GridSpacingItemDecoration(4, 4.dp,false))
            }

            val adapter = initSingleRCV(rvBet, 4, oddsDetail)
            rvBet.tag = oddsDetail.gameType
            if (nonAdapter) {
                oddsAdapter.isFirstRefresh = false
            } else {
                rvBet.tag = oddsDetail.gameType
                if (payloads.isNullOrEmpty()){
                    adapter.notifyDataSetChanged()
                } else {
                    payloads.forEach { payloadItem ->
                        oddsDetail.oddArrayList.forEachIndexed { index, odd ->
                            if (odd?.id == payloadItem) {
                                runWithCatch { adapter.notifyItemChanged(index) }
                            }
                        }
                    }
                }
            }

            return
        }


//      Timber.d("===洗刷刷4 else index:${12} payloads:${payloads?.size}")
        rvBet.setBackgroundResource(R.color.color_FFFFFF)
        if (rvBet.adapter == initSingleRCV(rvBet, spanCount, oddsDetail)) {
            rvBet.adapter!!.notifyDataSetChanged()
        }
        //如果赔率odd里面有队名，赔率按钮就不显示队名，否则就要在头部显示队名
        if (spanCount == 3) {
            //如果第三个标题不等于客队名，那么判断第三个为和局，迁移到第二个位置
            if (oddsDetail.oddArrayList.size > 2 && !TextUtils.equals(oddsDetail.matchInfo?.awayName, oddsDetail.oddArrayList[2]?.name)) {
                oddsDetail.oddArrayList.add(1, oddsDetail.oddArrayList.removeAt(2))
            }
            TextUtils.equals(oddsDetail.matchInfo?.homeName, oddsDetail.oddArrayList[0]?.name)
            itemView.tv_draw?.isVisible = true
            itemView.tv_draw?.text = oddsDetail.oddArrayList.getOrNull(1)?.name
        }

    }

    private fun initSingleRCV(recyclerView: RecyclerView, spanCount: Int, oddsDetail: OddsDetailListData): TypeSingleAdapter {
        lateinit var adapter: TypeSingleAdapter
        oddsDetail.sortOddForSingle()
//        if (oddsDetail.gameType== PlayCate.CORNER_HDP.value){
//            LogUtil.toJson(oddsDetail.oddArrayList?.map { it?.name+","+it?.spread+","+it?.odds+","+it?.marketSort +","+it?.rowSort })
//        }
        if (recyclerView.adapter == null) {
            adapter = TypeSingleAdapter(oddsDetail, oddsAdapter.onOddClickListener, oddsAdapter.oddsType)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = GridLayoutManager(itemView.context, spanCount)
        } else {
            adapter = recyclerView.adapter as TypeSingleAdapter
            adapter.oddsDetail = oddsDetail
            adapter.onOddClickListener = oddsAdapter.onOddClickListener
            adapter.oddsType = oddsAdapter.oddsType
            (recyclerView.layoutManager as GridLayoutManager).spanCount = spanCount
        }
        return adapter
    }

    /**
     * 当出现rowSort呈现11112222的时候，需要整理成 12121212的布局
     */
    fun OddsDetailListData.sortOddForSingle(){
        val newList = oddArrayList.filterNotNull().groupBy { it?.rowSort }.values.toList()
        if (newList.size==2&&newList.first().size>2){
            var result = mutableListOf<Odd?>()
            for (index in 0 until (newList.maxOf { it.size })){
                for (element in newList){
                    element.getOrNull(index)?.let { result.add(it) }
                }
            }
            oddArrayList = result
       }
    }

    private fun forSingleCS(oddsDetail: OddsDetailListData) {
        val spanCount = 2
        itemView.tv_draw?.isVisible = oddsDetail.matchInfo?.gameType == GameType.FT.key
        tvHomeName?.isVisible = oddsDetail.isExpand
        tvAwayName?.isVisible = oddsDetail.isExpand

        val homeList: MutableList<Odd> = mutableListOf()
        val awayList: MutableList<Odd> = mutableListOf()
        oddsDetail.oddArrayList.forEach {
            it?.let { odd ->
                val stringArray = odd.name?.replace("\\s".toRegex(), "")?.split("-") ?: return@let
                if ((stringArray.getOrNull(0).toIntS()) > (stringArray.getOrNull(1).toIntS())) {
                    homeList.add(odd)
                } else {
                    awayList.add(odd)
                }
            }
        }

        val formattedOddArray = mutableListOf<Odd?>()

        homeList.sortBy { it.rowSort }
        awayList.sortBy { it.rowSort }

        for (i in 0 until homeList.size.coerceAtLeast(awayList.size)) {
            homeList.getOrNull(i)?.let { formattedOddArray.add(it) }
            awayList.getOrNull(i)?.let { formattedOddArray.add(it) }
        }

        oddsDetail.oddArrayList = formattedOddArray
        rvBet?.apply {
            val onOddClickListener = oddsAdapter.onOddClickListener
            val oddsType = oddsAdapter.oddsType
            val singleAdapter = TypeSingleAdapter(oddsDetail, onOddClickListener, oddsType)
            adapter = singleAdapter
            layoutManager = GridLayoutManager(itemView.context, spanCount)
        }
    }

    private fun forFGLG(oddsDetail: OddsDetailListData) {
        itemView.findViewById<ConstraintLayout>(R.id.cl_tab).isVisible = oddsDetail.isExpand
        rvBet?.apply {
            val onOddClickListener = oddsAdapter.onOddClickListener
            val oddsType = oddsAdapter.oddsType
            adapter = TypeOneListAdapter(selectFGLG(oddsDetail), onOddClickListener, oddsType)
            layoutManager = LinearLayoutManager(itemView.context)
        }

        tvFg?.apply {
            isSelected = oddsDetail.gameTypeFgLgSelect == FGLGType.FG
            setOnClickListener {
                oddsDetail.gameTypeFgLgSelect = FGLGType.FG
                (rvBet?.adapter as TypeOneListAdapter).mOddsDetail = selectFGLG(oddsDetail)
            }
        }

        tvLg?.apply {
            isSelected = oddsDetail.gameTypeFgLgSelect == FGLGType.LG
            setOnClickListener {
                oddsDetail.gameTypeFgLgSelect = FGLGType.LG
                (rvBet?.adapter as TypeOneListAdapter).mOddsDetail = selectFGLG(oddsDetail)
            }
        }
    }

    private fun selectFGLG(oddsDetail: OddsDetailListData): OddsDetailListData {
        val oddArrayList: MutableList<Odd?> = mutableListOf()

        //回傳順序固定為首个进球主队,首个进球客队,无进球,最后进球主队,最后进球客队
        if (oddsDetail.gameTypeFgLgSelect == FGLGType.FG) {
            tvFg?.isSelected = true
            tvLg?.isSelected = false
            oddArrayList.add(oddsDetail.oddArrayList[0])
            oddArrayList.add(oddsDetail.oddArrayList[1])
            oddArrayList.add(oddsDetail.oddArrayList[2])
        } else {
            tvFg?.isSelected = false
            tvLg?.isSelected = true
            oddArrayList.add(oddsDetail.oddArrayList[3])
            oddArrayList.add(oddsDetail.oddArrayList[4])
            oddArrayList.add(oddsDetail.oddArrayList[2])
        }

        return OddsDetailListData(
            oddsDetail.gameType,
            oddsDetail.typeCodes,
            oddsDetail.name,
            oddArrayList,
            oddsDetail.nameMap,
            oddsDetail.rowSort
        ).apply {
            isExpand = oddsDetail.isExpand
            isMoreExpand = oddsDetail.isMoreExpand
            gameTypeFgLgSelect = oddsDetail.gameTypeFgLgSelect
            needShowItem = oddArrayList
        }
    }

    private fun forSCO(oddsDetail: OddsDetailListData, position: Int) {

        val teamNameList = oddsDetail.teamNameList
        val homeName = teamNameList.firstOrNull() ?: ""
        val awayName = teamNameList.getOrNull(1) ?: ""

        itemView.findViewById<ConstraintLayout>(R.id.cl_tab).isVisible = oddsDetail.isExpand

        rvBet?.apply {
            layoutManager = LinearLayoutManager(itemView.context)
            adapter = TypeSCOAdapter(
                selectSCO(oddsDetail, oddsDetail.gameTypeSCOSelect ?: homeName, homeName),
                oddsAdapter.onOddClickListener,
                oddsAdapter.oddsType,
                object : TypeSCOAdapter.OnMoreClickListener {
                    override fun click() {
                        oddsDetail.isMoreExpand = !oddsDetail.isMoreExpand
                        oddsAdapter.notifyItemChanged(position)
                    }
            })
        }

        tvHomeName?.apply {
            text = homeName
            isSelected = oddsDetail.gameTypeSCOSelect == homeName
            setOnClickListener {
                oddsDetail.gameTypeSCOSelect = homeName
                oddsDetail.isMoreExpand = false
                val teamName = oddsDetail.gameTypeSCOSelect ?: homeName
                (rvBet?.adapter as TypeSCOAdapter).mOddsDetail = selectSCO(oddsDetail, teamName, homeName)
                oddsAdapter.notifyItemChanged(position)
            }
        }

        tvAwayName?.apply {
            text = awayName
            isSelected = oddsDetail.gameTypeSCOSelect == awayName
            setOnClickListener {
                oddsDetail.gameTypeSCOSelect = awayName
                oddsDetail.isMoreExpand = false
                val teamName = oddsDetail.gameTypeSCOSelect ?: awayName
                (rvBet?.adapter as TypeSCOAdapter).mOddsDetail = selectSCO(oddsDetail, teamName, homeName)
                oddsAdapter.notifyItemChanged(position)
            }
        }
    }

    private fun selectSCO(oddsDetail: OddsDetailListData, teamName: String, homeName: String): OddsDetailListData {
        tvHomeName?.isSelected = teamName == homeName
        tvAwayName?.isSelected = teamName != homeName
        oddsDetail.gameTypeSCOSelect = teamName
        oddsDetail.scoItem = if (tvHomeName?.isSelected == true) oddsDetail.homeMap else oddsDetail.awayMap
        return oddsDetail
    }

    private fun group6Item(oddsDetail: OddsDetailListData) {
        val rvBet = rvBet ?: return
        val adapter = group6AdapterSetup(oddsDetail)
        rvBet.adapter = adapter
        rvBet.layoutManager = LinearLayoutManager(itemView.context)
    }

    private fun group6ItemForDC(oddsDetail: OddsDetailListData) {
        val rvBet = rvBet ?: return
        val adapter = group6AdapterSetup(oddsDetail)
        adapter.leftName = rvBet.context.getString(R.string.odds_detail_play_type_dc_1X)
        adapter.centerName = rvBet.context.getString(R.string.odds_detail_play_type_dc_2X)
        adapter.rightName = rvBet.context.getString(R.string.odds_detail_play_type_dc_12)
        rvBet.adapter = adapter
        rvBet.layoutManager = LinearLayoutManager(itemView.context)
    }

    private fun group4ItemForOuBts(oddsDetail: OddsDetailListData) {
        val rvBet = rvBet ?: return
        val adapter = group4AdapterSetup(oddsDetail)
        adapter.leftName = rvBet.context.getString(R.string.odds_detail_play_type_bts_y)
        adapter.rightName = rvBet.context.getString(R.string.odds_detail_play_type_bts_n)
        rvBet.adapter = adapter
        rvBet.layoutManager = LinearLayoutManager(itemView.context)
    }

    private fun group4ItemForOuTag(oddsDetail: OddsDetailListData) {
        val rvBet = rvBet ?: return
        val adapter = group4AdapterSetup(oddsDetail)
        adapter.leftName = rvBet.context.getString(R.string.odd_button_ou_o)
        adapter.rightName = rvBet.context.getString(R.string.odd_button_ou_u)
        adapter.isShowSpreadWithName = true
        rvBet.adapter = adapter
        rvBet.layoutManager = LinearLayoutManager(itemView.context)
    }

    private fun group6AdapterSetup(oddsDetail: OddsDetailListData): Type6GroupAdapter {
        return Type6GroupAdapter(oddsDetail, oddsAdapter.onOddClickListener, oddsAdapter.oddsType)
    }

    private fun group4AdapterSetup(oddsDetail: OddsDetailListData): Type4GroupAdapter {
        return Type4GroupAdapter(oddsDetail, oddsAdapter.onOddClickListener, oddsAdapter.oddsType)
    }

}