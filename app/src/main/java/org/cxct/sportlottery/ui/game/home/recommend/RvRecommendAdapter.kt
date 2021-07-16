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
import org.cxct.sportlottery.network.matchCategory.result.OddData
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.game.home.OnClickOddListener
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TimeUtil

class RvRecommendAdapter : RecyclerView.Adapter<RvRecommendAdapter.ItemViewHolder>() {

    private val mRequestOptions = RequestOptions()
        .placeholder(R.drawable.ic_image_load)
        .error(R.drawable.ic_image_broken)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    private var mVpAdapterList = mutableListOf<VpRecommendAdapter?>()

    var dataList = listOf<OddData>()
        set(value) {
            if (value != field) {
                field = value
                mVpAdapterList = MutableList(value.size) { null }
                notifyDataSetChanged()
            }
        }

    //指定刷新內部 ViewPager 的 subItem
    fun notifySubItemChanged(index: Int, indexMatchOdd: Int) {
        //TODO simon test 賽事推薦 刷新
//        if (index >= 0 && indexMatchOdd >= 0)
//            dataList[indexvpTableAdapter?.notifyItemChanged(indexMatchOdd)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_recommend_item, parent, false)
        return ItemViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val data = dataList[position]
        holder.bind(data, oddsType, position)
    }

    override fun getItemCount(): Int = dataList.size


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(data: OddData, oddsType: OddsType, position: Int) {
            itemView.apply {
                val matchOdd = data.toMatchOdd()
                block_match_info.setOnClickListener {
                    onClickMatchListener?.onClick(matchOdd)
                }

                Glide.with(context).load(data.matchInfo?.img).apply(mRequestOptions).into(iv_match_image)

                tv_game_name_home.text = data.matchInfo?.homeName
                tv_game_name_away.text = data.matchInfo?.awayName
                tv_match_time.text = TimeUtil.timeFormat(data.matchInfo?.startTime, "MM/dd\nHH:mm")

                mVpAdapterList[position]
                if (mVpAdapterList[position] == null)
                    mVpAdapterList[position] = VpRecommendAdapter(data.odds?.toList()?: listOf(), oddsType, matchOdd)

                mVpAdapterList[position]?.onClickOddListener = onClickOddListener

                view_pager.adapter = mVpAdapterList[position]
                indicator_view.setupWithViewPager2(view_pager)
            }
        }
    }

}

//TODO simon test review MatchOdd 資料轉換
fun OddData.toMatchOdd(): MatchOdd {
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

    return MatchOdd(matchInfo, this.odds?: mutableMapOf())
}