package org.cxct.sportlottery.ui2.maintab.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.third_game.third_games.hot.HotMatchLiveData

import org.cxct.sportlottery.util.setTeamLogo

class HotLiveAdapter(private var clickListener: ItemClickListener) :
    RecyclerView.Adapter<HotLiveAdapter.ItemViewHolder>() {
    open var mSelectedId: String? = null
    var data: List<HotMatchLiveData> = mutableListOf()
        set(value) {
            field = value

            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
        holder.apply {

            group.isSelected =  mSelectedId == item.matchInfo.id
            arrow.visibility = if (mSelectedId == item.matchInfo.id) View.VISIBLE else View.INVISIBLE
            group.setOnClickListener {
                mSelectedId = item.matchInfo.id
                notifyDataSetChanged()
                clickListener.onClick(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
    class ItemViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val arrow: ImageView = itemView.findViewById(R.id.iv_triangle_left)
        val group: LinearLayout = itemView.findViewById(R.id.ll_root)
        private val homeTeamIcon: ImageView = itemView.findViewById(R.id.iv_team_logo1)
        private val awayTeamIcon: ImageView = itemView.findViewById(R.id.iv_team_logo2)
        val homeTeamName: TextView = itemView.findViewById(R.id.tv_team_name1)
        val awayTeamName: TextView = itemView.findViewById(R.id.tv_team_name2)
        private val homeTeamNum: TextView = itemView.findViewById(R.id.tv_team_score1)
        private val awayTeamNum: TextView = itemView.findViewById(R.id.tv_team_score2)
        fun bind(item: HotMatchLiveData) {
            homeTeamIcon.setTeamLogo(item.matchInfo.homeIcon)
            awayTeamIcon.setTeamLogo(item.matchInfo.awayIcon)
            homeTeamName.text = item.matchInfo.homeName
            awayTeamName.text = item.matchInfo.awayName
            homeTeamNum.text = item.matchInfo.homeScore?:"0"
            awayTeamNum.text = item.matchInfo.awayScore?:"0"
        }
        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_hot_live, parent, false)
                return ItemViewHolder(view)
            }
        }

    }
    class ItemClickListener(private var clickListener: (data: HotMatchLiveData)->Unit){
        fun onClick(data: HotMatchLiveData) = clickListener(data)
    }
}