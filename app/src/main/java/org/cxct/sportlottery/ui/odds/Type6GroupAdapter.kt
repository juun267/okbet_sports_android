package org.cxct.sportlottery.ui.odds


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.common.OddDetailStateViewHolder
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.CustomForOddDetailVerticalDivider


/**
 * @author Kevin
 * @create 2021/6/29
 * @description
 */
class Type6GroupAdapter(
    private var oddsDetail: OddsDetailListData,
    private val onOddClickListener: OnOddClickListener,
    private val betInfoList: MutableList<BetInfoListData>,
    private val oddsType: OddsType
) : RecyclerView.Adapter<Type6GroupAdapter.ViewHolder>() {


    var leftName: String? = null


    var centerName: String? = null


    var rightName: String? = null


    val key = mutableListOf<String>().apply {
        oddsDetail.group6Item.forEach{
            add(it.key)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_6_group_item, parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        oddsDetail.group6Item[key[position]]?.let { holder.bindModel(it) }
    }


    override fun getItemCount(): Int = oddsDetail.group6Item.size


    inner class ViewHolder(view: View) : OddDetailStateViewHolder(view) {

        fun bindModel(oddsList: List<Odd>) {

            itemView.findViewById<TextView>(R.id.tv_left_name).text = leftName
            itemView.findViewById<TextView>(R.id.tv_center_name).text = centerName
            itemView.findViewById<TextView>(R.id.tv_right_name).text = rightName

            //順序 前兩項左列 中間兩項中列 後兩項右列
            val homeList: MutableList<Odd> = mutableListOf()
            val drawList: MutableList<Odd> = mutableListOf()
            val awayList: MutableList<Odd> = mutableListOf()

            homeList.apply {
                add(oddsList[0])
                add(oddsList[1])
            }

            drawList.apply {
                add(oddsList[2])
                add(oddsList[3])
            }

            awayList.apply {
                add(oddsList[4])
                add(oddsList[5])
            }

            setupRecyclerView(itemView.findViewById(R.id.rv_home), homeList)
            setupRecyclerView(itemView.findViewById(R.id.rv_draw), drawList)
            setupRecyclerView(itemView.findViewById(R.id.rv_away), awayList)

        }

        private fun setupRecyclerView(rv: RecyclerView?, list: MutableList<Odd>){
            rv?.apply {
                adapter = TypeCSAdapter(oddsDetail, list, onOddClickListener, betInfoList, oddsType)
                layoutManager = LinearLayoutManager(itemView.context)
                addItemDecoration(
                    CustomForOddDetailVerticalDivider(
                        context,
                        R.dimen.recyclerview_item_dec_spec_odds_detail_odds
                    )
                )
            }
        }

    }


}