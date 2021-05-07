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
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import kotlin.collections.ArrayList

const val DEFAULT_ITEM_VISIBLE_POSITION = 4

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
        HDP(R.layout.content_odds_detail_list_hdp),
        TWO_SIDES(R.layout.content_odds_detail_list_two_sides),
        CS(R.layout.content_odds_detail_list_cs),
        ONE_LIST(R.layout.content_odds_detail_list_one),
        SINGLE(R.layout.content_odds_detail_list_single),
        SINGLE_2_ITEM(R.layout.content_odds_detail_list_single_2_item)
    }


    enum class GameType(val value: String, val type: Int) {
        HDP("HDP", 0),//让球
        OU("O/U", 1),//大小
        OU_1ST("O/U-1ST", 2),//大/小-上半场
        OU_2ST("O/U-2ST", 3),//大/小-下半场
        CS("CS", 4),//波胆
        FG("FG", 5),//首先进球
        LG("LG", 6),//最后进球
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

        HDP_ONE_LIST("HDP", 42)

    }


    override fun getItemViewType(position: Int): Int {

        val type = oddsDetailDataList[position].gameType

        when {
            TextUtil.compareWithGameKey(type, GameType.HDP.value) -> {

                when (sportCode) {
                    SportType.FOOTBALL.code,
                    SportType.BASKETBALL.code -> return GameType.HDP.type
                    else -> return GameType.HDP_ONE_LIST.type
                }


            }

            type == GameType.OU.value -> return GameType.OU.type

            type == GameType.OU_1ST.value -> return GameType.OU_1ST.type

            type == GameType.OU_2ST.value -> return GameType.OU_2ST.type

            TextUtil.compareWithGameKey(type, GameType.CS.value) -> return GameType.CS.type

            type == GameType.FG.value -> return GameType.FG.type

            type == GameType.LG.value -> return GameType.LG.type

            //先判斷完整字串 再比對部分字串(由長至短)
            TextUtil.compareWithGameKey(type, GameType.DC_OU.value) -> return GameType.DC_OU.type
            TextUtil.compareWithGameKey(type, GameType.DC.value) -> return GameType.DC.type

            TextUtil.compareWithGameKey(type, GameType.OE.value) -> return GameType.OE.type

            TextUtil.compareWithGameKey(type, GameType.SCO.value) -> return GameType.SCO.type

            type == GameType.TG.value -> return GameType.TG.type

            TextUtil.compareWithGameKey(type, GameType.TG_.value) -> return GameType.TG_.type

            //先判斷完整字串 再比對部分字串(由長至短)
            type == GameType.SINGLE_BTS.value -> return GameType.SINGLE_BTS.type
            TextUtil.compareWithGameKey(type, GameType.OU_BTS.value) -> return GameType.OU_BTS.type
            TextUtil.compareWithGameKey(type, GameType.BTS.value) -> return GameType.BTS.type

            type == GameType.GT1ST.value -> return GameType.GT1ST.type

            type == GameType.SBH.value -> return GameType.SBH.type

            type == GameType.WBH.value -> return GameType.WBH.type

            type == GameType.WEH.value -> return GameType.WEH.type

            //先判斷完整字串 再比對部分字串(由長至短)
            type == GameType.HWMG_SINGLE.value -> return GameType.HWMG_SINGLE.type
            TextUtil.compareWithGameKey(type, GameType.WM.value) -> return GameType.WM.type

            type == GameType.CLSH.value -> return GameType.CLSH.type

            type == GameType.HTFT.value -> return GameType.HTFT.type

            type == GameType.W3.value -> return GameType.W3.type

            TextUtil.compareWithGameKey(type, GameType.TG_OU.value) -> return GameType.TG_OU.type

            TextUtil.compareWithGameKey(type, GameType.C_OU.value) -> return GameType.C_OU.type

            TextUtil.compareWithGameKey(type, GameType.C_OE.value) -> return GameType.C_OE.type

            TextUtil.compareWithGameKey(type, GameType.OU_I_OT.value) -> return GameType.OU_I_OT.type

            TextUtil.compareWithGameKey(type, GameType.OU_SEG.value) -> return GameType.OU_SEG.type

            TextUtil.compareWithGameKey(type, GameType.SINGLE_OU.value) -> return GameType.SINGLE_OU.type

            TextUtil.compareWithGameKey(type, GameType.SINGLE_FLG.value) -> return GameType.SINGLE_FLG.type


            type == GameType.SINGLE.value -> return if (oddsDetailDataList[position].oddArrayList.size == 2) {
                GameType.SINGLE_2.type
            } else {
                GameType.SINGLE.type
            }

            TextUtil.compareWithGameKey(type, GameType.SINGLE_1ST.value) -> return if (oddsDetailDataList[position].oddArrayList.size == 2) {
                GameType.SINGLE_1ST_2.type
            } else {
                GameType.SINGLE_1ST.type
            }

            TextUtil.compareWithGameKey(type, GameType.SINGLE_2ST.value) -> return if (oddsDetailDataList[position].oddArrayList.size == 2) {
                GameType.SINGLE_2ST_2.type
            } else {
                GameType.SINGLE_2ST.type
            }

            type == GameType.SINGLE_OT.value -> return if (oddsDetailDataList[position].oddArrayList.size == 2) {
                GameType.SINGLE_OT_2.type
            } else {
                GameType.SINGLE_OT.type
            }

            TextUtil.compareWithGameKey(type, GameType.SINGLE_SEG.value) -> return if (oddsDetailDataList[position].oddArrayList.size == 2) {
                GameType.SINGLE_SEG_2.type
            } else {
                GameType.SINGLE_SEG.type
            }

            else -> {
                return -1
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layout: Int = when (viewType) {
            GameType.HDP.type -> LayoutType.HDP.layout
            GameType.OU.type -> LayoutType.TWO_SIDES.layout
            GameType.OU_1ST.type -> LayoutType.TWO_SIDES.layout
            GameType.OU_2ST.type -> LayoutType.TWO_SIDES.layout
            GameType.CS.type -> LayoutType.CS.layout
            GameType.FG.type -> LayoutType.SINGLE.layout
            GameType.LG.type -> LayoutType.SINGLE.layout
            GameType.DC.type -> LayoutType.ONE_LIST.layout
            GameType.OE.type -> LayoutType.TWO_SIDES.layout
            GameType.SCO.type -> LayoutType.ONE_LIST.layout
            GameType.TG.type -> LayoutType.ONE_LIST.layout
            GameType.TG_.type -> LayoutType.ONE_LIST.layout
            GameType.BTS.type -> LayoutType.HDP.layout
            GameType.GT1ST.type -> LayoutType.ONE_LIST.layout
            GameType.SBH.type -> LayoutType.HDP.layout
            GameType.WBH.type -> LayoutType.HDP.layout
            GameType.WEH.type -> LayoutType.HDP.layout
            GameType.WM.type -> LayoutType.ONE_LIST.layout
            GameType.CLSH.type -> LayoutType.HDP.layout
            GameType.HTFT.type -> LayoutType.ONE_LIST.layout
            GameType.W3.type -> LayoutType.ONE_LIST.layout
            GameType.TG_OU.type -> LayoutType.TWO_SIDES.layout
            GameType.C_OU.type -> LayoutType.TWO_SIDES.layout
            GameType.C_OE.type -> LayoutType.TWO_SIDES.layout
            GameType.OU_I_OT.type -> LayoutType.TWO_SIDES.layout
            GameType.OU_SEG.type -> LayoutType.TWO_SIDES.layout
            GameType.SINGLE_OU.type -> LayoutType.ONE_LIST.layout
            GameType.SINGLE_FLG.type -> LayoutType.ONE_LIST.layout
            GameType.OU_BTS.type -> LayoutType.ONE_LIST.layout
            GameType.SINGLE_BTS.type -> LayoutType.ONE_LIST.layout
            GameType.DC_OU.type -> LayoutType.ONE_LIST.layout

            GameType.SINGLE.type -> LayoutType.SINGLE.layout
            GameType.SINGLE_1ST.type -> LayoutType.SINGLE.layout
            GameType.SINGLE_2ST.type -> LayoutType.SINGLE.layout
            GameType.SINGLE_OT.type -> LayoutType.SINGLE.layout
            GameType.SINGLE_SEG.type -> LayoutType.SINGLE.layout
            GameType.HWMG_SINGLE.type -> LayoutType.SINGLE.layout

            GameType.SINGLE_2.type -> LayoutType.SINGLE_2_ITEM.layout
            GameType.SINGLE_1ST_2.type -> LayoutType.SINGLE_2_ITEM.layout
            GameType.SINGLE_2ST_2.type -> LayoutType.SINGLE_2_ITEM.layout
            GameType.SINGLE_OT_2.type -> LayoutType.SINGLE_2_ITEM.layout
            GameType.SINGLE_SEG_2.type -> LayoutType.SINGLE_2_ITEM.layout

            GameType.HDP_ONE_LIST.type -> LayoutType.ONE_LIST.layout

            else -> LayoutType.ONE_LIST.layout

        }

        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false), viewType).apply {
            when (layout) {
                LayoutType.HDP.layout -> {
                    rvBet.apply {
                        addItemDecoration(
                            GridItemDecoration(
                                context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_grid_2_horizontal),
                                context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_grid_2_vertical),
                                ContextCompat.getColor(context, R.color.colorWhite),
                                false
                            )
                        )
                    }
                }

                LayoutType.TWO_SIDES.layout -> {
                    rvBet.apply {
                        addItemDecoration(
                            GridItemDecoration(
                                context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_two_side),
                                context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_two_side),
                                ContextCompat.getColor(context, R.color.colorWhite6),
                                false
                            )
                        )
                    }
                }

                LayoutType.CS.layout -> {
                    rvHome.apply {
                        addItemDecoration(
                            SpaceItemDecoration(
                                context,
                                R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_one_list
                            )
                        )
                    }
                    rvDraw.apply {
                        addItemDecoration(
                            SpaceItemDecoration(
                                context,
                                R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_one_list
                            )
                        )
                    }
                    rvAway.apply {
                        addItemDecoration(
                            SpaceItemDecoration(
                                context,
                                R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_one_list
                            )
                        )
                    }
                }

                LayoutType.ONE_LIST.layout -> {
                    rvBet.apply {
                        addItemDecoration(
                            DividerItemDecorator(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.divider_straight
                                )
                            )
                        )
                    }
                }

                LayoutType.SINGLE.layout -> {
                    rvBet.apply {
                        addItemDecoration(
                            GridItemDecoration(
                                context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_grid_3),
                                context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_grid_3),
                                ContextCompat.getColor(context, R.color.colorWhite),
                                false
                            )
                        )
                    }
                }

                LayoutType.SINGLE_2_ITEM.layout -> {
                    rvBet.apply {
                        addItemDecoration(
                            GridItemDecoration(
                                context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_grid_3),
                                context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_grid_3),
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


    inner class ViewHolder(itemView: View, var viewType: Int) : OddViewHolder(itemView) {

        private fun setVisibility(visible: Boolean) {
            val param = itemView.layoutParams as RecyclerView.LayoutParams
            if (visible) {
                param.bottomMargin = 5.dp
                param.height = LinearLayout.LayoutParams.WRAP_CONTENT
                param.width = LinearLayout.LayoutParams.MATCH_PARENT
                itemView.visibility = View.VISIBLE
            } else {
                param.bottomMargin = 0
                param.height = 0
                param.width = 0
                itemView.visibility = View.GONE
            }
            itemView.layoutParams = param
        }

        private val tvGameName = itemView.findViewById<TextView>(R.id.tv_game_name)
        private val llItem = itemView.findViewById<LinearLayout>(R.id.ll_item)
        private val ivArrowUp = itemView.findViewById<ImageView>(R.id.iv_arrow_up)
        private val vLine = itemView.findViewById<View>(R.id.v_line)

        val rvBet = itemView.findViewById<RecyclerView>(R.id.rv_bet)

        //cs
        val rvHome = itemView.findViewById<RecyclerView>(R.id.rv_home)
        val rvDraw = itemView.findViewById<RecyclerView>(R.id.rv_draw)
        val rvAway = itemView.findViewById<RecyclerView>(R.id.rv_away)


        fun bindModel(oddsDetail: OddsDetailListData, position: Int) {

            val type = oddsDetailDataList[position].gameType

            if (type.contains(":")) {
                tvGameName.text = oddsDetail.name.plus("  ").plus(type.split(":")[1])
            } else {
                tvGameName.text = oddsDetail.name
            }

            llItem.setOnClickListener {
                oddsDetail.isExpand = !oddsDetail.isExpand
                notifyItemChanged(position)
            }

            if (oddsDetail.isExpand) {
                ivArrowUp.animate().rotation(180f).setDuration(200).start()
                vLine.visibility = View.VISIBLE
            } else {
                ivArrowUp.animate().rotation(0f).setDuration(200).start()
                vLine.visibility = View.GONE
            }


            when (viewType) {
                GameType.BTS.type,
                GameType.HDP.type -> forHDP(oddsDetail)

                GameType.OU.type,
                GameType.OU_1ST.type,
                GameType.OU_2ST.type,
                GameType.OE.type,
                GameType.TG_OU.type,
                GameType.C_OU.type,
                GameType.C_OE.type,
                GameType.OU_I_OT.type,
                GameType.OU_SEG.type -> twoSides(oddsDetail)

                GameType.CS.type -> forCS(oddsDetail)

                GameType.FG.type,
                GameType.LG.type,
                GameType.HWMG_SINGLE.type,
                GameType.SINGLE_OT.type,
                GameType.SINGLE_SEG.type,
                GameType.SINGLE_1ST.type,
                GameType.SINGLE_2ST.type,
                GameType.SINGLE.type -> forSingle(oddsDetail)

                GameType.SINGLE_OT_2.type,
                GameType.SINGLE_SEG_2.type,
                GameType.SINGLE_1ST_2.type,
                GameType.SINGLE_2ST_2.type,
                GameType.SINGLE_2.type -> forSingle2Item(oddsDetail)

                GameType.SBH.type,
                GameType.WBH.type,
                GameType.WEH.type,
                GameType.CLSH.type -> forChangeHDP(oddsDetail)

                GameType.HDP_ONE_LIST.type,
                GameType.SCO.type,
                GameType.DC_OU.type,
                GameType.SINGLE_BTS.type,
                GameType.OU_BTS.type,
                GameType.SINGLE_FLG.type,
                GameType.W3.type,
                GameType.SINGLE_OU.type,
                GameType.DC.type,
                GameType.TG.type,
                GameType.TG_.type,
                GameType.GT1ST.type,
                GameType.WM.type,
                GameType.HTFT.type -> oneList(oddsDetail, sportCode)

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

        private fun oneList(oddsDetail: OddsDetailListData, sportCode: String?) {
            rvBet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            for (i in oddsDetail.oddArrayList.indices) {
                if (!oddsDetail.isMoreExpand && i > DEFAULT_ITEM_VISIBLE_POSITION) {
                    oddsDetail.oddArrayList[i].itemViewVisible = false
                }
            }

            rvBet.apply {
                adapter = TypeOneListAdapter(
                    sportCode ?: "",
                    oddsDetail,
                    onOddClickListener,
                    betInfoList,
                    object : TypeOneListAdapter.OnMoreClickListener {
                        override fun click() {
                            for (i in oddsDetail.oddArrayList.indices) {
                                if (i > DEFAULT_ITEM_VISIBLE_POSITION) {
                                    oddsDetail.oddArrayList[i].itemViewVisible = !oddsDetail.oddArrayList[i].itemViewVisible
                                }
                            }
                            oddsDetail.isMoreExpand = !oddsDetail.isMoreExpand
                            adapter?.notifyItemRangeChanged(OVER_COUNT, oddsDetail.oddArrayList.size - 1)
                        }
                    },
                    oddsType
                )
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }

        private fun forCS(oddsDetail: OddsDetailListData) {
            itemView.findViewById<LinearLayout>(R.id.ll_content).visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            val tvOther = itemView.findViewById<TextView>(R.id.tv_other)
            val rlOdds = itemView.findViewById<RelativeLayout>(R.id.rl_odds)

            val homeList: MutableList<Odd> = mutableListOf()
            val drawList = ArrayList<Odd>()
            val awayList: MutableList<Odd> = mutableListOf()

            for (odd in oddsDetail.oddArrayList) {

                if (odd.name != null) {
                    if (odd.name?.contains(" - ") == true) {
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
                        tvOther.text = itemView.context.getString(R.string.odds_detail_cs_other)
                        setData(oddsDetail, odd, onOddClickListener, betInfoList, BUTTON_SPREAD_TYPE_CENTER, oddsType)

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

            rvHome.apply {
                adapter = TypeCSAdapter(oddsDetail, homeList, onOddClickListener, betInfoList, oddsType)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            rvDraw.apply {
                adapter = TypeCSAdapter(oddsDetail, drawList, onOddClickListener, betInfoList, oddsType)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            rvAway.apply {
                adapter = TypeCSAdapter(oddsDetail, awayList, onOddClickListener, betInfoList, oddsType)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            if (drawList.size == 0) {
                rvDraw.visibility = View.GONE
                val param = rlOdds.layoutParams as ConstraintLayout.LayoutParams
                param.endToEnd = R.id.guideline_cs_other_2_list
                rlOdds.layoutParams = param
            }
        }

        private fun forSingle(oddsDetail: OddsDetailListData) {
            itemView.findViewById<TextView>(R.id.tv_home_name).text = oddsDetail.oddArrayList[0].name
            itemView.findViewById<TextView>(R.id.tv_draw).text = oddsDetail.oddArrayList[1].name
            itemView.findViewById<TextView>(R.id.tv_away_name).text = oddsDetail.oddArrayList[2].name
            itemView.findViewById<LinearLayout>(R.id.ll_game).visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            rvBet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            rvBet.apply {
                adapter = TypeSingleAdapter(oddsDetail, onOddClickListener, betInfoList, oddsType)
                layoutManager = GridLayoutManager(itemView.context, 3)
            }
        }

        private fun forSingle2Item(oddsDetail: OddsDetailListData) {
            itemView.findViewById<TextView>(R.id.tv_home_name).text = homeName
            itemView.findViewById<TextView>(R.id.tv_away_name).text = awayName
            itemView.findViewById<LinearLayout>(R.id.ll_game).visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            rvBet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            rvBet.apply {
                adapter = TypeSingleAdapter(oddsDetail, onOddClickListener, betInfoList, oddsType)
                layoutManager = GridLayoutManager(itemView.context, 2)
            }

        }

        private fun forChangeHDP(oddsDetail: OddsDetailListData) {
            itemView.findViewById<TextView>(R.id.tv_home_name).text = homeName
            itemView.findViewById<TextView>(R.id.tv_away_name).text = awayName
            itemView.findViewById<LinearLayout>(R.id.ll_game).visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            rvBet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            rvBet.apply {
                adapter = TypeOnlyOddHDPAdapter(oddsDetail, onOddClickListener, betInfoList, oddsType)
                layoutManager = GridLayoutManager(itemView.context, 2)
            }
        }

        private fun forHDP(oddsDetail: OddsDetailListData) {

            itemView.findViewById<TextView>(R.id.tv_home_name).text = homeName
            itemView.findViewById<TextView>(R.id.tv_away_name).text = awayName
            itemView.findViewById<LinearLayout>(R.id.ll_game).visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            rvBet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            rvBet.apply {
                adapter = TypeHDPAdapter(oddsDetail, onOddClickListener, betInfoList, oddsType)
                layoutManager = GridLayoutManager(itemView.context, 2)
            }
        }

        private fun twoSides(oddsDetail: OddsDetailListData) {
            rvBet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            rvBet.apply {
                adapter = TypeTwoSidesAdapter(oddsDetail, onOddClickListener, betInfoList, oddsType)
                layoutManager = GridLayoutManager(itemView.context, 2)
            }
        }

    }


}