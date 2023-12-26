package org.cxct.sportlottery.ui.sport.detail.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.detail.OddsDetailListData
import org.cxct.sportlottery.ui.sport.detail.OnOddClickListener
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.setTeamLogo


/**
 * @author Kevin
 * @create 2021/6/30
 * @description
 */
class Type4GroupAdapter(
    private var oddsDetail: OddsDetailListData,
    private val onOddClickListener: OnOddClickListener,
    private val oddsType: OddsType
) : RecyclerView.Adapter<Type4GroupAdapter.ViewHolder>() {


    var leftName: String? = null


    var rightName: String? = null


    private val keys = oddsDetail.oddArrayList
        .groupBy { it?.spread }
        .filter { it.key != null }
        .mapTo(mutableListOf()) { it.key }

    private val groupList by lazy {
        val newList =oddsDetail.oddArrayList.filterNotNull().groupBy { it?.marketSort }.values.toList()
        if (newList.size==1&&newList.first().size>4){ //例如双重机会那些玩法， marketSort都是一样的，导致数组size=1
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

    var isShowSpreadWithName = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_4_group_item, parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (groupList.isNotEmpty() && keys.isNotEmpty()) {
            try {
                keys[position]?.let { holder.bindModel(groupList[position], it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun getItemCount(): Int = groupList.size


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bindModel(oddsList: List<Odd?>, key: String) {
//            if (oddsDetail.gameType== PlayCate.OU_BTS.value) {
//                LogUtil.toJson(oddsList.map { it?.name + ","+ it?.spread + "," + it?.odds + "," + it?.marketSort + "," + it?.rowSort })
//            }
            itemView.findViewById<TextView>(R.id.tv_left_name).text =
                leftName?.plus(if (isShowSpreadWithName) key else "")
            itemView.findViewById<TextView>(R.id.tv_right_name).text =
                rightName?.plus(if (isShowSpreadWithName) key else "")
            itemView.findViewById<RecyclerView>(R.id.rv_bet)?.apply {
                visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
                adapter = TypeCSAdapter(oddsDetail, oddsList, onOddClickListener, oddsType)
                layoutManager = GridLayoutManager(itemView.context, 2)
            }

        }

    }
}


