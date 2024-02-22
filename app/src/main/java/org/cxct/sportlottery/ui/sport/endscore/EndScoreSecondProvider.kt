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
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.sport.list.adapter.SportMatchEvent
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.getEndScoreNameByTab
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter
import timber.log.Timber

class EndScoreSecondProvider(val adapter: EndScoreAdapter,
                             val onItemClick:(Int, View, BaseNode) -> Unit,
                             override val itemViewType: Int = 2,
                             override val layoutId: Int = R.layout.item_endscore_battle): BaseNodeProvider() {

    private val tabSelectedAdapter = TabSelectedAdapter { tab, reselected ->
        val playCate = tab.tag.toString()
        val matchOdd = tab.parent?.tag as MatchOdd
        if (!matchOdd.isExpanded) {
            return@TabSelectedAdapter
        }
        matchOdd.selectPlayCode = playCate
        adapter.findVisiableRangeMatchOdd(matchOdd.matchInfo?.id?:"")?.apply {
            selectPlayCode = playCate
            isExpanded = matchOdd.isExpanded
        }
        matchOdd.oddIdsMap?.get(matchOdd.selectPlayCode)?.let {
            if (it != matchOdd.selectPlayOdds) {
                matchOdd.selectPlayOdds = it
                post {
                    val newChildNodes = mutableListOf<BaseNode>().apply {
                        addAll(it.values)
                        add(ViewAllNode(parentNode = matchOdd))
                    }
                    adapter.nodeReplaceChildData(matchOdd, newChildNodes)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val vh = super.onCreateViewHolder(parent, viewType)
        val tabLayout = vh.getView<TabLayout>(R.id.tabLayout)
        OverScrollDecoratorHelper.setUpOverScroll(tabLayout)
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
                resetStyle(helper, item)
                rebindTab(getView(R.id.tabLayout), matchOdd)
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
        getView<ImageView>(R.id.ivHomeLogo).setTeamLogo(matchInfo?.homeIcon,R.drawable.ic_team_default_no_stroke)
        getView<ImageView>(R.id.ivAwayLogo).setTeamLogo(matchInfo?.awayIcon,R.drawable.ic_team_default_no_stroke)
        getView<View>(R.id.lin_match).setOnClickListener {
            onItemClick.invoke(helper.bindingAdapterPosition, it, item)
        }

        getView<View>(R.id.league_odd_match_favorite).run {
            isSelected = matchInfo?.isFavorite ?: false
            setOnClickListener { onItemClick.invoke(helper.bindingAdapterPosition, this, item) }
        }
        resetStyle(helper, item)
        rebindTab(getView(R.id.tabLayout), matchOdd)
        getView<View>(R.id.linExpand).setOnClickListener {
            adapter.expandOrCollapse(item, parentPayload = item)
            resetStyle(helper, item)
            rebindTab(getView(R.id.tabLayout), matchOdd)
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

    private fun resetStyle(helper: BaseViewHolder, matchOdd: MatchOdd) {
        val tvExpand = helper.getView<TextView>(R.id.tvExpand)
        val linExpand = helper.getView<View>(R.id.linExpand)
        when{
            matchOdd.oddIdsMap.isNullOrEmpty()->{
                linExpand.isEnabled = false
                tvExpand.setText(R.string.N698)
                tvExpand.setTextColor(ContextCompat.getColor(context, R.color.color_FFFFFF))
                linExpand.background = disableDrawable
                (linExpand.layoutParams as MarginLayoutParams).bottomMargin = 10.dp
                tvExpand.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
            matchOdd.isExpanded->{
                linExpand.isEnabled = true
                tvExpand.setText(R.string.D039)
                tvExpand.setTextColor(ContextCompat.getColor(context, R.color.color_025BE8))
                linExpand.background = collapseDrawable
                (linExpand.layoutParams as MarginLayoutParams).bottomMargin = 10.dp
                tvExpand.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_to_up_blue, 0)
            }
            else->{
                linExpand.isEnabled = true
                tvExpand.setText(R.string.N698)
                tvExpand.setTextColor(Color.WHITE)
                linExpand.background = expandedDrawable
                (linExpand.layoutParams as MarginLayoutParams).bottomMargin = 20.dp
                tvExpand.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_to_down_white, 0)
            }
        }

    }

    private fun rebindTab(tablayout: TabLayout, matchOdd: MatchOdd) {
        tablayout.isVisible = matchOdd.isExpanded
        tablayout.tag = matchOdd
        if (tablayout.tabCount > 0){
            if(tablayout.tabCount == matchOdd.oddIdsMap.size){
                repeat(tablayout.tabCount) {
                    val tab = tablayout.getTabAt(it)!!
                    if (tab.tag == matchOdd.selectPlayCode) {
                        tab.select()
                        return
                    }
                }
            }else{
                tablayout.removeAllTabs()
            }
        }

        if (tablayout.tabCount == 0 ) {
            tablayout.removeOnTabSelectedListener(tabSelectedAdapter)
            matchOdd.oddIdsMap?.keys.forEach {
                val tab = tablayout.newTab().setTag(it).setText(it.getEndScoreNameByTab(context))
                tablayout.addTab(tab)
            }
            tablayout.addOnTabSelectedListener(tabSelectedAdapter)
            val index = matchOdd.oddIdsMap?.keys.indexOf(matchOdd.selectPlayCode)
            tablayout.selectTab(tablayout.getTabAt(index),true)
        }
    }
}