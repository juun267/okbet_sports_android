package org.cxct.sportlottery.ui.sport.endscore

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.sport.SportFragment2
import org.cxct.sportlottery.ui.sport.list.adapter.SportMatchEvent
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.view.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.view.tablayout.TabSelectedAdapter

class EndScoreSecondProvider(val adapter: EndScoreAdapter,
                             val onItemClick:(Int, View, BaseNode) -> Unit,
                             override val itemViewType: Int = 2,
                             override val layoutId: Int = R.layout.item_endscore_battle): BaseNodeProvider() {

    override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) = helper.run {
        if (payloads.isEmpty()) {
            return@run
        }

        val matchInfo = (item as MatchOdd).matchInfo
        if (payloads.first() is SportMatchEvent.FavoriteChanged) {
            getView<View>(R.id.league_odd_match_favorite).isSelected = matchInfo?.isFavorite ?: false
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
            onItemClick.invoke(helper.bindingAdapterPosition,
                it,
                item)
        }

        getView<View>(R.id.league_odd_match_favorite).run {
            isSelected = matchInfo?.isFavorite ?: false
            setOnClickListener { onItemClick.invoke(helper.bindingAdapterPosition, this, item) }
        }
        val tvExpand = getView<TextView>(R.id.tvExpand)
        val linExpand = getView<View>(R.id.linExpand)
        val tabLayou = getView<TabLayout>(R.id.tabLayout)
        resetStyle(getView(R.id.llMatchInfo),linExpand, tvExpand,tabLayou, item.isExpanded)
        linExpand.setOnClickListener {
            adapter.expandOrCollapse(item, parentPayload = item)
            resetStyle(getView(R.id.llMatchInfo),linExpand, tvExpand,tabLayou, item.isExpanded)
        }
        tabLayou.apply {
            if (tabCount>0){
                removeAllTabs()
            }
            LogUtil.toJson(matchOdd.oddIdsMap?.map { it.key+","+it.value?.size })
            matchOdd.oddIdsMap?.forEach {
                matchOdd.betPlayCateNameMap?.get(it.key)?.get(LanguageManager.getSelectLanguage(context).key)?.let { name->
                    addTab(newTab().setTag(it.key).setText(name))
                }
            }
            if (tabCount>0) {
                matchOdd.selectPlayCode = getTabAt(0)?.tag.toString()
            }else{
                matchOdd.selectPlayCode = PlayCate.FS_LD_CS.value
            }
            addOnTabSelectedListener(TabSelectedAdapter{ tab, _ ->
                matchOdd.selectPlayCode = tab.tag.toString()
                matchOdd.oddIdsMap?.get(matchOdd.selectPlayCode)?.let {
                    it.values.let {
                        post{
                            adapter.nodeReplaceChildData(item, it)
                        }
                    }
                }
            })
            OverScrollDecoratorHelper.setUpOverScroll(this)
        }
    }

    private val expandedDrawable by lazy {
        DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.color_025BE8)
    }

    private val collapseDrawable by lazy {
        DrawableCreatorUtils.getCommonBackgroundStyle(8, android.R.color.white, R.color.color_025BE8, 1)
    }

    private fun resetStyle(llMatchInfo: View, linExpand: View, tvExpand: TextView,tabLayout: TabLayout, isExpand: Boolean) = tvExpand.run {
        if (isExpand) {
            setText(R.string.D039)
            setTextColor(ContextCompat.getColor(context, R.color.color_025BE8))
            linExpand.background = collapseDrawable
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_to_up_blue, 0)
            llMatchInfo.setBackgroundColor(ContextCompat.getColor(context, R.color.color_F8F9FD))
        } else {
            setText(R.string.N698)
            setTextColor(Color.WHITE)
            linExpand.background = expandedDrawable
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_to_down_white, 0)
            llMatchInfo.setBackgroundColor(Color.TRANSPARENT)
        }
        tabLayout.isVisible = isExpand
    }

}