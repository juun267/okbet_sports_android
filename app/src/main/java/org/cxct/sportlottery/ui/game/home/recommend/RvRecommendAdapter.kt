package org.cxct.sportlottery.ui.game.home.recommend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.home_recommend_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.matchCategory.result.MatchRecommendResult
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TimeUtil

class RvRecommendAdapter : RecyclerView.Adapter<RvRecommendAdapter.ItemViewHolder>() {

    private val mRequestOptions = RequestOptions()
        .placeholder(R.drawable.ic_image_load)
        .error(R.drawable.ic_image_broken)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    private var mDataList = listOf<RecommendGameEntity>()

    fun setData(result: MatchRecommendResult) {
        val dataList = mutableListOf<RecommendGameEntity>()
        result.rows?.forEach { row ->
            row.leagueOdds?.matchOdds?.forEach { oddData ->
                val beans = oddData.odds?.map { OddBean(it.key, it.value) }?: listOf()
                val entity = RecommendGameEntity(
                    code = row.sport?.code,
                    name = row.sport?.name,
                    matchInfo = oddData.matchInfo,
                    oddBeans = beans
                )
                dataList.add(entity)
            }
        }
        mDataList = dataList

        notifyDataSetChanged()
    }

    fun getData() = mDataList

    //指定刷新內部 ViewPager 的 subItem
    fun notifySubItemChanged(index: Int, indexVpAdapter: Int) {
        if (index >= 0 && indexVpAdapter >= 0)
            mDataList[index].vpRecommendAdapter?.notifyItemChanged(indexVpAdapter)
    }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    var onClickOddListener: OnClickOddListener? = null

    var onClickMatchListener: OnSelectItemListener<RecommendGameEntity>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_recommend_item, parent, false)
        return ItemViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        try {
            val data = mDataList[position]
            holder.bind(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = mDataList.size


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(data: RecommendGameEntity) {
            itemView.apply {
                block_match_info.setOnClickListener {
                    onClickMatchListener?.onClick(data)
                }

                Glide.with(context).load(data.matchInfo?.img).apply(mRequestOptions).into(iv_match_image)

                tv_game_name_home.text = data.matchInfo?.homeName
                tv_game_name_away.text = data.matchInfo?.awayName
                tv_match_time.text = TimeUtil.timeFormat(data.matchInfo?.startTime, "MM/dd\nHH:mm")


                if (data.vpRecommendAdapter == null)
                    data.vpRecommendAdapter = VpRecommendAdapter(data.code, data.oddBeans, oddsType, data.toMatchOdd())

                data.vpRecommendAdapter?.onClickOddListener = onClickOddListener

                view_pager.adapter = data.vpRecommendAdapter
                indicator_view.setupWithViewPager2(view_pager)
            }
        }
    }

}

//TODO simon test review MatchOdd 資料轉換
fun RecommendGameEntity.toMatchOdd(): MatchOdd {
    val matchInfo = MatchInfo(
        gameType = null,
        awayName = this.matchInfo?.awayName.toString(),
        endTime = this.matchInfo?.endTime.toString(),
        homeName = this.matchInfo?.homeName?: "",
        id = this.matchInfo?.id?: "",
        playCateNum = this.matchInfo?.playCateNum?: -1,
        startTime = this.matchInfo?.startTime.toString(),
        status = this.matchInfo?.status?: -1
    )
    val odds: MutableMap<String, MutableList<Odd?>> = mutableMapOf()
    this.oddBeans.forEach {
        odds[it.playTypeCode] = it.oddList.toMutableList()
    }
    return MatchOdd(matchInfo, odds)
}