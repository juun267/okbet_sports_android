package org.cxct.sportlottery.ui.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.DisplayUtil.dp

class OddsDetailListAdapter(private val oddsDetailListData: ArrayList<OddsDetailListData>) :
    RecyclerView.Adapter<OddsDetailListAdapter.ViewHolder>() {

    //需要再確認代號
    enum class GameType(val value: String, val layout: Int, val type: Int) {
        HDP("HDP", R.layout.content_odds_detail_list_hdp, 0),//让球
        TG("TG", R.layout.content_odds_detail_list_hdp, 1),//进球
        CS("CS", R.layout.content_odds_detail_list_hdp, 2),//波胆
    }

    override fun getItemViewType(position: Int): Int {

        if (oddsDetailListData[position].gameType == GameType.HDP.value || oddsDetailListData[position].gameType.contains(GameType.HDP.value)) {
            return GameType.HDP.type
        } else {
            return -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        var layout: Int = GameType.HDP.layout

        when (viewType) {
            GameType.HDP.type -> layout = GameType.HDP.layout
            GameType.HDP.type -> layout = GameType.HDP.layout
            GameType.HDP.type -> layout = GameType.HDP.layout
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

    private lateinit var code: String

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
                    val rvGame = itemView.findViewById<RecyclerView>(R.id.rv_game)
                    rvGame.visibility = if (oddsDetail.isExpand) View.VISIBLE else View.GONE;
                    rvGame.apply {
                        adapter = TypeHDPAdapter(oddsDetail.oddArrayList)
                        layoutManager = LinearLayoutManager(itemView.context)
                    }
                }
            }
            for (element in oddsDetail.typeCodes) {
                if(element == code){
                    setVisibility(true)
                    break
                }else{
                    setVisibility(false)
                }
            }
        }
    }


}