package org.cxct.sportlottery.ui.odds

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil


class OddsDetailListAdapter(private val onOddClickListener: OnOddClickListener) :
    RecyclerView.Adapter<OddsDetailListAdapter.ViewHolder>() {

    private var betInfoList: MutableList<BetInfoListData> = mutableListOf()

    var oddsDetailDataList: ArrayList<OddsDetailListData> = ArrayList()

    var curMatchId: String? = null


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
        HDP("HDP", R.layout.content_odds_detail_list_group_item, 0),//让球
        OU("O/U", R.layout.content_odds_detail_list_two_sides, 1),//大小
        OU_1ST("O/U-1ST", R.layout.content_odds_detail_list_two_sides, 2),//大/小-上半场
        OU_2ST("O/U-2ST", R.layout.content_odds_detail_list_two_sides, 3),//大/小-下半场
        CS("CS", R.layout.content_odds_detail_list_cs, 4),//波胆
        FG("FG", R.layout.content_odds_detail_list_one_list, 5),//首先进球
        LG("LG", R.layout.content_odds_detail_list_one_list, 6),//最后进球
        SINGLE("1X2", R.layout.content_odds_detail_list_one_list, 7),//独赢
        DC("DC", R.layout.content_odds_detail_list_one_list, 8),//双重机会
        OE("O/E", R.layout.content_odds_detail_list_two_sides, 9),//单/双
        SCO("SCO", R.layout.content_odds_detail_list_one_list, 10),//进球球员
        TG("TG", R.layout.content_odds_detail_list_one_list, 11),//总进球数
        TG_("TG-", R.layout.content_odds_detail_list_one_list, 12),//进球数-半場
        BTS("BTS", R.layout.content_odds_detail_list_one_list, 13),//双方球队进球
        GT1ST("GT1ST", R.layout.content_odds_detail_list_one_list, 14),//首个入球时间
        SBH("SBH", R.layout.content_odds_detail_list_one_list, 15),//双半场进球
        WBH("WBH", R.layout.content_odds_detail_list_one_list, 16),//赢得所有半场
        WEH("WEH", R.layout.content_odds_detail_list_one_list, 17),//赢得任一半场
        WM("WM", R.layout.content_odds_detail_list_one_list, 18),//净胜球数
        CLSH("CLSH", R.layout.content_odds_detail_list_one_list, 19),//零失球
        HTFT("HT/FT", R.layout.content_odds_detail_list_one_list, 20),//半场/全场
        W3("W3", R.layout.content_odds_detail_list_group_item, 21),//三项让球
        TG_OU("TG&O/U", R.layout.content_odds_detail_list_two_sides, 22),//球队进球数&大/小
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

            checkKey(type, GameType.SINGLE.value) -> return GameType.SINGLE.type

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

            type == GameType.WM.value -> return GameType.WM.type

            type == GameType.CLSH.value -> return GameType.CLSH.type

            type == GameType.HTFT.value -> return GameType.HTFT.type

            type == GameType.W3.value -> return GameType.W3.type

            checkKey(type, GameType.TG_OU.value) -> return GameType.TG_OU.type

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
            GameType.SINGLE.type -> layout = GameType.SINGLE.layout
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
                return
            }
        }

        oldOddList.forEach {  oldOddData ->
            newOddList.forEach { newOddData ->
                if (oldOddData.id == newOddData.id) {
                    oldOddData.name = newOddData.name
                    oldOddData.extInfo = newOddData.extInfo
                    oldOddData.spread = newOddData.spread
                    oldOddData.odds = newOddData.odds
                    oldOddData.status = newOddData.status
                    oldOddData.producerId = newOddData.producerId
                    oldOddData.oddState = getOddState(oldOddData, newOddData)
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
                itemView.visibility = View.GONE
                param.height = 0
                param.width = 0
            }
            itemView.layoutParams = param
        }

        private val tvName = itemView.findViewById<TextView>(R.id.tv_name)
        private val llItem = itemView.findViewById<LinearLayout>(R.id.ll_item)
        private val ivArrowUp = itemView.findViewById<ImageView>(R.id.iv_arrow_up)

        fun bindModel(oddsDetail: OddsDetailListData, position: Int) {

            tvName.text = oddsDetail.name
            llItem.setOnClickListener {
                oddsDetail.isExpand = !oddsDetail.isExpand
                notifyItemChanged(position)
            }

            if (oddsDetail.isExpand) {
                ivArrowUp.animate().rotation(180f).setDuration(200).start()
            } else {
                ivArrowUp.animate().rotation(0f).setDuration(200).start()
            }


            when (viewType) {
                GameType.HDP.type -> groupItem(oddsDetail, 2)
                GameType.W3.type -> groupItem(oddsDetail, 3)

                GameType.OU.type,
                GameType.OU_1ST.type,
                GameType.OU_2ST.type,
                GameType.OE.type,
                GameType.TG_OU.type -> twoSides(oddsDetail)

                GameType.CS.type -> cs(oddsDetail)

                GameType.FG.type,
                GameType.LG.type,
                GameType.SINGLE.type,
                GameType.DC.type,
                GameType.SCO.type,
                GameType.TG.type,
                GameType.TG_.type,
                GameType.BTS.type,
                GameType.GT1ST.type,
                GameType.SBH.type,
                GameType.WBH.type,
                GameType.WEH.type,
                GameType.WM.type,
                GameType.CLSH.type,
                GameType.HTFT.type -> oneList(oddsDetail)
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
                    // 目前api會回傳空陣列 無法比對
                    // https://sports.cxct.org/api/front/playcate/type/list
                }

            }
        }

        private fun oneList(oddsDetail: OddsDetailListData) {
            val rvBet = itemView.findViewById<RecyclerView>(R.id.rv_bet)
            rvBet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
            rvBet.apply {
                adapter = TypeOneListAdapter(oddsDetail.oddArrayList, onOddClickListener, betInfoList, curMatchId)
                layoutManager = LinearLayoutManager(itemView.context)

            }
        }

        private fun cs(oddsDetail: OddsDetailListData) {
            itemView.findViewById<LinearLayout>(R.id.ll_content).visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE

            val homeList = ArrayList<Odd>()
            val drawList = ArrayList<Odd>()
            val awayList = ArrayList<Odd>()

            for (odd in oddsDetail.oddArrayList) {
                odd.name?.let { name ->
                    if (name.contains(" - ")) {
                        val stringArray: List<String> = name.split(" - ")?: listOf()
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

                        val select = betInfoList.any { it.matchOdd.oddsId == odd.id }
                        odd.isSelect = select
                        val tvOdds = itemView.findViewById<TextView>(R.id.tv_odds)
                        tvOdds.text = TextUtil.formatForOdd(odd.odds)
                        tvOdds.isSelected = odd.isSelect

                        tvOdds.setOnClickListener {
                            if (!odd.isSelect) {
                                if (curMatchId != null && betInfoList.any { it.matchOdd.matchId == curMatchId }) {
                                    return@setOnClickListener
                                }
                                onOddClickListener.getBetInfoList(odd)
                            } else {
                                onOddClickListener.removeBetInfoItem(odd)
                            }
                        }
                    }

                }
            }

            itemView.findViewById<RecyclerView>(R.id.rv_home).apply {
                adapter = TypeCSAdapter(homeList, onOddClickListener, betInfoList, curMatchId)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            itemView.findViewById<RecyclerView>(R.id.rv_draw).apply {
                adapter = TypeCSAdapter(drawList, onOddClickListener, betInfoList, curMatchId)
                layoutManager = LinearLayoutManager(itemView.context)
            }

            itemView.findViewById<RecyclerView>(R.id.rv_away).apply {
                adapter = TypeCSAdapter(awayList, onOddClickListener, betInfoList, curMatchId)
                layoutManager = LinearLayoutManager(itemView.context)
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
            }
        }

    }


}