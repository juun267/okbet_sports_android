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
import org.cxct.sportlottery.enum.OddSpreadForSCO
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
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
        GROUP_4(R.layout.content_odds_detail_list_group_4_item),
        SCO(R.layout.content_odds_detail_list_sco)
    }

    override fun getItemViewType(position: Int): Int {

        val type = oddsDetailDataList[position].gameType

        /*比對時先判斷完整字串 再比對部分字串(由長至短)*/
        when {
            TextUtil.compareWithGameKey(type, PlayCate.HDP.value) -> return PlayCate.HDP.ordinal

            type == PlayCate.OU.value -> return PlayCate.OU.ordinal

            type == PlayCate.OU_1ST.value -> return PlayCate.OU_1ST.ordinal

            type == PlayCate.OU_2ST.value -> return PlayCate.OU_2ST.ordinal

            type == PlayCate.OU_TTS1ST.value -> return PlayCate.OU_TTS1ST.ordinal

            TextUtil.compareWithGameKey(type, PlayCate.CS.value) -> return PlayCate.CS.ordinal

            type == PlayCate.FGLG.value -> return PlayCate.FGLG.ordinal

            /**/
            type == PlayCate.DC_BTS.value -> return PlayCate.DC_BTS.ordinal
            type == PlayCate.DC_FLG.value -> return PlayCate.DC_FLG.ordinal
            TextUtil.compareWithGameKey(type, PlayCate.DC_OU.value) -> return PlayCate.DC_OU.ordinal
            TextUtil.compareWithGameKey(type, PlayCate.DC.value) -> return PlayCate.DC.ordinal

            /**/
            type == PlayCate.OU_OE.value -> return PlayCate.OU_OE.ordinal
            TextUtil.compareWithGameKey(type, PlayCate.C_OE.value) -> return PlayCate.C_OE.ordinal
            TextUtil.compareWithGameKey(type, PlayCate.OE.value) -> return PlayCate.OE.ordinal

            type == PlayCate.SCO.value -> return PlayCate.SCO.ordinal

            type == PlayCate.TG.value -> return PlayCate.TG.ordinal

            TextUtil.compareWithGameKey(type, PlayCate.TG_.value) -> return PlayCate.TG_.ordinal

            /**/
            type == PlayCate.SINGLE_BTS.value -> return PlayCate.SINGLE_BTS.ordinal
            type == PlayCate.OU_BTS.value -> return PlayCate.OU_BTS.ordinal
            TextUtil.compareWithGameKey(type, PlayCate.BTS.value) -> return PlayCate.BTS.ordinal

            type == PlayCate.GT1ST.value -> return PlayCate.GT1ST.ordinal

            type == PlayCate.SBH.value -> return PlayCate.SBH.ordinal

            type == PlayCate.WBH.value -> return PlayCate.WBH.ordinal

            type == PlayCate.WEH.value -> return PlayCate.WEH.ordinal

            /**/
            type == PlayCate.HWMG_SINGLE.value -> return PlayCate.HWMG_SINGLE.ordinal
            TextUtil.compareWithGameKey(type, PlayCate.WM.value) -> return PlayCate.WM.ordinal

            type == PlayCate.CLSH.value -> return PlayCate.CLSH.ordinal

            type == PlayCate.HTFT.value -> return PlayCate.HTFT.ordinal

            type == PlayCate.W3.value -> return PlayCate.W3.ordinal

            TextUtil.compareWithGameKey(type, PlayCate.TG_OU.value) -> return PlayCate.TG_OU.ordinal

            TextUtil.compareWithGameKey(type, PlayCate.C_OU.value) -> return PlayCate.C_OU.ordinal

            TextUtil.compareWithGameKey(
                type,
                PlayCate.OU_I_OT.value
            ) -> return PlayCate.OU_I_OT.ordinal

            TextUtil.compareWithGameKey(
                type,
                PlayCate.OU_SEG.value
            ) -> return PlayCate.OU_SEG.ordinal

            TextUtil.compareWithGameKey(
                type,
                PlayCate.SINGLE_OU.value
            ) -> return PlayCate.SINGLE_OU.ordinal

            TextUtil.compareWithGameKey(
                type,
                PlayCate.SINGLE_FLG.value
            ) -> return PlayCate.SINGLE_FLG.ordinal


            type == PlayCate.SINGLE.value -> return if (sportCode == GameType.FT.key) {
                PlayCate.SINGLE.ordinal
            } else {
                PlayCate.SINGLE_2.ordinal
            }

            TextUtil.compareWithGameKey(
                type,
                PlayCate.SINGLE_1ST.value
            ) -> return if (sportCode == GameType.FT.key) {
                PlayCate.SINGLE_1ST.ordinal
            } else {
                PlayCate.SINGLE_1ST_2.ordinal
            }

            TextUtil.compareWithGameKey(
                type,
                PlayCate.SINGLE_2ST.value
            ) -> return if (sportCode == GameType.FT.key) {
                PlayCate.SINGLE_2ST.ordinal
            } else {
                PlayCate.SINGLE_2ST_2.ordinal
            }

            type == PlayCate.SINGLE_OT.value -> return if (sportCode == GameType.FT.key) {
                PlayCate.SINGLE_OT.ordinal
            } else {
                PlayCate.SINGLE_OT_2.ordinal
            }

            TextUtil.compareWithGameKey(
                type,
                PlayCate.SINGLE_SEG.value
            ) -> return if (sportCode == GameType.FT.key) {
                PlayCate.SINGLE_SEG.ordinal
            } else {
                PlayCate.SINGLE_SEG_2.ordinal
            }

            type == PlayCate.NGOAL_1.value -> return PlayCate.NGOAL_1.ordinal

            type == PlayCate.TWTN.value -> return PlayCate.TWTN.ordinal

            else -> {
                return PlayCate.UNCHECK.ordinal
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layout: Int = when (viewType) {

            PlayCate.OU_TTS1ST.ordinal,
            PlayCate.OU_OE.ordinal,
            PlayCate.OU_BTS.ordinal -> LayoutType.GROUP_4.layout

            PlayCate.DC_OU.ordinal,
            PlayCate.DC_BTS.ordinal,
            PlayCate.DC_FLG.ordinal,
            PlayCate.SINGLE_FLG.ordinal,
            PlayCate.SINGLE_BTS.ordinal,
            PlayCate.SINGLE_OU.ordinal -> LayoutType.GROUP_6.layout

            PlayCate.CS.ordinal -> LayoutType.CS.layout

            PlayCate.FGLG.ordinal -> LayoutType.FG_LG.layout

            PlayCate.SCO.ordinal -> LayoutType.SCO.layout

            PlayCate.DC.ordinal,
            PlayCate.GT1ST.ordinal,
            PlayCate.SBH.ordinal,
            PlayCate.WBH.ordinal,
            PlayCate.WEH.ordinal,
            PlayCate.WM.ordinal,
            PlayCate.HTFT.ordinal,
            PlayCate.W3.ordinal,
            PlayCate.HDP_ONE_LIST.ordinal,
            PlayCate.NGOAL_1.ordinal,
            PlayCate.HWMG_SINGLE.ordinal -> LayoutType.ONE_LIST.layout

            PlayCate.TWTN.ordinal,
            PlayCate.HDP.ordinal,
            PlayCate.OU.ordinal,
            PlayCate.OU_1ST.ordinal,
            PlayCate.OU_2ST.ordinal,
            PlayCate.OE.ordinal,
            PlayCate.TG.ordinal,
            PlayCate.TG_.ordinal,
            PlayCate.BTS.ordinal,
            PlayCate.CLSH.ordinal,
            PlayCate.TG_OU.ordinal,
            PlayCate.C_OU.ordinal,
            PlayCate.C_OE.ordinal,
            PlayCate.OU_I_OT.ordinal,
            PlayCate.OU_SEG.ordinal -> LayoutType.TWO_SPAN_COUNT.layout

            PlayCate.SINGLE.ordinal,
            PlayCate.SINGLE_1ST.ordinal,
            PlayCate.SINGLE_2ST.ordinal,
            PlayCate.SINGLE_OT.ordinal,
            PlayCate.SINGLE_SEG.ordinal -> LayoutType.SINGLE.layout

            PlayCate.SINGLE_2.ordinal,
            PlayCate.SINGLE_1ST_2.ordinal,
            PlayCate.SINGLE_2ST_2.ordinal,
            PlayCate.SINGLE_OT_2.ordinal,
            PlayCate.SINGLE_SEG_2.ordinal -> LayoutType.SINGLE_2_ITEM.layout

            else -> LayoutType.ONE_LIST.layout

        }

        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(layout, parent, false),
            viewType
        ).apply {

            when (layout) {

                LayoutType.SCO.layout -> {
                    rvBet?.apply {
                        addItemDecoration(
                            DividerItemDecorator(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.divider_color_silverlight_1dp
                                )
                            )
                        )
                    }
                }

                LayoutType.GROUP_4.layout,
                LayoutType.GROUP_6.layout -> {
                    rvBet?.apply {
                        addItemDecoration(
                            DividerItemDecorator(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.divider_color_white4_2dp
                                )
                            )
                        )
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
        holder.bindModel(oddsDetailDataList[position])
    }


    fun notifyDataSetChangedByCode(code: String) {
        this.code = code
        notifyDataSetChanged()
    }


    @Suppress("UNCHECKED_CAST")
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

        //SCO, CS
        val tvHomeName: TextView? = itemView.findViewById(R.id.tv_home_name)
        val tvAwayName: TextView? = itemView.findViewById(R.id.tv_away_name)

        fun bindModel(oddsDetail: OddsDetailListData) {

            val type = oddsDetail.gameType

            tvGameName.text = if (type.contains(":"))
                oddsDetail.name.plus("  ").plus(type.split(":")[1])
            else oddsDetail.name

            controlExpandBottom(oddsDetail.isExpand)

            clItem.setOnClickListener {
                oddsDetail.isExpand = !oddsDetail.isExpand
                notifyItemChanged(adapterPosition)
            }

            rvBet?.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE


            when (viewType) {
                PlayCate.TWTN.ordinal,
                PlayCate.CLSH.ordinal,
                PlayCate.OU.ordinal,
                PlayCate.OU_1ST.ordinal,
                PlayCate.OU_2ST.ordinal,
                PlayCate.OE.ordinal,
                PlayCate.TG.ordinal,
                PlayCate.TG_.ordinal,
                PlayCate.TG_OU.ordinal,
                PlayCate.C_OU.ordinal,
                PlayCate.C_OE.ordinal,
                PlayCate.OU_I_OT.ordinal,
                PlayCate.OU_SEG.ordinal,
                PlayCate.BTS.ordinal,
                PlayCate.HDP.ordinal -> for2SpanCount(oddsDetail)

                PlayCate.CS.ordinal -> forCS(oddsDetail)

                PlayCate.SINGLE_OT.ordinal,
                PlayCate.SINGLE_SEG.ordinal,
                PlayCate.SINGLE_1ST.ordinal,
                PlayCate.SINGLE_2ST.ordinal,
                PlayCate.SINGLE.ordinal -> forSingle(oddsDetail, 3)

                PlayCate.SINGLE_OT_2.ordinal,
                PlayCate.SINGLE_SEG_2.ordinal,
                PlayCate.SINGLE_1ST_2.ordinal,
                PlayCate.SINGLE_2ST_2.ordinal,
                PlayCate.SINGLE_2.ordinal -> forSingle(oddsDetail, 2)

                PlayCate.FGLG.ordinal -> forFGLG(oddsDetail)

                PlayCate.HWMG_SINGLE.ordinal,
                PlayCate.WBH.ordinal,
                PlayCate.WEH.ordinal,
                PlayCate.SBH.ordinal,
                PlayCate.NGOAL_1.ordinal,
                PlayCate.HDP_ONE_LIST.ordinal,
                PlayCate.W3.ordinal,
                PlayCate.DC.ordinal,
                PlayCate.GT1ST.ordinal,
                PlayCate.WM.ordinal,
                PlayCate.HTFT.ordinal -> oneList(oddsDetail)

                PlayCate.SCO.ordinal -> forSCO(oddsDetail, adapterPosition)

                PlayCate.DC_OU.ordinal,
                PlayCate.DC_BTS.ordinal,
                PlayCate.DC_FLG.ordinal -> group6ItemForDC(oddsDetail)

                PlayCate.SINGLE_FLG.ordinal,
                PlayCate.SINGLE_BTS.ordinal,
                PlayCate.SINGLE_OU.ordinal -> group6Item(oddsDetail)

                PlayCate.OU_BTS.ordinal -> group4ItemForOuBts(oddsDetail)

                PlayCate.OU_TTS1ST.ordinal,
                PlayCate.OU_OE.ordinal -> group4ItemForOuTag(oddsDetail)

                //臨時新增或尚未確定的排版 以單行列表作為排版
                PlayCate.UNCHECK.ordinal -> oneList(oddsDetail)

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

            tvHomeName?.text = homeName
            tvAwayName?.text = awayName

            itemView.findViewById<LinearLayout>(R.id.ll_content).visibility =
                if (oddsDetail.isExpand) View.VISIBLE else View.GONE

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
                adapter =
                    TypeCSAdapter(oddsDetail, homeList, onOddClickListener, betInfoList, oddsType)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            rvDraw?.apply {
                adapter =
                    TypeCSAdapter(oddsDetail, drawList, onOddClickListener, betInfoList, oddsType)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            rvAway?.apply {
                adapter =
                    TypeCSAdapter(oddsDetail, awayList, onOddClickListener, betInfoList, oddsType)
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
                adapter = TypeTwoSpanCountGridAdapter(
                    oddsDetail,
                    onOddClickListener,
                    betInfoList,
                    oddsType
                )
                layoutManager = GridLayoutManager(itemView.context, 2)
            }
        }

        private fun forFGLG(oddsDetail: OddsDetailListData) {
            itemView.findViewById<ConstraintLayout>(R.id.cl_tab).visibility =
                if (oddsDetail.isExpand) View.VISIBLE else View.GONE
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
                    (rvBet?.adapter as TypeOneListAdapter).mOddsDetail =
                        selectFGLG(oddsDetail.apply {
                            gameTypeFgLgSelect = FGLGType.FG
                        })
                }
            }

            tvLg?.apply {
                isSelected = oddsDetail.gameTypeFgLgSelect == FGLGType.LG
                setOnClickListener {
                    (rvBet?.adapter as TypeOneListAdapter).mOddsDetail =
                        selectFGLG(oddsDetail.apply {
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

        private fun forSCO(oddsDetail: OddsDetailListData, position: Int) {

            val teamNameList = setupSCOTeamName(oddsDetail)

            itemView.findViewById<ConstraintLayout>(R.id.cl_tab).visibility =
                if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            rvBet?.apply {
                adapter = TypeSCOAdapter(
                    selectSCO(
                        oddsDetail,
                        oddsDetail.gameTypeSCOSelect ?: teamNameList[0],
                        teamNameList
                    ),
                    onOddClickListener,
                    betInfoList,
                    oddsType,
                    object : TypeSCOAdapter.OnMoreClickListener {
                        override fun click() {
                            oddsDetail.isMoreExpand = !oddsDetail.isMoreExpand
                            this@OddsDetailListAdapter.notifyItemChanged(position)
                        }
                    }
                )
                layoutManager = LinearLayoutManager(itemView.context)
            }

            tvHomeName?.apply {
                isSelected = oddsDetail.gameTypeSCOSelect == teamNameList[0]
                setOnClickListener {
                    (rvBet?.adapter as TypeSCOAdapter).mOddsDetail = selectSCO(
                        oddsDetail = oddsDetail.apply {
                            gameTypeSCOSelect = teamNameList[0]
                            isMoreExpand = false
                        },
                        teamName = oddsDetail.gameTypeSCOSelect ?: teamNameList[0],
                        teamNameList = teamNameList
                    )
                    this@OddsDetailListAdapter.notifyItemChanged(position)
                }
            }

            tvAwayName?.apply {
                isSelected = oddsDetail.gameTypeSCOSelect == teamNameList[1]
                setOnClickListener {
                    (rvBet?.adapter as TypeSCOAdapter).mOddsDetail = selectSCO(
                        oddsDetail = oddsDetail.apply {
                            gameTypeSCOSelect = teamNameList[1]
                            isMoreExpand = false
                        },
                        teamName = oddsDetail.gameTypeSCOSelect ?: teamNameList[1],
                        teamNameList = teamNameList
                    )
                    this@OddsDetailListAdapter.notifyItemChanged(position)
                }
            }
        }

        private fun setupSCOTeamName(oddsDetail: OddsDetailListData): MutableList<String> {
            val groupTeamName = oddsDetail.oddArrayList.groupBy {
                it?.extInfo
            }
            return mutableListOf<String>().apply {
                groupTeamName.forEach {
                    it.key?.let { key ->
                        add(
                            key
                        )
                    }
                }
            }.apply {
                tvHomeName?.text = this[0]
                tvAwayName?.text = this[1]
            }
        }

        private fun selectSCO(
            oddsDetail: OddsDetailListData,
            teamName: String,
            teamNameList: MutableList<String>
        ): OddsDetailListData {
            oddsDetail.gameTypeSCOSelect = teamName

            tvHomeName?.isSelected = teamName == teamNameList[0]
            tvAwayName?.isSelected = teamName != teamNameList[0]

            //建立球員列表(一個球員三個賠率)
            var map: HashMap<String, List<Odd?>> = HashMap()

            //過濾掉 其他:(第一、任何、最後), 无進球
            //依隊名分開
            oddsDetail.oddArrayList.filterNot { odd ->
                odd?.spread == OddSpreadForSCO.SCORE_1ST_O.spread ||
                        odd?.spread == OddSpreadForSCO.SCORE_ANT_O.spread ||
                        odd?.spread == OddSpreadForSCO.SCORE_LAST_O.spread ||
                        odd?.spread == OddSpreadForSCO.SCORE_N.spread
            }.groupBy {
                it?.extInfo
            }.forEach {
                if (it.key == teamName) {
                    map = it.value.groupBy { odd -> odd?.name } as HashMap<String, List<Odd?>>
                }
            }

            //保留 其他:(第一、任何、最後), 无進球
            //依球員名稱分開
            //倒序排列 多的在前(無進球只有一種賠率 放最後面)
            //添加至球員列表內
            oddsDetail.oddArrayList.filter { odd ->
                odd?.spread == OddSpreadForSCO.SCORE_1ST_O.spread ||
                        odd?.spread == OddSpreadForSCO.SCORE_ANT_O.spread ||
                        odd?.spread == OddSpreadForSCO.SCORE_LAST_O.spread ||
                        odd?.spread == OddSpreadForSCO.SCORE_N.spread
            }.groupBy {
                it?.name
            }.entries.sortedByDescending {
                it.value.size
            }.associateBy(
                { it.key }, { it.value }
            ).forEach {
                map[it.key ?: ""] = it.value
            }

            return OddsDetailListData(
                oddsDetail.gameType,
                oddsDetail.typeCodes,
                oddsDetail.name,
                oddsDetail.oddArrayList
            ).apply {
                isExpand = oddsDetail.isExpand
                isMoreExpand = oddsDetail.isMoreExpand
                gameTypeSCOSelect = oddsDetail.gameTypeSCOSelect
                scoItem = map
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

        private fun group6AdapterSetup(oddsDetail: OddsDetailListData): Type6GroupAdapter =
            Type6GroupAdapter(
                oddsDetail.apply {
                    groupItem =
                        oddsDetail.oddArrayList.groupBy { it?.spread } as HashMap<String, List<Odd?>>
                },
                onOddClickListener,
                betInfoList,
                oddsType
            )

        private fun group4AdapterSetup(oddsDetail: OddsDetailListData): Type4GroupAdapter =
            Type4GroupAdapter(
                oddsDetail.apply {

                    //依key分組 有元件需要用key做顯示
                    val keys = (oddsDetail.oddArrayList
                        .groupBy { it?.spread }
                        .filter { it.key != null } as HashMap<String, List<Odd?>>)
                        .mapTo(mutableListOf(), { it.key })

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