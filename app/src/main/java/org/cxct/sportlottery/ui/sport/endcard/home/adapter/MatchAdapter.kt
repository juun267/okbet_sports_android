package org.cxct.sportlottery.ui.sport.endcard.home.adapter

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BaseNodeAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.extentions.circleOf
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemEndcardMatchBinding
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.ArithUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.Spanny
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import splitties.systemservices.layoutInflater
import kotlin.random.Random

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

    fun updateBetsNum(matchId: String, betsNum: Int) {
        if (data.isEmpty()) {
            return
        }

        data.forEachIndexed { index, baseNode ->
            if (baseNode is MatchOdd && matchId == baseNode.matchInfo?.id) {
                baseNode.bkEndCarkOFLCount = betsNum + baseNode.bkEndCarkOFLCount
                notifyItemChanged(index, baseNode)
                return
            }
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
        textView.typeface = AppFont.helvetica
        textView.includeFontPadding = false
        textView.setBackgroundColor(parent.context.getColor(R.color.color_213643))
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
            .setSolidColor(context.getColor(R.color.color_355D73))
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

    override fun convert(helper: BaseViewHolder, item: BaseNode, payloads: List<Any>) {
        val matchOdd = item as MatchOdd
        val betNum = matchOdd.bkEndCarkOFLCount
        with((helper as BindingVH<ItemEndcardMatchBinding>).vb) {
            tvNum.text = if (betNum > 99) "99+" else betNum.toString()
            tvView.setBackgroundResource(R.drawable.ic_viewcard_1)
            tvNum.visible()
        }
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val binding = (helper as BindingVH<ItemEndcardMatchBinding>).vb
        val matchOdd = item as MatchOdd
        val matchInfo = matchOdd.matchInfo!!

        with(binding) {
            val league = (item.parentNode as LeagueOdd).league
            matchInfo.categoryIcon = if (league.icon.isEmptyStr()) league.categoryIcon else league.icon
            tvView.setOnClickListener { showOdd(matchOdd) }
            tvHomeName.text = matchInfo.homeName
            tvAwayName.text = matchInfo.awayName
            tvTime.text = TimeUtil.timeFormat(matchInfo.startTime, TimeUtil.DMY_HM_FORMAT)
            ivHomeLogo.circleOf(matchInfo.homeIcon, R.drawable.ic_team_default_1)
            ivAwayLogo.circleOf(matchInfo.awayIcon, R.drawable.ic_team_default_1)
            val betNum = matchOdd.bkEndCarkOFLCount
            if (betNum > 0 && LoginRepository.isLogined()) {
                tvNum.text = if (betNum > 99) "99+" else betNum.toString()
                tvView.setBackgroundResource(R.drawable.ic_viewcard_1)
                tvNum.visible()
            } else {
                tvView.background = viewcardBg
                tvNum.gone()
            }
            //2. 热度说明：NBA及PBA赛事11-30中随机值；其他联赛1-15中随机值（可由前端做）
            val hotCount=if (matchInfo.shortName?.contains("NBA") == true||matchInfo.leagueName?.contains("NBA") == true){
                Random.nextInt(11,30)
            }else{
                Random.nextInt(1,15)
            }.toString()
            tvHotCount.text = Spanny(String.format(context.getString(R.string.P360),hotCount))
                .findAndSpan(hotCount){ ForegroundColorSpan(ContextCompat.getColor(context,R.color.color_1CD219)) }

            val betCount = matchInfo.betCount.toString()
            tvBetCount.text = Spanny(String.format(context.getString(R.string.P361),betCount))
                .findAndSpan(betCount){ ForegroundColorSpan(ContextCompat.getColor(context,R.color.color_1CD219)) }

        }
    }

}

class EndCardLeague(val leagueOdd: LeagueOdd, override val childNode: MutableList<BaseNode>?): BaseNode()