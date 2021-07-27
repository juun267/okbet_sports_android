package org.cxct.sportlottery.ui.odds


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.common.OddDetailStateViewHolder
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.GridItemDecoration
import java.util.*


/**
 * @author Kevin
 * @create 2021/6/30
 * @description
 */
class Type4GroupAdapter(
    private var oddsDetail: OddsDetailListData,
    private val onOddClickListener: OnOddClickListener,
    private val betInfoList: MutableList<BetInfoListData>,
    private val oddsType: OddsType
) : RecyclerView.Adapter<Type4GroupAdapter.ViewHolder>() {


    var leftName: String? = null


    var rightName: String? = null


    private val keys = mutableListOf<String>().apply {
        oddsDetail.groupItem.forEach {
            add(it.key)
        }
    }


    var isShowSpreadWithName = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_4_group_item, parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        oddsDetail.groupItem[keys[position]]?.let { holder.bindModel(it, keys[position]) }
    }


    override fun getItemCount(): Int = oddsDetail.groupItem.size


    inner class ViewHolder(view: View) : OddDetailStateViewHolder(view) {

        fun bindModel(oddsList: List<Odd?>, key: String) {

            itemView.findViewById<TextView>(R.id.tv_left_name).text = leftName?.plus(if (isShowSpreadWithName) key else "")
            itemView.findViewById<TextView>(R.id.tv_right_name).text = rightName?.plus(if (isShowSpreadWithName) key else "")

            itemView.findViewById<RecyclerView>(R.id.rv_bet)?.apply {
                visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE
                adapter = TypeCSAdapter(oddsDetail, oddsList, onOddClickListener, betInfoList, oddsType)
                layoutManager = GridLayoutManager(itemView.context, 2)
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