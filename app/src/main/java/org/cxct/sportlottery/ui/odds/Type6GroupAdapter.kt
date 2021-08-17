package org.cxct.sportlottery.ui.odds


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd
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
    private val oddsType: OddsType
) : RecyclerView.Adapter<Type6GroupAdapter.ViewHolder>() {


    var leftName: String? = null


    var centerName: String? = null


    var rightName: String? = null


    private val groupList = oddsDetail.oddArrayList.chunked(6)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_6_group_item, parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bindModel(groupList[position])


    override fun getItemCount(): Int = groupList.size


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bindModel(oddsList: List<Odd?>) {

            itemView.findViewById<TextView>(R.id.tv_left_name).text = leftName
            itemView.findViewById<TextView>(R.id.tv_center_name).text = centerName
            itemView.findViewById<TextView>(R.id.tv_right_name).text = rightName

            //順序 前兩項左列 中間兩項中列 後兩項右列
            val homeList: MutableList<Odd?> = mutableListOf()
            val drawList: MutableList<Odd?> = mutableListOf()
            val awayList: MutableList<Odd?> = mutableListOf()

            homeList.apply {
                add(oddsList.firstOrNull())
                add(oddsList.getOrNull(1))
            }

            drawList.apply {
                add(oddsList.getOrNull(2))
                add(oddsList.getOrNull(3))
            }

            awayList.apply {
                add(oddsList.getOrNull(4))
                add(oddsList.getOrNull(5))
            }

            setupRecyclerView(itemView.findViewById(R.id.rv_home), homeList)
            setupRecyclerView(itemView.findViewById(R.id.rv_draw), drawList)
            setupRecyclerView(itemView.findViewById(R.id.rv_away), awayList)

        }

        private fun setupRecyclerView(rv: RecyclerView?, list: MutableList<Odd?>){
            rv?.apply {
                adapter = TypeCSAdapter(oddsDetail, list, onOddClickListener, oddsType)
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