package org.cxct.sportlottery.ui.maintab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.index.home.HomeLiveData
import org.cxct.sportlottery.util.LogUtil

import org.cxct.sportlottery.util.setTeamLogo

class HotLiveAdapter(private var clickListener:ItemClickListener): RecyclerView.Adapter<HotLiveAdapter.ItemViewHolder>() {
    private var mSelectedPosition = 0
    var data: List<HomeLiveData> = mutableListOf()
        set(value) {
            mSelectedPosition = 0
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
            group.isSelected =  mSelectedPosition == position
            arrow.visibility = if (mSelectedPosition == position) View.VISIBLE else View.INVISIBLE
            group.setOnClickListener {
                mSelectedPosition = position
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
        fun bind(item: HomeLiveData) {
            homeTeamIcon.setTeamLogo(item.homeTeamIcon)
            awayTeamIcon.setTeamLogo(item.awayTeamIcon)
            homeTeamName.text = item.homeTeamName
            awayTeamName.text = item.awayTeamName
            homeTeamNum.text = item.homeTeamNum
            awayTeamNum.text = item.awayTeamNum

        }
        companion object {
            fun from(parent: ViewGroup):ItemViewHolder{
                val view =  LayoutInflater.from(parent.context)
                .inflate(R.layout.item_hot_live, parent, false)
                return ItemViewHolder(view)
            }
        }

    }
    class ItemClickListener(private var clickListener: (data: HomeLiveData)->Unit){
        fun onClick(data: HomeLiveData) = clickListener(data)
    }
}