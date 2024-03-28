package org.cxct.sportlottery.ui.sport.endcard.home.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.setLeagueLogo

class LeagueAdapter(private val onItemClick: (LeagueOdd) -> Unit)
    : BaseQuickAdapter<LeagueOdd, BaseViewHolder>(0) {

    private var currentItem: LeagueOdd? = null
    private val iconWH = 52.dp
    private val iconId = View.generateViewId()
    private val nameId = View.generateViewId()
    private val selectedColor = Color.WHITE
    private val unSelectedColor by lazy { context.getColor(R.color.color_6D7693) }
    private val iconLP = LinearLayout.LayoutParams(iconWH, iconWH)
    private val nameLP = LinearLayout.LayoutParams(-2, -2).apply { topMargin = 4.dp }
    private val iconBg by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_314260))
            .setWidth(iconWH)
            .setHeight(iconWH)
            .setRadius(iconWH.toFloat())
    }
    private val iconForeground by lazy {
        ShapeDrawable()
            .setSolidColor(context.getColor(R.color.color_4D333333))
            .setWidth(iconWH)
            .setHeight(iconWH)
            .setRadius(iconWH.toFloat())
    }


    override fun setNewInstance(list: MutableList<LeagueOdd>?) {
        currentItem = list?.getOrNull(0)
        super.setNewInstance(list)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val cxt = parent.context
        val root = LinearLayout(cxt)
        root.layoutParams = LinearLayout.LayoutParams(iconWH, -1).apply { rightMargin = 16.dp }
        root.gravity = Gravity.CENTER
        root.orientation = LinearLayout.VERTICAL

        val icon = AppCompatImageView(cxt)
        icon.id = iconId
        icon.background = iconBg
        11.dp.let { icon.setPadding(it, it, it, it) }
        root.addView(icon, iconLP)

        val name = AppCompatTextView(cxt)
        name.ellipsize = TextUtils.TruncateAt.END
        name.id = nameId
        name.gravity = Gravity.CENTER
        name.maxLines = 1
        name.textSize = 14f
        root.addView(name, nameLP)

        return BaseViewHolder(root)
    }

    override fun convert(holder: BaseViewHolder, item: LeagueOdd, payloads: List<Any>) {
        changeStyle(currentItem == item, holder.getView(iconId), holder.getView(nameId))
    }

    override fun convert(holder: BaseViewHolder, item: LeagueOdd) {

        val nameText = holder.getView<TextView>(nameId)
        nameText.text = if (item.league.shortName.isEmptyStr()) item.league.name else item.league.shortName

        val icon = holder.getView<ImageView>(iconId)

        if (item.league.icon.isEmptyStr()) {
            icon.setLeagueLogo(item.league.categoryIcon)
        } else {
            icon.load(item.league.icon, R.drawable.ic_team_default)
        }

        changeStyle(currentItem == item, icon, nameText)

        holder.itemView.setOnClickListener {
            if (currentItem == item) {
                return@setOnClickListener
            }

            val lastIndex = getItemPosition(currentItem)
            currentItem = item
            notifyItemChanged(lastIndex, lastIndex)
            notifyItemChanged(holder.bindingAdapterPosition, 0)
            onItemClick.invoke(item)
        }
    }

    private fun changeStyle(isSelected: Boolean, icon: ImageView, nameText: TextView) {
        if (isSelected) {
            nameText.setTextColor(selectedColor)
            nameText.typeface = Typeface.DEFAULT_BOLD
            icon.foreground = null
        } else {
            nameText.setTextColor(unSelectedColor)
            nameText.typeface = Typeface.DEFAULT
            icon.foreground = iconForeground
        }
    }


}