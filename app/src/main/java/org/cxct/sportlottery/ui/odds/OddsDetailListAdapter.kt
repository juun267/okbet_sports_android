package org.cxct.sportlottery.ui.odds


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import kotlin.collections.ArrayList


/**
 * @author Kevin
 * @create 2020/12/23
 * @description 表格型排版與後端回傳順序有關
 */
class OddsDetailListAdapter(private val onOddClickListener: OnOddClickListener) :
    RecyclerView.Adapter<OddsDetailListAdapter.ViewHolder>() {


    var betInfoList: MutableList<BetInfoListData> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    var oddsDetailDataList: ArrayList<OddsDetailListData> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    var homeName: String? = null


    var awayName: String? = null


    var sportCode: String? = null


    private lateinit var code: String


    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    enum class LayoutType(val layout: Int) {
        TWO_SPAN_COUNT(R.layout.content_odds_detail_list_2_span_count),
        CS(R.layout.content_odds_detail_list_cs),
        ONE_LIST(R.layout.content_odds_detail_list_one),
        SINGLE(R.layout.content_odds_detail_list_single),
        SINGLE_2_ITEM(R.layout.content_odds_detail_list_single_2_item),
        FG_LG(R.layout.content_odds_detail_list_fg_lg),
        GROUP_6(R.layout.content_odds_detail_list_group_6_item),
        GROUP_4(R.layout.content_odds_detail_list_group_4_item)
    }


    enum class GameType(val value: String, val type: Int) {

        UNCHECK("UNCHECK", -1),//未確定

        HDP("HDP", 0),//让球
        OU("O/U", 1),//大小
        OU_1ST("O/U-1ST", 2),//大/小-上半场
        OU_2ST("O/U-2ST", 3),//大/小-下半场
        CS("CS", 4),//波胆
        FGLG("FG/LG", 5),//最先进球/最后进球
        DC("DC", 8),//双重机会
        OE("O/E", 9),//单/双
        SCO("SCO", 10),//进球球员
        TG("TG", 11),//总进球数
        TG_("TG-", 12),//进球数-半場
        BTS("BTS", 13),//双方球队进球
        GT1ST("GT1ST", 14),//首个入球时间
        SBH("SBH", 15),//双半场进球
        WBH("WBH", 16),//赢得所有半场
        WEH("WEH", 17),//赢得任一半场
        WM("WM", 18),//净胜球数
        CLSH("CLSH", 19),//零失球
        HTFT("HT/FT", 20),//半场/全场
        W3("W3", 21),//三项让球
        TG_OU("TG&O/U", 22),//球队进球数&大/小
        C_OU("CORNER-O/U", 23),//角球大/小
        C_OE("CORNER-OE", 24),//角球单/双
        OU_I_OT("O/U-INCL-OT", 25),//大/小(含加时)
        OU_SEG("O/U-SEG", 26),//总得分大/小-第X节
        SINGLE_OU("1X2-O/U", 29),//独赢大/小

        SINGLE_FLG("1X2-FLG", 32),//独赢-最先進球
        OU_BTS("O/U-BTS", 33),//大小&双方球队进球
        SINGLE_BTS("1X2-BTS", 34),//独赢&双方球队进球
        DC_OU("DC-O/U", 35),//双重机会&大小

        //single
        SINGLE("1X2", 7),//独赢
        SINGLE_1ST("1X2-1ST", 27),//独赢-上半場
        SINGLE_2ST("1X2-2ST", 28),//独赢-下半場
        SINGLE_OT("1X2-INCL-OT", 30),//独赢(含加时)
        SINGLE_SEG("1X2-SEG", 31),//独赢-第X节

        //single two item
        SINGLE_2("1X2", 36),
        SINGLE_1ST_2("1X2-1ST", 37),
        SINGLE_2ST_2("1X2-2ST", 38),
        SINGLE_OT_2("1X2-INCL-OT", 39),
        SINGLE_SEG_2("1X2-SEG", 40),

        HWMG_SINGLE("HWMG&1X2", 41),
        HDP_ONE_LIST("HDP", 42),
        NGOAL_1("NGOAL:1", 43),//第1个进球
        TWTN("TWTN", 44),//零失球獲勝
        DC_FLG("DC-FLG", 45),//双重机会&首次进球队伍
        DC_BTS("DC-BTS", 46),//双重机会&双方球队进球
        OU_OE("O/U-O/E", 47),//进球 大/小&进球数 单/双
        OU_TTS1ST("O/U-TTS1ST", 48)//进球 大/小&首次进球队伍
    }


    override fun getItemViewType(position: Int): Int {

        val type = oddsDetailDataList[position].gameType

        /*比對時先判斷完整字串 再比對部分字串(由長至短)*/
        when {
            TextUtil.compareWithGameKey(type, GameType.HDP.value) -> return GameType.HDP.type

            type == GameType.OU.value -> return GameType.OU.type

            type == GameType.OU_1ST.value -> return GameType.OU_1ST.type

            type == GameType.OU_2ST.value -> return GameType.OU_2ST.type

            type == GameType.OU_TTS1ST.value -> return GameType.OU_TTS1ST.type

            TextUtil.compareWithGameKey(type, GameType.CS.value) -> return GameType.CS.type

            type == GameType.FGLG.value -> return GameType.FGLG.type

            /**/
            type == GameType.DC_BTS.value -> return GameType.DC_BTS.type
            type == GameType.DC_FLG.value -> return GameType.DC_FLG.type
            TextUtil.compareWithGameKey(type, GameType.DC_OU.value) -> return GameType.DC_OU.type
            TextUtil.compareWithGameKey(type, GameType.DC.value) -> return GameType.DC.type

            /**/
            type == GameType.OU_OE.value -> return GameType.OU_OE.type
            TextUtil.compareWithGameKey(type, GameType.C_OE.value) -> return GameType.C_OE.type
            TextUtil.compareWithGameKey(type, GameType.OE.value) -> return GameType.OE.type

            TextUtil.compareWithGameKey(type, GameType.SCO.value) -> return GameType.SCO.type

            type == GameType.TG.value -> return GameType.TG.type

            TextUtil.compareWithGameKey(type, GameType.TG_.value) -> return GameType.TG_.type

            /**/
            type == GameType.SINGLE_BTS.value -> return GameType.SINGLE_BTS.type
            type == GameType.OU_BTS.value -> return GameType.OU_BTS.type
            TextUtil.compareWithGameKey(type, GameType.BTS.value) -> return GameType.BTS.type

            type == GameType.GT1ST.value -> return GameType.GT1ST.type

            type == GameType.SBH.value -> return GameType.SBH.type

            type == GameType.WBH.value -> return GameType.WBH.type

            type == GameType.WEH.value -> return GameType.WEH.type

            /**/
            type == GameType.HWMG_SINGLE.value -> return GameType.HWMG_SINGLE.type
            TextUtil.compareWithGameKey(type, GameType.WM.value) -> return GameType.WM.type

            type == GameType.CLSH.value -> return GameType.CLSH.type

            type == GameType.HTFT.value -> return GameType.HTFT.type

            type == GameType.W3.value -> return GameType.W3.type

            TextUtil.compareWithGameKey(type, GameType.TG_OU.value) -> return GameType.TG_OU.type

            TextUtil.compareWithGameKey(type, GameType.C_OU.value) -> return GameType.C_OU.type

            TextUtil.compareWithGameKey(type, GameType.OU_I_OT.value) -> return GameType.OU_I_OT.type

            TextUtil.compareWithGameKey(type, GameType.OU_SEG.value) -> return GameType.OU_SEG.type

            TextUtil.compareWithGameKey(type, GameType.SINGLE_OU.value) -> return GameType.SINGLE_OU.type

            TextUtil.compareWithGameKey(type, GameType.SINGLE_FLG.value) -> return GameType.SINGLE_FLG.type


            type == GameType.SINGLE.value -> return if (sportCode == SportType.FOOTBALL.code) {
                GameType.SINGLE.type
            } else {
                GameType.SINGLE_2.type
            }

            TextUtil.compareWithGameKey(type, GameType.SINGLE_1ST.value) -> return if (sportCode == SportType.FOOTBALL.code) {
                GameType.SINGLE_1ST.type
            } else {
                GameType.SINGLE_1ST_2.type
            }

            TextUtil.compareWithGameKey(type, GameType.SINGLE_2ST.value) -> return if (sportCode == SportType.FOOTBALL.code) {
                GameType.SINGLE_2ST.type
            } else {
                GameType.SINGLE_2ST_2.type
            }

            type == GameType.SINGLE_OT.value -> return if (sportCode == SportType.FOOTBALL.code) {
                GameType.SINGLE_OT.type
            } else {
                GameType.SINGLE_OT_2.type
            }

            TextUtil.compareWithGameKey(type, GameType.SINGLE_SEG.value) -> return if (sportCode == SportType.FOOTBALL.code) {
                GameType.SINGLE_SEG.type
            } else {
                GameType.SINGLE_SEG_2.type
            }

            type == GameType.NGOAL_1.value -> return GameType.NGOAL_1.type

            type == GameType.TWTN.value -> return GameType.TWTN.type

            else -> {
                return GameType.UNCHECK.type
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layout: Int = when (viewType) {

            GameType.OU_TTS1ST.type -> LayoutType.GROUP_4.layout
            GameType.OU_OE.type -> LayoutType.GROUP_4.layout
            GameType.OU_BTS.type -> LayoutType.GROUP_4.layout

            GameType.DC_OU.type -> LayoutType.GROUP_6.layout
            GameType.DC_BTS.type -> LayoutType.GROUP_6.layout
            GameType.DC_FLG.type -> LayoutType.GROUP_6.layout
            GameType.SINGLE_FLG.type -> LayoutType.GROUP_6.layout
            GameType.SINGLE_BTS.type -> LayoutType.GROUP_6.layout
            GameType.SINGLE_OU.type -> LayoutType.GROUP_6.layout

            GameType.CS.type -> LayoutType.CS.layout

            GameType.FGLG.type -> LayoutType.FG_LG.layout

            GameType.DC.type -> LayoutType.ONE_LIST.layout
            GameType.SCO.type -> LayoutType.ONE_LIST.layout
            GameType.GT1ST.type -> LayoutType.ONE_LIST.layout
            GameType.SBH.type -> LayoutType.ONE_LIST.layout
            GameType.WBH.type -> LayoutType.ONE_LIST.layout
            GameType.WEH.type -> LayoutType.ONE_LIST.layout
            GameType.WM.type -> LayoutType.ONE_LIST.layout
            GameType.HTFT.type -> LayoutType.ONE_LIST.layout
            GameType.W3.type -> LayoutType.ONE_LIST.layout
            GameType.HDP_ONE_LIST.type -> LayoutType.ONE_LIST.layout
            GameType.NGOAL_1.type -> LayoutType.ONE_LIST.layout
            GameType.TWTN.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.HWMG_SINGLE.type -> LayoutType.ONE_LIST.layout

            GameType.HDP.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.OU.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.OU_1ST.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.OU_2ST.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.OE.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.TG.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.TG_.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.BTS.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.CLSH.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.TG_OU.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.C_OU.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.C_OE.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.OU_I_OT.type -> LayoutType.TWO_SPAN_COUNT.layout
            GameType.OU_SEG.type -> LayoutType.TWO_SPAN_COUNT.layout

            GameType.SINGLE.type -> LayoutType.SINGLE.layout
            GameType.SINGLE_1ST.type -> LayoutType.SINGLE.layout
            GameType.SINGLE_2ST.type -> LayoutType.SINGLE.layout
            GameType.SINGLE_OT.type -> LayoutType.SINGLE.layout
            GameType.SINGLE_SEG.type -> LayoutType.SINGLE.layout

            GameType.SINGLE_2.type -> LayoutType.SINGLE_2_ITEM.layout
            GameType.SINGLE_1ST_2.type -> LayoutType.SINGLE_2_ITEM.layout
            GameType.SINGLE_2ST_2.type -> LayoutType.SINGLE_2_ITEM.layout
            GameType.SINGLE_OT_2.type -> LayoutType.SINGLE_2_ITEM.layout
            GameType.SINGLE_SEG_2.type -> LayoutType.SINGLE_2_ITEM.layout

            else -> LayoutType.ONE_LIST.layout

        }

        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false), viewType).apply {

            when (layout) {

                LayoutType.GROUP_4.layout,
                LayoutType.GROUP_6.layout -> {
                    rvBet?.apply {
                        addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.divider_color_white4_2dp)))
                    }
                }

                LayoutType.TWO_SPAN_COUNT.layout -> {
                    rvBet?.apply {
                        addItemDecoration(
                            GridItemDecoration(
                                context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_odds),
                                context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_odds),
                                ContextCompat.getColor(context, R.color.colorWhite),
                                false
                            )
                        )
                    }
                }

                LayoutType.CS.layout -> {
                    rvHome?.apply {
                        addItemDecoration(
                            CustomForOddDetailVerticalDivider(
                                context,
                                R.dimen.recyclerview_item_dec_spec_odds_detail_odds
                            )
                        )
                    }
                    rvDraw?.apply {
                        addItemDecoration(
                            CustomForOddDetailVerticalDivider(
                                context,
                                R.dimen.recyclerview_item_dec_spec_odds_detail_odds
                            )
                        )
                    }
                    rvAway?.apply {
                        addItemDecoration(
                            CustomForOddDetailVerticalDivider(
                                context,
                                R.dimen.recyclerview_item_dec_spec_odds_detail_odds
                            )
                        )
                    }
                }

                LayoutType.ONE_LIST.layout,
                LayoutType.FG_LG.layout -> {
                    rvBet?.apply {
                        addItemDecoration(
                            CustomForOddDetailVerticalDivider(
                                context,
                                R.dimen.recyclerview_item_dec_spec_odds_detail_odds
                            )
                        )
                    }
                }

                LayoutType.SINGLE.layout,
                LayoutType.SINGLE_2_ITEM.layout -> {
                    rvBet?.apply {
                        addItemDecoration(
                            GridItemDecoration(
                                context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_odds),
                                context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_odds),
                                ContextCompat.getColor(context, R.color.colorWhite),
                                false
                            )
                        )
                    }
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return oddsDetailDataList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel(oddsDetailDataList[position], position)
    }


    fun notifyDataSetChangedByCode(code: String) {
        this.code = code
        notifyDataSetChanged()
    }


    inner class ViewHolder(itemView: View, var viewType: Int) : RecyclerView.ViewHolder(itemView) {

        private fun setVisibility(visible: Boolean) {
            val param = itemView.layoutParams as RecyclerView.LayoutParams
            if (visible) {
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT
                param.width = LinearLayout.LayoutParams.MATCH_PARENT
                itemView.visibility = View.VISIBLE
            } else {
                param.height = 0
                param.width = 0
                itemView.visibility = View.GONE
            }
            itemView.layoutParams = param
        }

        private fun controlExpandBottom(expand: Boolean) {
            val param = itemView.layoutParams as RecyclerView.LayoutParams
            param.bottomMargin = if (expand) 0.dp else 8.dp
            itemView.layoutParams = param
        }

        private val tvGameName = itemView.findViewById<TextView>(R.id.tv_game_name)
        private val clItem = itemView.findViewById<ConstraintLayout>(R.id.cl_item)

        val rvBet: RecyclerView? = itemView.findViewById(R.id.rv_bet)

        //cs
        val rvHome: RecyclerView? = itemView.findViewById(R.id.rv_home)
        val rvDraw: RecyclerView? = itemView.findViewById(R.id.rv_draw)
        val rvAway: RecyclerView? = itemView.findViewById(R.id.rv_away)

        //FGLG
        val tvFg: TextView? = itemView.findViewById(R.id.tv_fg)
        val tvLg: TextView? = itemView.findViewById(R.id.tv_lg)

        fun bindModel(oddsDetail: OddsDetailListData, position: Int) {

            val type = oddsDetailDataList[position].gameType

            if (type.contains(":")) {
                tvGameName.text = oddsDetail.name.plus("  ").plus(type.split(":")[1])
            } else {
                tvGameName.text = oddsDetail.name
            }

            controlExpandBottom(oddsDetail.isExpand)

            clItem.setOnClickListener {
                oddsDetail.isExpand = !oddsDetail.isExpand
                notifyItemChanged(position)
            }

            rvBet?.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE


            when (viewType) {
                GameType.TWTN.type,
                GameType.CLSH.type,
                GameType.OU.type,
                GameType.OU_1ST.type,
                GameType.OU_2ST.type,
                GameType.OE.type,
                GameType.TG.type,
                GameType.TG_.type,
                GameType.TG_OU.type,
                GameType.C_OU.type,
                GameType.C_OE.type,
                GameType.OU_I_OT.type,
                GameType.OU_SEG.type,
                GameType.BTS.type,
                GameType.HDP.type -> for2SpanCount(oddsDetail)

                GameType.CS.type -> forCS(oddsDetail)

                GameType.SINGLE_OT.type,
                GameType.SINGLE_SEG.type,
                GameType.SINGLE_1ST.type,
                GameType.SINGLE_2ST.type,
                GameType.SINGLE.type -> forSingle(oddsDetail, 3)

                GameType.SINGLE_OT_2.type,
                GameType.SINGLE_SEG_2.type,
                GameType.SINGLE_1ST_2.type,
                GameType.SINGLE_2ST_2.type,
                GameType.SINGLE_2.type -> forSingle(oddsDetail, 2)

                GameType.FGLG.type -> forFGLG(oddsDetail)

                GameType.HWMG_SINGLE.type,
                GameType.WBH.type,
                GameType.WEH.type,
                GameType.SBH.type,
                GameType.NGOAL_1.type,
                GameType.HDP_ONE_LIST.type,
                GameType.SCO.type,
                GameType.W3.type,
                GameType.DC.type,
                GameType.GT1ST.type,
                GameType.WM.type,
                GameType.HTFT.type -> oneList(oddsDetail)

                GameType.DC_OU.type,
                GameType.DC_BTS.type,
                GameType.DC_FLG.type -> group6ItemForDC(oddsDetail)

                GameType.SINGLE_FLG.type,
                GameType.SINGLE_BTS.type,
                GameType.SINGLE_OU.type -> group6Item(oddsDetail)

                GameType.OU_BTS.type -> group4ItemForOuBts(oddsDetail)

                GameType.OU_TTS1ST.type,
                GameType.OU_OE.type -> group4ItemForOuTag(oddsDetail)

                //臨時新增或尚未確定的排版 以單行列表作為排版
                GameType.UNCHECK.type -> oneList(oddsDetail)

            }

            for (element in oddsDetail.typeCodes) {
                try {
                    if (element == code) {
                        setVisibility(true)
                        break
                    } else {
                        setVisibility(false)
                    }
                } catch (e: Exception) {
                    setVisibility(true)
                }
            }
        }

        private fun oneList(oddsDetail: OddsDetailListData) {
            rvBet?.apply {
                adapter = TypeOneListAdapter(
                    oddsDetail,
                    onOddClickListener,
                    betInfoList,
                    oddsType
                )
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }

        private fun forCS(oddsDetail: OddsDetailListData) {

            itemView.findViewById<TextView>(R.id.tv_home_name).text = homeName
            itemView.findViewById<TextView>(R.id.tv_away_name).text = awayName

            itemView.findViewById<LinearLayout>(R.id.ll_content).visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            val homeList: MutableList<Odd> = mutableListOf()
            val drawList: MutableList<Odd> = mutableListOf()
            val awayList: MutableList<Odd> = mutableListOf()

            for (odd in oddsDetail.oddArrayList) {
                if (odd?.name?.contains(" - ") == true) {
                    val stringArray: List<String> = odd.name?.split(" - ") ?: listOf()
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
                        oddsDetail.gameType, oddsDetail.typeCodes, oddsDetail.name, list
                    )

                    rvBet?.apply {
                        adapter = TypeOneListAdapter(
                            od,
                            onOddClickListener,
                            betInfoList,
                            oddsType
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
                adapter = TypeCSAdapter(oddsDetail, homeList, onOddClickListener, betInfoList, oddsType)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            rvDraw?.apply {
                adapter = TypeCSAdapter(oddsDetail, drawList, onOddClickListener, betInfoList, oddsType)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            rvAway?.apply {
                adapter = TypeCSAdapter(oddsDetail, awayList, onOddClickListener, betInfoList, oddsType)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            if (drawList.size == 0) {
                rvDraw?.visibility = View.GONE
                itemView.findViewById<TextView>(R.id.tv_draw).visibility = View.GONE
            }
        }

        private fun forSingle(oddsDetail: OddsDetailListData, spanCount: Int) {
            rvBet?.apply {
                adapter = TypeSingleAdapter(oddsDetail, onOddClickListener, betInfoList, oddsType)
                layoutManager = GridLayoutManager(itemView.context, spanCount)
            }
        }

        private fun for2SpanCount(oddsDetail: OddsDetailListData) {
            rvBet?.apply {
                adapter = TypeTwoSpanCountGridAdapter(oddsDetail, onOddClickListener, betInfoList, oddsType)
                layoutManager = GridLayoutManager(itemView.context, 2)
            }
        }

        private fun forFGLG(oddsDetail: OddsDetailListData) {
            itemView.findViewById<ConstraintLayout>(R.id.cl_tab).visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            itemView.findViewById<View>(R.id.divider).visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            rvBet?.apply {
                adapter = TypeOneListAdapter(
                    selectFGLG(oddsDetail),
                    onOddClickListener,
                    betInfoList,
                    oddsType
                )
                layoutManager = LinearLayoutManager(itemView.context)
            }

            tvFg?.apply {
                isSelected = oddsDetail.gameTypeFgLgSelect == FGLGType.FG
                setOnClickListener {
                    (rvBet?.adapter as TypeOneListAdapter).mOddsDetail = selectFGLG(oddsDetail.apply {
                        gameTypeFgLgSelect = FGLGType.FG
                    })
                }
            }

            tvLg?.apply {
                isSelected = oddsDetail.gameTypeFgLgSelect == FGLGType.LG
                setOnClickListener {
                    (rvBet?.adapter as TypeOneListAdapter).mOddsDetail = selectFGLG(oddsDetail.apply {
                        gameTypeFgLgSelect = FGLGType.LG
                    })
                }
            }
        }

        private fun selectFGLG(oddsDetail: OddsDetailListData): OddsDetailListData {
            val oddArrayList: MutableList<Odd?> = mutableListOf()

            //回傳順序固定為首个进球主队,首个进球客队,无进球,最后进球主队,最后进球客队
            when (oddsDetail.gameTypeFgLgSelect) {
                FGLGType.FG -> {
                    tvFg?.isSelected = true
                    tvLg?.isSelected = false
                    oddArrayList.apply {
                        add(oddsDetail.oddArrayList[0])
                        add(oddsDetail.oddArrayList[1])
                        add(oddsDetail.oddArrayList[2])
                    }
                }
                else -> {
                    tvFg?.isSelected = false
                    tvLg?.isSelected = true
                    oddArrayList.apply {
                        add(oddsDetail.oddArrayList[3])
                        add(oddsDetail.oddArrayList[4])
                        add(oddsDetail.oddArrayList[2])
                    }
                }
            }
            return OddsDetailListData(
                oddsDetail.gameType,
                oddsDetail.typeCodes,
                oddsDetail.name,
                oddArrayList
            ).apply {
                isExpand = oddsDetail.isExpand
                isMoreExpand = oddsDetail.isMoreExpand
                gameTypeFgLgSelect = oddsDetail.gameTypeFgLgSelect
            }
        }

        private fun group6Item(oddsDetail: OddsDetailListData) {
            rvBet?.apply {
                adapter = group6AdapterSetup(oddsDetail).apply {
                    leftName = homeName
                    centerName = itemView.context.getString(R.string.draw)
                    rightName = awayName
                }
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }

        private fun group6ItemForDC(oddsDetail: OddsDetailListData) {
            rvBet?.apply {
                adapter = group6AdapterSetup(oddsDetail).apply {
                    leftName = context.getString(R.string.odds_detail_play_type_dc_1X)
                    centerName = context.getString(R.string.odds_detail_play_type_dc_2X)
                    rightName = context.getString(R.string.odds_detail_play_type_dc_12)
                }
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }

        private fun group4ItemForOuBts(oddsDetail: OddsDetailListData) {
            rvBet?.apply {
                adapter = group4AdapterSetup(oddsDetail).apply {
                    leftName = context.getString(R.string.odds_detail_play_type_bts_y)
                    rightName = context.getString(R.string.odds_detail_play_type_bts_n)
                }
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }


        private fun group4ItemForOuTag(oddsDetail: OddsDetailListData) {
            rvBet?.apply {
                adapter = group4AdapterSetup(oddsDetail).apply {
                    leftName = context.getString(R.string.odds_detail_play_type_ou_o)
                    rightName = context.getString(R.string.odds_detail_play_type_ou_u)
                    isShowSpreadWithName = true
                }
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun group6AdapterSetup(oddsDetail: OddsDetailListData): Type6GroupAdapter =
            Type6GroupAdapter(
                oddsDetail.apply { groupItem = oddsDetail.oddArrayList.groupBy { it?.spread } as HashMap<String, List<Odd?>> },
                onOddClickListener,
                betInfoList,
                oddsType
            )

        @Suppress("UNCHECKED_CAST")
        private fun group4AdapterSetup(oddsDetail: OddsDetailListData): Type4GroupAdapter =
            Type4GroupAdapter(
                oddsDetail.apply {

                    //依key分組 有元件需要用key做顯示
                    val filterMap = oddsDetail.oddArrayList.groupBy { it?.spread }.filter {
                        it.key != null
                    } as HashMap<String, List<Odd?>>

                    val keys = mutableListOf<String>().apply { filterMap.forEach { add(it.key) } }

                    //依key數量等分
                    val splitList = splitSameLength(oddsDetail.oddArrayList, keys.size)

                    groupItem = HashMap<String, List<Odd?>>().apply {
                        for (i in splitList.indices) {
                            this[keys[i]] = splitList[i]
                        }
                    }

                },
                onOddClickListener,
                betInfoList,
                oddsType
            )

    }

}