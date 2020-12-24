package org.cxct.sportlottery.ui.odds

import android.R.string
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.util.DisplayUtil.dp


class OddsDetailListAdapter(private val oddsDetailListData: ArrayList<OddsDetailListData>) :
    RecyclerView.Adapter<OddsDetailListAdapter.ViewHolder>() {

    private lateinit var code: String

    //需要再確認代號
    enum class GameType(val value: String, val layout: Int, val type: Int) {
        HDP("HDP", R.layout.content_odds_detail_list_hdp, 0),//让球
        TG("TG", 0, 1),//进球
        CS("CS", R.layout.content_odds_detail_list_cs, 2),//波胆
        FG("FG", R.layout.content_odds_detail_list_fg_lg, 3),//首先进球
        LG("LG", R.layout.content_odds_detail_list_fg_lg, 4),//最后进球
    }

    override fun getItemViewType(position: Int): Int {

        val type = oddsDetailListData[position].gameType

        when {
            checkKey(type, GameType.HDP.value) -> {
                return GameType.HDP.type
            }
            checkKey(type, GameType.CS.value) -> {
                return GameType.CS.type
            }
            type == GameType.FG.value -> {
                return GameType.FG.type
            }
            type == GameType.LG.value -> {
                return GameType.LG.type
            }
            else -> {
                return -1
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        var layout: Int = GameType.HDP.layout

        when (viewType) {
            GameType.HDP.type -> layout = GameType.HDP.layout
            GameType.TG.type -> layout = GameType.TG.layout
            GameType.CS.type -> layout = GameType.CS.layout
            GameType.FG.type -> layout = GameType.FG.layout
            GameType.LG.type -> layout = GameType.LG.layout
        }

        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false), viewType)
    }


    override fun getItemCount(): Int {
        return oddsDetailListData.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel(oddsDetailListData[position])
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

        private val tv_name = itemView.findViewById<TextView>(R.id.tv_name)
        private val ll_item = itemView.findViewById<LinearLayout>(R.id.ll_item)

        fun bindModel(oddsDetail: OddsDetailListData) {
            tv_name.text = oddsDetail.name
            ll_item.setOnClickListener {
                oddsDetail.isExpand = !oddsDetail.isExpand
                notifyDataSetChanged()
            }

            when (viewType) {

                GameType.HDP.type -> {
                    val rv_bet = itemView.findViewById<RecyclerView>(R.id.rv_bet)
                    rv_bet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
                    rv_bet.apply {
                        adapter = TypeHDPAdapter(oddsDetail.oddArrayList)
                        layoutManager = LinearLayoutManager(itemView.context)
                    }
                }

                GameType.CS.type -> {
                    itemView.findViewById<LinearLayout>(R.id.ll_content).visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE

                    val homeList = ArrayList<Odd>()
                    val drawList = ArrayList<Odd>()
                    val awayList = ArrayList<Odd>()

                    for (element in oddsDetail.oddArrayList) {
                        if (element.name.contains(" - ")) {
                            val stringArray: List<String> = element.name.split(" - ")
                            if (stringArray[0].toInt() > stringArray[1].toInt()) {
                                homeList.add(element)
                            }
                            if (stringArray[0].toInt() == stringArray[1].toInt()) {
                                drawList.add(element)
                            }
                            if (stringArray[0].toInt() < stringArray[1].toInt()) {
                                awayList.add(element)
                            }
                        } else {
                            val tv_odds = itemView.findViewById<TextView>(R.id.tv_odds)
                            tv_odds.text = element.odds.toString()
                            tv_odds.setOnClickListener {
                                tv_odds.isSelected = !tv_odds.isSelected
                                element.isSelect = tv_odds.isSelected

                                //TODO 添加至投注單
                            }
                        }
                    }

                    itemView.findViewById<RecyclerView>(R.id.rv_home).apply {
                        adapter = TypeCSAdapter(homeList)
                        layoutManager = LinearLayoutManager(itemView.context)
                    }

                    itemView.findViewById<RecyclerView>(R.id.rv_draw).apply {
                        adapter = TypeCSAdapter(drawList)
                        layoutManager = LinearLayoutManager(itemView.context)
                    }

                    itemView.findViewById<RecyclerView>(R.id.rv_away).apply {
                        adapter = TypeCSAdapter(awayList)
                        layoutManager = LinearLayoutManager(itemView.context)
                    }
                }

                GameType.FG.type, GameType.LG.type -> {
                    val rv_bet = itemView.findViewById<RecyclerView>(R.id.rv_bet)
                    rv_bet.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
                    rv_bet.apply {
                        adapter = TypeFGLGAdapter(oddsDetail.oddArrayList)
                        layoutManager = LinearLayoutManager(itemView.context)
                    }
                }
            }

            for (element in oddsDetail.typeCodes) {
                if (element == code) {
                    setVisibility(true)
                    break
                } else {
                    setVisibility(false)
                }
            }
        }
    }


}