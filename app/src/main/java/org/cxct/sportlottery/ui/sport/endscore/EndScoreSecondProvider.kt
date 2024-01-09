package org.cxct.sportlottery.ui.sport.endscore

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.sport.list.adapter.SportMatchEvent
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.getEndScoreNameByTab
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter

class EndScoreSecondProvider(val adapter: EndScoreAdapter,
                             val onItemClick:(Int, View, BaseNode) -> Unit,
                             override val itemViewType: Int = 2,
                             override val layoutId: Int = R.layout.item_endscore_battle): BaseNodeProvider() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val vh = super.onCreateViewHolder(parent, viewType)
        val tabLayout = vh.getView<TabLayout>(R.id.tabLayout)
        OverScrollDecoratorHelper.setUpOverScroll(tabLayout)
        tabLayout.addOnTabSelectedListener(TabSelectedAdapter { tab, reselected ->

            val pair = tab.tag as Pair<String, MatchOdd>
            if (!pair.second.isExpanded) {
                return@TabSelectedAdapter
            }
            pair.second.selectPlayCode = pair.first
            pair.second.oddIdsMap?.get(pair.second.selectPlayCode)?.let {
                if (it != pair.second.selectPlayOdds) {
                    pair.second.selectPlayOdds = it
                    post { adapter.nodeReplaceChildData(pair.second, it.values) }
                }
            }
        })

        return vh
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) = helper.run {
        if (payloads.isEmpty()) {
            return@run
        }

        val matchOdd = item as MatchOdd
        val matchInfo = matchOdd.matchInfo
        payloads.forEach {
            if (it is SportMatchEvent.FavoriteChanged) {
                getView<View>(R.id.league_odd_match_favorite).isSelected = matchInfo?.isFavorite ?: false
            } else if (it is SportMatchEvent.OddsChanged) {
                val tvExpand = getView<TextView>(R.id.tvExpand)
                val linExpand = getView<View>(R.id.linExpand)
                val tabLayout = getView<TabLayout>(R.id.tabLayout)
                resetStyle(linExpand, tvExpand, tabLayout, item)
                rebindTab(getView(R.id.tabLayout), matchOdd, true)
            }
        }
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode): Unit = helper.run {
        helper.itemView.tag = item
        val matchOdd = item as MatchOdd
        val matchInfo = matchOdd.matchInfo
        setText(R.id.tvHomeName, matchInfo?.homeName)
        setText(R.id.tvAwayName, matchInfo?.awayName)
        setText(R.id.league_odd_match_time, TimeUtil.timeFormat(matchInfo?.startTime, TimeUtil.DM_HM_FORMAT))
        getView<ImageView>(R.id.ivHomeLogo).setTeamLogo(matchInfo?.homeIcon)
        getView<ImageView>(R.id.ivAwayLogo).setTeamLogo(matchInfo?.awayIcon)
        getView<View>(R.id.lin_match).setOnClickListener {
            onItemClick.invoke(helper.bindingAdapterPosition, it, item)
        }

        getView<View>(R.id.league_odd_match_favorite).run {
            isSelected = matchInfo?.isFavorite ?: false
            setOnClickListener { onItemClick.invoke(helper.bindingAdapterPosition, this, item) }
        }
        val tvExpand = getView<TextView>(R.id.tvExpand)
        val linExpand = getView<View>(R.id.linExpand)
        val tabLayout = getView<TabLayout>(R.id.tabLayout)
        resetStyle(linExpand, tvExpand, tabLayout, item)
        rebindTab(tabLayout, matchOdd)
        linExpand.setOnClickListener {
            adapter.expandOrCollapse(item, parentPayload = item)
            resetStyle(linExpand, tvExpand, tabLayout, item)
            rebindTab(tabLayout, matchOdd, true)
        }

    }

    private val expandedDrawable by lazy {
        DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_025BE8)
    }

    private val collapseDrawable by lazy {
        DrawableCreatorUtils.getCommonBackgroundStyle(8, android.R.color.white, R.color.color_025BE8, 1)
    }
    private val disableDrawable by lazy {
        DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_cccccc)
    }

    private fun resetStyle(linExpand: View, tvExpand: TextView,tabLayout: TabLayout, matchOdd: MatchOdd) = tvExpand.run {
        when{
            matchOdd.oddIdsMap.isNullOrEmpty()->{
                linExpand.isEnabled = false
                setText(R.string.N698)
                setTextColor(ContextCompat.getColor(context, R.color.color_FFFFFF))
                linExpand.background = disableDrawable
                (linExpand.layoutParams as MarginLayoutParams).bottomMargin = 10.dp
                setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
            matchOdd.isExpanded->{
                linExpand.isEnabled = true
                setText(R.string.D039)
                setTextColor(ContextCompat.getColor(context, R.color.color_025BE8))
                linExpand.background = collapseDrawable
                (linExpand.layoutParams as MarginLayoutParams).bottomMargin = 10.dp
                setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_to_up_blue, 0)
            }
            else->{
                linExpand.isEnabled = true
                setText(R.string.N698)
                setTextColor(Color.WHITE)
                linExpand.background = expandedDrawable
                (linExpand.layoutParams as MarginLayoutParams).bottomMargin = 20.dp
                setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_to_down_white, 0)
            }
        }

    }

    private fun rebindTab(tablayout: TabLayout, matchOdd: MatchOdd, update: Boolean = false) = tablayout.run {
        isVisible = matchOdd.isExpanded
        if (tabCount > 0){
            if (update && tabCount == matchOdd.oddIdsMap.size) {
                repeat(tabCount) {
                    val tab = getTabAt(it)!!
                    val pair = tab.tag as Pair<String, MatchOdd>
                    if (pair.first == matchOdd.selectPlayCode) {
                        tab.select()
                        return@run
                    }
                }
            }

            removeAllTabs()
        }

        matchOdd.oddIdsMap?.keys.forEach {
            addTab(newTab().setTag(Pair(it, matchOdd)).setText(it.getEndScoreNameByTab(context)))
        }
        if (tabCount == 0) {
            matchOdd.selectPlayCode = PlayCate.FS_LD_CS.value
            matchOdd.selectPlayOdds = null
        }
    }
}