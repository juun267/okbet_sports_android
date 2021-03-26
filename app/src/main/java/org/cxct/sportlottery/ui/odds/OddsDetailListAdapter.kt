package org.cxct.sportlottery.ui.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.GridItemDecoration
import org.cxct.sportlottery.util.OddButtonHighLight
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.TextUtil
import java.util.*
import kotlin.collections.ArrayList


class OddsDetailListAdapter(private val onOddClickListener: OnOddClickListener, private val sportGameType: String) :
    RecyclerView.Adapter<OddsDetailListAdapter.ViewHolder>() {


    private var betInfoList: MutableList<BetInfoListData> = mutableListOf()


    var oddsDetailDataList: ArrayList<OddsDetailListData> = ArrayList()


    var curMatchId: String? = null


    var homeName: String? = null


    var awayName: String? = null


    var updatedOddsDetailDataList = ArrayList<OddsDetailListData>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    fun setBetInfoList(betInfoList: MutableList<BetInfoListData>) {
        this.betInfoList.clear()
        this.betInfoList.addAll(betInfoList)
        notifyDataSetChanged()
    }


    fun setCurrentMatchId(mid: String?) {
        curMatchId = mid
    }


    private lateinit var code: String


    enum class GameType(val value: String, val layout: Int, val type: Int) {
        HDP("HDP", R.layout.content_odds_detail_list_hdp, 0),//让球
        OU("O/U", R.layout.content_odds_detail_list_two_sides, 1),//大小
        OU_1ST("O/U-1ST", R.layout.content_odds_detail_list_two_sides, 2),//大/小-上半场
        OU_2ST("O/U-2ST", R.layout.content_odds_detail_list_two_sides, 3),//大/小-下半场
        CS("CS", R.layout.content_odds_detail_list_cs, 4),//波胆
        FG("FG", R.layout.content_odds_detail_list_one, 5),//首先进球
        LG("LG", R.layout.content_odds_detail_list_one, 6),//最后进球
        SINGLE("1X2", R.layout.content_odds_detail_list_single, 7),//独赢
        DC("DC", R.layout.content_odds_detail_list_one, 8),//双重机会
        OE("O/E", R.layout.content_odds_detail_list_two_sides, 9),//单/双
        SCO("SCO", R.layout.content_odds_detail_list_one, 10),//进球球员
        TG("TG", R.layout.content_odds_detail_list_one, 11),//总进球数
        TG_("TG-", R.layout.content_odds_detail_list_one, 12),//进球数-半場
        BTS("BTS", R.layout.content_odds_detail_list_one, 13),//双方球队进球
        GT1ST("GT1ST", R.layout.content_odds_detail_list_one, 14),//首个入球时间
        SBH("SBH", R.layout.content_odds_detail_list_one, 15),//双半场进球
        WBH("WBH", R.layout.content_odds_detail_list_one, 16),//赢得所有半场
        WEH("WEH", R.layout.content_odds_detail_list_one, 17),//赢得任一半场
        WM("WM", R.layout.content_odds_detail_list_one, 18),//净胜球数
        CLSH("CLSH", R.layout.content_odds_detail_list_one, 19),//零失球
        HTFT("HT/FT", R.layout.content_odds_detail_list_one, 20),//半场/全场
        W3("W3", R.layout.content_odds_detail_list_group, 21),//三项让球
        TG_OU("TG&O/U", R.layout.content_odds_detail_list_two_sides, 22),//球队进球数&大/小
        C_OU("CORNER-O/U", R.layout.content_odds_detail_list_two_sides, 23),//角球大/小
        C_OE("CORNER-OE", R.layout.content_odds_detail_list_two_sides, 24),//角球单/双
        OU_I_OT("O/U-INCL-OT", R.layout.content_odds_detail_list_two_sides, 25),//大/小(含加时)
        OU_SEG("O/U-SEG", R.layout.content_odds_detail_list_two_sides, 26),//总得分大/小-第X节
        SINGLE_1ST("1X2-1ST", R.layout.content_odds_detail_list_single, 27),//独赢-上半場
        SINGLE_2ST("1X2-2ST", R.layout.content_odds_detail_list_single, 28),//独赢-下半場
        SINGLE_OU("1X2-O/U", R.layout.content_odds_detail_list_one, 29),//独赢大/小
        SINGLE_OT("1X2-INCL-OT", R.layout.content_odds_detail_list_one, 30),//独赢(含加时)
        SINGLE_SEG("1X2-SEG", R.layout.content_odds_detail_list_single, 31),//独赢-第X节
    }


    override fun getItemViewType(position: Int): Int {

        val type = oddsDetailDataList[position].gameType

        when {
            checkKey(type, GameType.HDP.value) -> return GameType.HDP.type

            type == GameType.OU.value -> return GameType.OU.type

            type == GameType.OU_1ST.value -> return GameType.OU_1ST.type

            type == GameType.OU_2ST.value -> return GameType.OU_2ST.type

            checkKey(type, GameType.CS.value) -> return GameType.CS.type

            type == GameType.FG.value -> return GameType.FG.type

            type == GameType.LG.value -> return GameType.LG.type

            type == GameType.SINGLE.value -> return GameType.SINGLE.type

            checkKey(type, GameType.DC.value) -> return GameType.DC.type

            checkKey(type, GameType.OE.value) -> return GameType.OE.type

            checkKey(type, GameType.SCO.value) -> return GameType.SCO.type

            type == GameType.TG.value -> return GameType.TG.type

            checkKey(type, GameType.TG_.value) -> return GameType.TG_.type

            checkKey(type, GameType.BTS.value) -> return GameType.BTS.type

            type == GameType.GT1ST.value -> return GameType.GT1ST.type

            type == GameType.SBH.value -> return GameType.SBH.type

            type == GameType.WBH.value -> return GameType.WBH.type

            type == GameType.WEH.value -> return GameType.WEH.type

            checkKey(type, GameType.WM.value) -> return GameType.WM.type

            type == GameType.CLSH.value -> return GameType.CLSH.type

            type == GameType.HTFT.value -> return GameType.HTFT.type

            type == GameType.W3.value -> return GameType.W3.type

            checkKey(type, GameType.TG_OU.value) -> return GameType.TG_OU.type

            checkKey(type, GameType.C_OU.value) -> return GameType.C_OU.type

            checkKey(type, GameType.C_OE.value) -> return GameType.C_OE.type

            checkKey(type, GameType.OU_I_OT.value) -> return GameType.OU_I_OT.type

            checkKey(type, GameType.OU_SEG.value) -> return GameType.OU_SEG.type

            checkKey(type, GameType.SINGLE_1ST.value) -> return GameType.SINGLE_1ST.type

            checkKey(type, GameType.SINGLE_2ST.value) -> return GameType.SINGLE_2ST.type

            checkKey(type, GameType.SINGLE_OU.value) -> return GameType.SINGLE_OU.type

            type == GameType.SINGLE_OT.value -> return GameType.SINGLE_OT.type

            checkKey(type, GameType.SINGLE_SEG.value) -> return GameType.SINGLE_SEG.type

            else -> {
                return -1
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        var layout: Int = GameType.HDP.layout

        when (viewType) {
            GameType.HDP.type -> layout = GameType.HDP.layout
            GameType.OU.type -> layout = GameType.OU.layout
            GameType.OU_1ST.type -> layout = GameType.OU_1ST.layout
            GameType.OU_2ST.type -> layout = GameType.OU_2ST.layout
            GameType.CS.type -> layout = GameType.CS.layout
            GameType.FG.type -> layout = GameType.FG.layout
            GameType.LG.type -> layout = GameType.LG.layout

            GameType.SINGLE.type -> {
                layout = if (sportGameType == SportType.FOOTBALL.code) {
                    GameType.SINGLE.layout
                } else {
                    GameType.HDP.layout
                }
            }

            GameType.DC.type -> layout = GameType.DC.layout
            GameType.OE.type -> layout = GameType.OE.layout
            GameType.SCO.type -> layout = GameType.SCO.layout
            GameType.TG.type -> layout = GameType.TG.layout
            GameType.TG_.type -> layout = GameType.TG_.layout
            GameType.BTS.type -> layout = GameType.BTS.layout
            GameType.GT1ST.type -> layout = GameType.GT1ST.layout
            GameType.SBH.type -> layout = GameType.SBH.layout
            GameType.WBH.type -> layout = GameType.WBH.layout
            GameType.WEH.type -> layout = GameType.WEH.layout
            GameType.WM.type -> layout = GameType.WM.layout
            GameType.CLSH.type -> layout = GameType.CLSH.layout
            GameType.HTFT.type -> layout = GameType.HTFT.layout
            GameType.W3.type -> layout = GameType.W3.layout
            GameType.W3.type -> layout = GameType.W3.layout
            GameType.TG_OU.type -> layout = GameType.TG_OU.layout
            GameType.C_OU.type -> layout = GameType.C_OU.layout
            GameType.C_OE.type -> layout = GameType.C_OE.layout
            GameType.OU_I_OT.type -> layout = GameType.OU_I_OT.layout
            GameType.OU_SEG.type -> layout = GameType.OU_SEG.layout
            GameType.SINGLE_1ST.type -> {
                layout = if (sportGameType == SportType.FOOTBALL.code) {
                    GameType.SINGLE_1ST.layout
                } else {
                    GameType.HDP.layout
                }
            }
            GameType.SINGLE_2ST.type -> {
                layout = if (sportGameType == SportType.FOOTBALL.code) {
                    GameType.SINGLE_2ST.layout
                } else {
                    GameType.HDP.layout
                }
            }
            GameType.SINGLE_OU.type -> layout = GameType.SINGLE_OU.layout
            GameType.SINGLE_OT.type -> layout = GameType.SINGLE_OT.layout
            GameType.SINGLE_SEG.type -> {
                layout = if (sportGameType == SportType.FOOTBALL.code) {
                    GameType.SINGLE_SEG.layout
                } else {
                    GameType.HDP.layout
                }
            }
        }

        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false), viewType)
    }


    override fun getItemCount(): Int {
        return oddsDetailDataList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        updateItemDataFromSocket(oddsDetailDataList[position], updatedOddsDetailDataList)
        holder.bindModel(oddsDetailDataList[position], position)
    }


    private fun updateItemDataFromSocket(oddsDetail: OddsDetailListData, updatedOddsDetail: ArrayList<OddsDetailListData>) {
        val oldOddList = oddsDetail.oddArrayList
        var newOddList = listOf<Odd>()

        for (item in updatedOddsDetail) {
            if (item.gameType == oddsDetail.gameType) {
                newOddList = item.oddArrayList
                break
            }
        }

        oldOddList.forEach { oldOddData ->
            newOddList.forEach { newOddData ->
                if (oldOddData.id == newOddData.id) {
                    oldOddData.name = newOddData.name
                    oldOddData.extInfo = newOddData.extInfo
                    oldOddData.spread = newOddData.spread

                    //先判斷大小
                    oldOddData.oddState = getOddState(oldOddData, newOddData)

                    //再帶入新的賠率
                    oldOddData.odds = newOddData.odds

                    oldOddData.status = newOddData.status
                    oldOddData.producerId = newOddData.producerId
                }
            }
        }
    }


    private fun getOddState(oldItem: Odd, it: Odd): Int {
        val oldOdd = oldItem.odds ?: 0.0
        val newOdd = it.odds ?: 0.0
        return when {
            newOdd == oldOdd -> OddState.SAME.state
            newOdd > oldOdd -> OddState.LARGER.state
            newOdd < oldOdd -> OddState.SMALLER.state
            else -> OddState.SAME.state
        }
    }


    fun notifyDataSetChangedByCode(code: String) {
        this.code = code
        notifyDataSetChanged()
    }


    private fun checkKey(type: String, value: String): Boolean {
        return type == value || type.contains(value)
    }


    inner class ViewHolder(itemView: View, var viewType: Int) : RecyclerView.ViewHolder(itemView) {

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

        private val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        private val llItem = itemView.findViewById<LinearLayout>(R.id.ll_item)
        private val ivArrowUp = itemView.findViewById<ImageView>(R.id.iv_arrow_up)
        private val vLine = itemView.findViewById<View>(R.id.v_line)

        fun bindModel(oddsDetail: OddsDetailListData, position: Int) {

            val type = oddsDetailDataList[position].gameType

            if (type.contains(":")) {
                tvName.text = oddsDetail.name.plus("  ").plus(type.split(":")[1])
            } else {
                tvName.text = oddsDetail.name
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
                GameType.HDP.type -> forHDP(oddsDetail)
                GameType.W3.type -> groupItem(oddsDetail, 3)

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

                GameType.SINGLE_SEG.type,
                GameType.SINGLE_1ST.type,
                GameType.SINGLE_2ST.type,
                GameType.SINGLE.type -> {
                    if (sportGameType == SportType.FOOTBALL.code) {
                        forSingle(oddsDetail)
                    } else {
                        forHDP(oddsDetail)
                    }
                }


                GameType.SINGLE_OT.type,
                GameType.SINGLE_OU.type,
                GameType.FG.type,
                GameType.LG.type,
                GameType.DC.type,
//                GameType.SCO.type,
                GameType.TG.type,
                GameType.TG_.type,
                GameType.BTS.type,
                GameType.GT1ST.type,
                GameType.SBH.type,
                GameType.WBH.type,
                GameType.WEH.type,
                GameType.WM.type,
                GameType.CLSH.type,
                GameType.HTFT.type -> oneList(oddsDetail, false)

                GameType.SCO.type -> oneList(oddsDetail, true)

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

        private fun oneList(oddsDetail: OddsDetailListData, isSCO: Boolean) {
            val rvBet = itemView.findViewById<RecyclerView>(R.id.rv_bet)
            rvBet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            if (isSCO) {
                for (i in oddsDetail.oddArrayList.indices) {
                    if (!oddsDetail.isMoreExpand && i > 4) {
                        oddsDetail.oddArrayList[i].itemViewVisible = false
                    }
                }
            }

            rvBet.apply {
                adapter = TypeOneListAdapter(
                    oddsDetail,
                    onOddClickListener,
                    betInfoList,
                    curMatchId,
                    isSCO,
                    object : TypeOneListAdapter.OnMoreClickListener {
                        override fun click() {
                            for (i in oddsDetail.oddArrayList.indices) {
                                if (i > 4) {
                                    oddsDetail.oddArrayList[i].itemViewVisible = !oddsDetail.oddArrayList[i].itemViewVisible
                                }
                            }
                            oddsDetail.isMoreExpand = !oddsDetail.isMoreExpand
                            adapter?.notifyDataSetChanged()
                        }
                    }
                )
                layoutManager = LinearLayoutManager(itemView.context)
                if (itemDecorationCount == 0) {
                    addItemDecoration(
                        SpaceItemDecoration(
                            context,
                            R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_one_list
                        )
                    )
                }
            }
        }

        private fun forCS(oddsDetail: OddsDetailListData) {
            itemView.findViewById<LinearLayout>(R.id.ll_content).visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE

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

                        val tvOdds = itemView.findViewById<TextView>(R.id.tv_odds)
                        val vCover = itemView.findViewById<ImageView>(R.id.iv_disable_cover)

                        odd.odds?.let { odds -> tvOdds.text = TextUtil.formatForOdd(odds) }

                        OddButtonHighLight.set(tvOdds, null, odd)

                        when (odd.status) {
                            BetStatus.ACTIVATED.code -> {
                                itemView.visibility = View.VISIBLE
                                vCover.visibility = View.GONE
                                tvOdds.isEnabled = true

                                val select = betInfoList.any { it.matchOdd.oddsId == odd.id }
                                odd.isSelect = select

                                tvOdds.isSelected = odd.isSelect ?: false
                                tvOdds.setOnClickListener {
                                    if (odd.isSelect != true) {
                                        if (curMatchId != null && betInfoList.any { it.matchOdd.matchId == curMatchId }) {
                                            return@setOnClickListener
                                        }
                                        onOddClickListener.getBetInfoList(odd)
                                    } else {
                                        onOddClickListener.removeBetInfoItem(odd)
                                    }
                                }
                            }
                            BetStatus.LOCKED.code -> {
                                itemView.visibility = View.VISIBLE
                                vCover.visibility = View.VISIBLE
                                tvOdds.isEnabled = false
                            }
                            BetStatus.DEACTIVATED.code -> {
                                //比照h5照樣顯示（文件為不顯示）
                                itemView.visibility = View.VISIBLE
                                vCover.visibility = View.VISIBLE
                                tvOdds.isEnabled = false
                            }
                        }
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

            itemView.findViewById<RecyclerView>(R.id.rv_home).apply {
                adapter = TypeCSAdapter(homeList, onOddClickListener, betInfoList, curMatchId)
                layoutManager = LinearLayoutManager(itemView.context)
                if (itemDecorationCount == 0) {
                    addItemDecoration(
                        SpaceItemDecoration(
                            context,
                            R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_one_list
                        )
                    )
                }
            }

            itemView.findViewById<RecyclerView>(R.id.rv_draw).apply {
                adapter = TypeCSAdapter(drawList, onOddClickListener, betInfoList, curMatchId)
                layoutManager = LinearLayoutManager(itemView.context)
                if (itemDecorationCount == 0) {
                    addItemDecoration(
                        SpaceItemDecoration(
                            context,
                            R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_one_list
                        )
                    )
                }
            }

            itemView.findViewById<RecyclerView>(R.id.rv_away).apply {
                adapter = TypeCSAdapter(awayList, onOddClickListener, betInfoList, curMatchId)
                layoutManager = LinearLayoutManager(itemView.context)
                if (itemDecorationCount == 0) {
                    addItemDecoration(
                        SpaceItemDecoration(
                            context,
                            R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_one_list
                        )
                    )
                }
            }
        }

        private fun forSingle(oddsDetail: OddsDetailListData) {

            itemView.findViewById<TextView>(R.id.tv_home_name).text = oddsDetail.oddArrayList[0].name
            itemView.findViewById<TextView>(R.id.tv_draw).text = oddsDetail.oddArrayList[1].name
            itemView.findViewById<TextView>(R.id.tv_away_name).text = oddsDetail.oddArrayList[2].name
            itemView.findViewById<RelativeLayout>(R.id.rl_game).visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            val rvBet = itemView.findViewById<RecyclerView>(R.id.rv_bet)
            rvBet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            rvBet.apply {
                adapter = TypeSingleAdapter(oddsDetail.oddArrayList, onOddClickListener, betInfoList, curMatchId)
                layoutManager = GridLayoutManager(itemView.context, 3)
                if (itemDecorationCount == 0) {
                    addItemDecoration(
                        GridItemDecoration(
                            itemView.context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_grid_3),
                            itemView.context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_grid_3),
                            ContextCompat.getColor(itemView.context, R.color.colorWhite),
                            false
                        )
                    )
                }
            }
        }

        private fun forHDP(oddsDetail: OddsDetailListData) {
            itemView.findViewById<TextView>(R.id.tv_home_name).text = homeName
            itemView.findViewById<TextView>(R.id.tv_away_name).text = awayName
            itemView.findViewById<RelativeLayout>(R.id.rl_game).visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            val rvBet = itemView.findViewById<RecyclerView>(R.id.rv_bet)
            rvBet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            rvBet.apply {
                adapter = TypeHDPAdapter(oddsDetail.oddArrayList, onOddClickListener, betInfoList, curMatchId)
                layoutManager = GridLayoutManager(itemView.context, 2)
                if (itemDecorationCount == 0) {
                    addItemDecoration(
                        GridItemDecoration(
                            itemView.context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_grid_2_horizontal),
                            itemView.context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_grid_2_vertical),
                            ContextCompat.getColor(itemView.context, R.color.colorWhite),
                            false
                        )
                    )
                }
            }
        }

        private fun groupItem(oddsDetail: OddsDetailListData, groupItemCount: Int) {
            val rvBet = itemView.findViewById<RecyclerView>(R.id.rv_bet)
            rvBet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            rvBet.apply {
                adapter = TypeGroupItemAdapter(oddsDetail.oddArrayList, groupItemCount, onOddClickListener, betInfoList, curMatchId)
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }

        private fun twoSides(oddsDetail: OddsDetailListData) {
            val rvBet = itemView.findViewById<RecyclerView>(R.id.rv_bet)
            rvBet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            rvBet.apply {
                adapter = TypeTwoSidesAdapter(oddsDetail.oddArrayList, onOddClickListener, betInfoList, curMatchId)
                layoutManager = GridLayoutManager(itemView.context, 2)
                if (itemDecorationCount == 0) {
                    addItemDecoration(
                        GridItemDecoration(
                            itemView.context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_two_side),
                            itemView.context.resources.getDimensionPixelOffset(R.dimen.recyclerview_item_dec_spec_odds_detail_game_type_two_side),
                            ContextCompat.getColor(itemView.context, R.color.colorWhite6),
                            false
                        )
                    )
                }
            }
        }

    }


}