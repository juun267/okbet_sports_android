package org.cxct.sportlottery.ui.sport.endcard.home.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BaseNodeAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.extentions.circleOf
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemEndcardMatchBinding
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import splitties.systemservices.layoutInflater

class MatchAdapter(showOdd: (MatchOdd) -> Unit): BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(EndCardLeagueNameProvider())
        addFullSpanNodeProvider(EndCardMatchProvider(showOdd))
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is EndCardLeague -> 1
            else -> 2
        }
    }
}

private class EndCardLeagueNameProvider(
    override val itemViewType: Int = 1,
    override val layoutId: Int = 0
) : BaseNodeProvider() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val textView = TextView(parent.context)

        textView.gravity = Gravity.CENTER_VERTICAL
        textView.setTextColor(Color.WHITE)
        textView.textSize = 14f
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.includeFontPadding = false
        textView.setBackgroundColor(parent.context.getColor(R.color.color_1A202E))
        textView.setPadding(4.dp, 0, 0, 0)

        val lp = LinearLayout.LayoutParams(-1, 44.dp)
        lp.topMargin = 4.dp
        textView.layoutParams = lp
        return BaseViewHolder(textView)
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        (helper.itemView as TextView).text = "\t ${(item as EndCardLeague).leagueOdd.league.name}"
    }

}

private class EndCardMatchProvider(
    private val showOdd: (MatchOdd) -> Unit,
    override val itemViewType: Int = 2,
    override val layoutId: Int = 0
) : BaseNodeProvider() {

    private val viewcardBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_353B4E))
            .setRadius(8.dp.toFloat())
            .setWidth(100.dp)
            .setHeight(36.dp)
    }

    private val numBg by lazy {
        val wh = 16.dp
        ShapeDrawable()
            .setSolidColor(Color.RED)
            .setRadius(wh.toFloat())
            .setWidth(wh)
            .setHeight(wh)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val holder = BindingVH.of(ItemEndcardMatchBinding.inflate(parent.layoutInflater, parent, false))
        holder.vb.tvNum.background = numBg
        return holder
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val binding = (helper as BindingVH<ItemEndcardMatchBinding>).vb
        val matchOdd = item as MatchOdd
        val matchInfo = matchOdd.matchInfo!!

        with(binding) {
            tvView.setOnClickListener { showOdd(matchOdd) }
            tvHomeName.text = matchInfo.homeName
            tvAwayName.text = matchInfo.awayName
            tvTime.text = TimeUtil.timeFormat(matchInfo.startTime, TimeUtil.DM_HM_FORMAT)
            ivHomeLogo.circleOf(matchInfo.homeIcon, R.drawable.ic_team_default_no_stroke)
            ivAwayLogo.circleOf(matchInfo.awayIcon, R.drawable.ic_team_default_no_stroke)
            val betNum = matchOdd.bkEndCarkOFLCount
            if (betNum > 0 && LoginRepository.isLogined()) {
                tvNum.text = betNum.toString()
                tvView.setBackgroundResource(R.drawable.ic_viewcard_1)
                tvNum.visible()
            } else {
                tvView.background = viewcardBg
                tvNum.gone()
            }
        }
    }

}

class EndCardLeague(val leagueOdd: LeagueOdd, override val childNode: MutableList<BaseNode>?): BaseNode()