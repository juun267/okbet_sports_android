package org.cxct.sportlottery.ui.game.home

import android.content.Context
import android.os.Parcelable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.home_game_table_4.view.*
import kotlinx.android.synthetic.main.home_recommend_item.view.*
import kotlinx.android.synthetic.main.home_recommend_item.view.indicator_view
import kotlinx.android.synthetic.main.home_recommend_item.view.view_pager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.game.home.recommend.RecommendGameEntity
import org.cxct.sportlottery.ui.game.home.recommend.VpRecommendAdapter
import org.cxct.sportlottery.ui.game.home.recommend.toMatchOdd
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TimeUtil

/**
 * 賽事推薦的選單(首頁)
 */
class RecommendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val mRequestOptions = RequestOptions()
        .placeholder(R.drawable.ic_image_load)
        .error(R.drawable.ic_image_broken)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    var saveInstanceState: Int = 0

    // TODO binding
    var onClickMatchListener: OnSelectItemListener<RecommendGameEntity>? = null
    var onClickOddListener: OnClickOddListener? = null
    var onClickOutrightOddListener: OnClickOddListener? = null
    var onClickMoreListener: OnClickMoreListener? = null

    fun bind(data: RecommendGameEntity, oddsType: OddsType) {
        itemView.apply {
            block_match_info.setOnClickListener {
                onClickMatchListener?.onClick(data)
            }

            Glide.with(context).load(data.matchInfo?.img).apply(mRequestOptions).into(iv_match_image)

            tv_game_name_home.apply {
                visibility = if (data.isOutright == 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                text = data.matchInfo?.homeName
            }

            tv_game_name_away.apply {
                visibility = if (data.isOutright == 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                text = data.matchInfo?.awayName
            }

            rec_outright_game_type.apply {
                visibility = if (data.isOutright == 1) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                text = data.name
            }

            rec_outright_league.apply {
                visibility = if (data.isOutright == 1) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                text = data.leagueName
            }

            tv_match_time.apply {
                visibility = if (data.isOutright == 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                text = data.matchInfo?.getStartTime(context)
            }

            if (data.vpRecommendAdapter == null)
                data.vpRecommendAdapter = VpRecommendAdapter(
                    data.code,
                    data.oddBeans,
                    data.isOutright,
                    data.toMatchOdd(),
                    data.dynamicMarkets,
                    data.playCateNameMap,
                    data.betPlayCateNameMap
                )

            data.vpRecommendAdapter?.oddsType = oddsType
            data.vpRecommendAdapter?.onClickOddListener = onClickOddListener
            data.vpRecommendAdapter?.onClickOutrightOddListener = onClickOutrightOddListener
            data.vpRecommendAdapter?.onClickMoreListener = onClickMoreListener

            view_pager.adapter = data.vpRecommendAdapter
            view_pager.getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER //移除漣漪效果
            OverScrollDecoratorHelper.setUpOverScroll(view_pager.getChildAt(0) as RecyclerView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
            indicator_view.setupWithViewPager2(view_pager)
            indicator_view.apply {
                visibility = if (data.isOutright == 1 || data.oddBeans.size <= 1) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        }
    }
    private fun org.cxct.sportlottery.network.matchCategory.result.MatchInfo.getStartTime(context: Context): String {
        val dateFormat = "dd / MM"
        val todayDate = TimeUtil.timeFormat(System.currentTimeMillis(), dateFormat)
        return this.startTime?.let { startTimeNotNull ->
            TimeUtil.timeFormat(startTimeNotNull, "$dateFormat\nHH:mm")
                .replace(todayDate, context.getString(R.string.home_tab_today))
        } ?: ""
    }
}