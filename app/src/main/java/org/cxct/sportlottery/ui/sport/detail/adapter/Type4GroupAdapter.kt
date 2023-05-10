package org.cxct.sportlottery.ui.sport.detail.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.detail.OddsDetailListData
import org.cxct.sportlottery.ui.sport.detail.OnOddClickListener
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


    private val groupList = oddsDetail.oddArrayList.chunked(4)


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

            itemView.findViewById<TextView>(R.id.tv_left_name).text =
                leftName?.plus(if (isShowSpreadWithName) key else "")
            itemView.findViewById<TextView>(R.id.tv_right_name).text =
                rightName?.plus(if (isShowSpreadWithName) key else "")
            itemView.findViewById<ImageView>(R.id.iv_home_logo)
                .setTeamLogo(oddsDetail.matchInfo?.homeIcon)
            itemView.findViewById<ImageView>(R.id.iv_away_logo)
                .setTeamLogo(oddsDetail.matchInfo?.awayIcon)
            itemView.findViewById<RecyclerView>(R.id.rv_bet)?.apply {
                visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
                adapter = TypeCSAdapter(oddsDetail, oddsList, onOddClickListener, oddsType)
                layoutManager = GridLayoutManager(itemView.context, 2)
            }

        }

    }
}


