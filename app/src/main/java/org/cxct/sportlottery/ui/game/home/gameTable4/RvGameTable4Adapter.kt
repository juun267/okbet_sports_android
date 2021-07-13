package org.cxct.sportlottery.ui.game.home.gameTable4

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_game_table_4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.match.MatchPreloadData
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.GameConfigManager.getGameIcon
import org.cxct.sportlottery.util.GameConfigManager.getTitleBarBackground

class RvGameTable4Adapter : RecyclerView.Adapter<RvGameTable4Adapter.ItemViewHolder>() {

    private var mDataList = listOf<GameEntity>()

    fun setData(matchPreloadData: MatchPreloadData?) {
        mDataList = matchPreloadData?.datas?.map { data ->
            data.matchOdds.forEach {
                it.matchInfo?.sportType = SportType.getSportType(data.code)
            }
            GameEntity(data.code, data.name, data.num, data.matchOdds)
        } ?: listOf()

        notifyDataSetChanged()
    }

    fun getData() = mDataList

    //指定刷新內部 ViewPager 的 subItem
    fun notifySubItemChanged(index: Int, indexMatchOdd: Int) {
        mDataList[index].vpTableAdapter?.notifyItemChanged(indexMatchOdd)
    }

    var matchType: MatchType = MatchType.IN_PLAY
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    var onClickOddListener: OnClickOddListener? = null

    var onClickMatchListener: OnSelectItemListener<MatchOdd>? = null

    var onClickTotalMatchListener: OnSelectItemListener<GameEntity>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_game_table_4, parent, false)
        return ItemViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val data = mDataList[position]
        holder.bind(data, oddsType)
    }

    override fun getItemCount(): Int = mDataList.size

    override fun onViewRecycled(holder: ItemViewHolder) {
        super.onViewRecycled(holder)
        //當 viewHolder 被回收就 stopTimer
        holder.itemView.apply {
            val adapter = view_pager.adapter
            if (adapter is Vp2GameTable4Adapter)
                adapter.stopAllTimer()
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(data: GameEntity, oddsType: OddsType) {
            itemView.apply {
                iv_game_icon.setImageResource(getGameIcon(data.code))
                tv_game_name.text = data.name
                tv_game_num.text = data.num.toString()
                titleBar.setBackgroundResource(getTitleBarBackground(data.code))
                titleBar.setOnClickListener {
                    onClickTotalMatchListener?.onClick(data)
                }

                if (data.vpTableAdapter == null) {
                    data.vpTableAdapter = Vp2GameTable4Adapter()
                    data.vpTableAdapter?.onClickMatchListener = onClickMatchListener
                    data.vpTableAdapter?.onClickOddListener = onClickOddListener
                    data.vpTableAdapter?.matchType = matchType
                    data.vpTableAdapter?.oddsType = oddsType
                    data.vpTableAdapter?.dataList = data.matchOdds
                }

                view_pager.adapter = data.vpTableAdapter
                indicator_view.setupWithViewPager2(view_pager)
            }
        }
    }

}