package org.cxct.sportlottery.ui.sport.endcard.home.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class DateAdapter(private val onItemClick: (leagueOdd: LeagueOdd, List<MatchOdd>) -> Unit)
    : BaseQuickAdapter<Pair<String, List<MatchOdd>>, BaseViewHolder>(0) {

    private var currentLeagueOdd: LeagueOdd? = null
    private var currentItem: Pair<String, List<MatchOdd>>? = null
    private lateinit var today: String

    private val selectedColor by lazy { context.getColor(R.color.color_FFFFFF) }
    private val unSelectedColor by lazy { context.getColor(R.color.color_638095) }
    private val weekId = View.generateViewId()
    private val dateId = View.generateViewId()
    private val selectedDrawable by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_2F4553))
            .setRadius(8.dp.toFloat())
    }

    fun setNewLeagueData(leagueOdd: LeagueOdd?): List<Pair<String, List<MatchOdd>>> {
        currentLeagueOdd = leagueOdd
        if (leagueOdd == null) {
            currentItem = null
            return listOf()
        }

        val weakFormat = SimpleDateFormat("EE")
        val date1Format = SimpleDateFormat("MMM")
        val calendar = Calendar.getInstance()

        val date = Date(System.currentTimeMillis())
        calendar.time = date
        today = "${date1Format.format(date)} ${calendar.get(Calendar.DAY_OF_MONTH)}"

        val dateList = leagueOdd.matchOdds.groupBy {
            val startDate = Date(it.matchInfo!!.startTime)
            calendar.time = startDate
            "${weakFormat.format(startDate)}-${date1Format.format(startDate)} ${calendar.get(Calendar.DAY_OF_MONTH)}"
        }.toList()

        currentItem = dateList.first()
        setNewInstance(dateList.toMutableList())
        return dateList
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val frameLayout = FrameLayout(context)
        val lp = LinearLayout.LayoutParams(60.dp, 44.dp)
        lp.gravity = Gravity.CENTER_VERTICAL
        lp.leftMargin = 12.dp
        lp.topMargin = 10.dp
        frameLayout.layoutParams = lp

        val week = AppCompatTextView(context)
        week.typeface = AppFont.regular
        week.gravity = Gravity.CENTER_HORIZONTAL
        week.id = weekId
        week.textSize = 12f
        val weekLP = FrameLayout.LayoutParams(-1, -2)
        weekLP.topMargin = 3.dp
        frameLayout.addView(week, weekLP)

        val date = AppCompatTextView(context)
        date.typeface = AppFont.regular
        date.gravity = Gravity.CENTER_HORIZONTAL
        date.id = dateId
        date.textSize = 13f
        date.setTextColor(Color.WHITE)
        val dateLP = FrameLayout.LayoutParams(-1, -2)
        dateLP.bottomMargin = 3.dp
        dateLP.gravity = Gravity.BOTTOM
        frameLayout.addView(date, dateLP)

        return BaseViewHolder(frameLayout)
    }

    override fun convert(holder: BaseViewHolder, item: Pair<String, List<MatchOdd>>, payloads: List<Any>) {
        changeStyle(item == currentItem, holder.itemView, holder.getView(weekId), holder.getView(dateId))
    }

    override fun convert(holder: BaseViewHolder, item: Pair<String, List<MatchOdd>>) {
        val week = holder.getView<TextView>(weekId)
        val date = holder.getView<TextView>(dateId)
        val time = item.first.split("-")
        if (item.first.endsWith(today)) {
            week.setText(R.string.C016_small)
        } else {
            week.text = time.getOrNull(0)
        }

        date.text = time.getOrNull(1)
        changeStyle(item == currentItem, holder.itemView, week, date)
        holder.itemView.setOnClickListener {
            val lastPosition = getItemPosition(currentItem)
            currentItem = item
            notifyItemChanged(lastPosition, lastPosition)
            holder.bindingAdapterPosition.let { notifyItemChanged(it, it) }
            onItemClick.invoke(currentLeagueOdd!!, item.second)
        }
    }

    private fun changeStyle(isSelected: Boolean, itemView: View, week: TextView, date: TextView) {
        if (isSelected) {
            week.setTextColor(selectedColor)
//            week.typeface = AppFont.regular
            itemView.background = selectedDrawable
//            date.typeface = AppFont.regular
            date.setTextColor(Color.WHITE)
        } else {
            week.setTextColor(unSelectedColor)
//            week.typeface = AppFont.helvetica
            itemView.background = null
//            date.typeface = AppFont.helvetica
            date.setTextColor(date.resources.getColor(R.color.color_B1BAD3))
        }
    }
}