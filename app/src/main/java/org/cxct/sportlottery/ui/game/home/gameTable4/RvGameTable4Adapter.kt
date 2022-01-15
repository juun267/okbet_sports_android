package org.cxct.sportlottery.ui.game.home.gameTable4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.home_game_table_4.view.*
import kotlinx.android.synthetic.main.home_sport_table_4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.match.MatchPreloadData
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.game.home.OnClickFavoriteListener
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.game.home.OnClickStatisticsListener
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.GameConfigManager.getGameIcon
import org.cxct.sportlottery.util.GameConfigManager.getTitleBarBackground
import org.cxct.sportlottery.util.MatchOddUtil.updateDiscount
import org.cxct.sportlottery.util.RecyclerViewGridDecoration

class RvGameTable4Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mDataList = mutableListOf<GameEntity>()
    private var mMatchType: MatchType = MatchType.IN_PLAY
    var isLogin: Boolean? = false

    enum class ItemType {
        ODD_DATA, SPORT_GRID_REPAY
    }

    var discount: Float = 1.0F
        set(newDiscount) {
            mDataList.forEach { gameEntity ->
                gameEntity.matchOdds.forEach { matchOdd ->
                    matchOdd.oddsMap.forEach { (key, value) ->
                        value?.forEach { odd ->
                            odd?.updateDiscount(field, newDiscount)
                        }
                    }
                }
            }
            notifyDataSetChanged()
            field = newDiscount
        }

    fun setData(matchPreloadData: MatchPreloadData?, matchType: MatchType) {
        var otherMatchList: MutableList<OtherMatch> = mutableListOf()
        var otherMatch: OtherMatch
        val dataList: MutableList<GameEntity> = mutableListOf()
        matchPreloadData?.datas?.forEach { data ->
            if (data.matchOdds.isNotEmpty()) {
                data.matchOdds.forEach {
                    it.matchInfo?.gameType = data.code
                }
                var gameEntity = GameEntity(data.code, data.name, data.num, data.matchOdds, data.playCateNameMap)
                dataList.add(gameEntity)
            } else {
                otherMatch = OtherMatch(data.code, data.name, data.num)
                otherMatchList.add(otherMatch)
            }
        }
        if(!otherMatchList.isNullOrEmpty()){
            var otherGameEntity = GameEntity(null, null, 0, emptyList(), mutableMapOf(), otherMatchList)
            dataList.add(otherGameEntity)
        }
        mDataList = dataList
        mMatchType = matchType
        stopAllTimer()
        notifyDataSetChanged()
    }

    fun getData() = mDataList

    //指定刷新內部 ViewPager 的 subItem
    fun notifySubItemChanged(index: Int, indexMatchOdd: Int) {
        if (index >= 0 && indexMatchOdd >= 0)
            mDataList[index].vpTableAdapter?.notifyItemChanged(indexMatchOdd)
    }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                stopAllTimer()
                notifyDataSetChanged()
            }
        }

    var onClickOddListener: OnClickOddListener? = null

    var onClickMatchListener: OnSelectItemListener<MatchOdd>? = null

    var onClickTotalMatchListener: OnSelectItemListener<GameEntity>? = null

    var onClickSportListener: OnSelectItemListener<OtherMatch>? = null

    var onClickFavoriteListener: OnClickFavoriteListener? = null

    var onClickStatisticsListener: OnClickStatisticsListener? = null

    override fun getItemViewType(position: Int): Int {
        return when (mDataList[position].otherMatch.isNullOrEmpty()) {
            true -> ItemType.ODD_DATA.ordinal
            false -> ItemType.SPORT_GRID_REPAY.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ItemType.ODD_DATA.ordinal -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_game_table_4, parent, false)
                return ItemViewHolder(layout)
            }
            ItemType.SPORT_GRID_REPAY.ordinal -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_sport_table_4, parent, false)
                return SportGridViewHolder(layout)
            }
            else -> {
                val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_game_table_4, parent, false)
                return ItemViewHolder(layout)
            }
        }
    }

//    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
//        val data = mDataList[position]
//        holder.bind(data, oddsType)
//    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = mDataList[position]
        when (holder) {
            is ItemViewHolder -> {
                holder.apply {
                    bind(data,oddsType)
                }
            }
            is SportGridViewHolder ->{
                holder.apply {
                    bind(data,oddsType)
                }
            }
        }
    }

    override fun getItemCount(): Int = mDataList.size

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is ItemViewHolder -> {
                //當 viewHolder 被回收就 stopTimer
                holder.itemView.apply {
                    val adapter = view_pager.adapter
                    if (adapter is Vp2GameTable4Adapter)
                        adapter.stopAllTimer()
                }
            }
        }

    }

//    override fun onViewRecycled(holder: ItemViewHolder) {
//        super.onViewRecycled(holder)
//        //當 viewHolder 被回收就 stopTimer
//        holder.itemView.apply {
//            val adapter = view_pager.adapter
//            if (adapter is Vp2GameTable4Adapter)
//                adapter.stopAllTimer()
//        }
//    }

    fun stopAllTimer() {
        mDataList.forEach {
            it.vpTableAdapter?.stopAllTimer()
            it.vpTableAdapter = null
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(data: GameEntity, oddsType: OddsType) {
            itemView.apply {
                tv_game_name.text = data.name
                tv_game_num.text = data.num.toString()
                getGameIcon(data.code)?.let {
                    iv_game_icon.setImageResource(it)
                }
                getTitleBarBackground(data.code)?.let {
                    titleBar.setBackgroundResource(it)
                }
                titleBar.setOnClickListener {
                    onClickTotalMatchListener?.onClick(data)
                }

                if (data.vpTableAdapter == null)
                    data.vpTableAdapter =
                        Vp2GameTable4Adapter(data.matchOdds!!, oddsType, mMatchType, data.playCateNameMap)

                data.vpTableAdapter?.onClickMatchListener = onClickMatchListener
                data.vpTableAdapter?.onClickOddListener = onClickOddListener
                data.vpTableAdapter?.onClickFavoriteListener = onClickFavoriteListener
                data.vpTableAdapter?.onClickStatisticsListener = onClickStatisticsListener
                data.vpTableAdapter?.isLogin = isLogin

                view_pager.adapter = data.vpTableAdapter
                indicator_view.setupWithViewPager2(view_pager)
                indicator_view.apply {
                    visibility = if (data.matchOdds!!.size <= 1) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }

                view_pager.getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER //移除漣漪效果
                OverScrollDecoratorHelper.setUpOverScroll(
                    view_pager.getChildAt(0) as RecyclerView,
                    OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL
                )
            }
        }
    }

    inner class SportGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var adapter: CommonAdapter<OtherMatch>

        fun bind(data: GameEntity, oddsType: OddsType) {
            itemView.apply {
                rvSport.layoutManager =
                    LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
                rvSport.apply {
                    layoutManager = GridLayoutManager(context, 2)
                }
                rvSport.addItemDecoration(
                    RecyclerViewGridDecoration(
                        2,
                        resources.getDimension(R.dimen.margin_8).toInt(),
                        resources.getDimension(R.dimen.margin_8).toInt(),
                        resources.getDimension(R.dimen.margin_8).toInt()
                    )
                )
                rvSport.isNestedScrollingEnabled = false
                adapter = object : CommonAdapter<OtherMatch>(
                    context,
                    R.layout.item_home_sport,
                    data.otherMatch
                ) {
                    override fun convert(
                        holder: ViewHolder,
                        t: OtherMatch,
                        position: Int
                    ) {
                        getGameIcon(t.code)?.let {
                            holder.getView<ImageView>(R.id.ivSportLogo).setImageResource(it)
                        }
                        holder.setText(R.id.tvSport, t.name)
                        holder.setText(R.id.tvSportCount, t.num.toString())
                        holder.getView<LinearLayout>(R.id.layoutSport).setOnClickListener {
                            onClickSportListener?.onClick(t)
                        }
                    }
                }
                rvSport.adapter = adapter
            }
        }
    }


}