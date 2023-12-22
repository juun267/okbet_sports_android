package org.cxct.sportlottery.ui.sport.detail.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.match_odds_change.Odds
import org.cxct.sportlottery.ui.sport.detail.OddsDetailListData
import org.cxct.sportlottery.ui.sport.detail.OnOddClickListener
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.setTeamLogo


/**
 * @author Kevin
 * @create 2021/6/29
 * @description
 */
class Type6GroupAdapter(
    private var oddsDetail: OddsDetailListData,
    private val onOddClickListener: OnOddClickListener,
    private val oddsType: OddsType
) : RecyclerView.Adapter<Type6GroupAdapter.ViewHolder>() {


    var leftName: String? = null


    var centerName: String? = null


    var rightName: String? = null

    private val groupList by lazy {
       val newList =oddsDetail.oddArrayList.filterNotNull().groupBy { it?.marketSort }.values.toList()
        if (newList.size==1&&newList.first().size>6){ //例如双重机会那些玩法， marketSort都是一样的，导致数组size=1
            var newList2 = mutableListOf<MutableList<Odd>>()
            newList.first().groupBy { it.rowSort }.values.forEachIndexed { index, odds ->
                odds.forEachIndexed { index, odd ->
                    val childList = newList2.getOrNull(index)?: mutableListOf<Odd>().apply { newList2.add(this) }
                    childList.add(odd)
                }
            }
            newList2
        }else{
            newList
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_6_group_item, parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindModel(groupList[position])


    override fun getItemCount(): Int = groupList.size


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bindModel(oddsList: List<Odd?>) {
            if (oddsDetail.gameType== PlayCate.DC_OU.value) {
                LogUtil.toJson(oddsList.map { it?.name + ","+ it?.spread + "," + it?.odds + "," + it?.marketSort + "," + it?.rowSort })
            }
            if (!leftName.isNullOrEmpty()){
                itemView.findViewById<TextView>(R.id.tv_home_name).text = leftName
                itemView.findViewById<TextView>(R.id.tv_draw).text = centerName
                itemView.findViewById<TextView>(R.id.tv_away_name).text = rightName
            }
            //順序 前兩項左列 中間兩項中列 後兩項右列
            val homeList: MutableList<Odd?> = mutableListOf()
            val drawList: MutableList<Odd?> = mutableListOf()
            val awayList: MutableList<Odd?> = mutableListOf()

            homeList.apply {
                add(oddsList.firstOrNull { it?.rowSort==1 })
                add(oddsList.firstOrNull { it?.rowSort==2 })
            }

            drawList.apply {
                add(oddsList.firstOrNull { it?.rowSort==3 })
                add(oddsList.firstOrNull { it?.rowSort==4 })
            }

            awayList.apply {
                add(oddsList.firstOrNull { it?.rowSort==5 })
                add(oddsList.firstOrNull { it?.rowSort==6 })
            }

            setupRecyclerView(itemView.findViewById(R.id.rv_home), homeList)
            setupRecyclerView(itemView.findViewById(R.id.rv_draw), drawList)
            setupRecyclerView(itemView.findViewById(R.id.rv_away), awayList)

        }

        private fun setupRecyclerView(rv: RecyclerView?, list: MutableList<Odd?>){
            rv?.apply {
                adapter = TypeCSAdapter(oddsDetail, list, onOddClickListener, oddsType)
                layoutManager = LinearLayoutManager(itemView.context)
            }
        }

    }


}