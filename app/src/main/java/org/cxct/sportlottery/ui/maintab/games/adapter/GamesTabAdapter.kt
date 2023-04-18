package org.cxct.sportlottery.ui.maintab.games.adapter

import android.app.ActionBar.LayoutParams
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.maintab.games.bean.GameTab
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator

class GamesTabAdapter(tabs: MutableList<GameTab>, val onSelected: (GameTab) -> Unit)
    : BaseQuickAdapter<GameTab, GamesTabAdapter.VH>(0, tabs), OnItemClickListener {


    private var selectedTab: GameTab
    private val textColor by lazy { context.getColor(R.color.color_6D7693) }

    init {
        setOnItemClickListener(this)
        selectedTab = tabs[0]
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent.context)
    }

    override fun convert(holder: VH, item: GameTab) = holder.run {
        val isSelected = selectedTab == item
        icon.setImageResource(item.icon)
        name.setText(item.name)
        name.setTextColor(if (isSelected) Color.WHITE else textColor)
        root.isSelected = isSelected
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val item = getItem(position)
        if (selectedTab != item) {
            onSelectChanged(position, item)
            onSelected.invoke(item)
        }
    }

    private fun onSelectChanged(position: Int, item: GameTab) {
        val last = data.indexOf(selectedTab)
        selectedTab = item
        notifyItemChanged(last)
        notifyItemChanged(position)
    }

    class VH(context: Context, val root: LinearLayout = LinearLayout(context)): BaseViewHolder(root) {

        var icon: ImageView
        var name: TextView

        init {
            root.layoutParams = ViewGroup.LayoutParams(-2, 36.dp)
            root.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
            8.dp.let { root.setPadding(it, 0 , it, 0) }
            root.background = getItemBackground()

            icon = AppCompatImageView(context)
            val parms = 20.dp.run { LayoutParams(this,this) }
            parms.rightMargin = 5.dp
            root.addView(icon, parms)

            name = AppCompatTextView(context)
            name.textSize = 14f
            root.addView(name)
        }

        private fun getItemBackground(): Drawable {
            return DrawableCreator.Builder()
                .setCornersRadius(8.dp.toFloat())
                .setSelectedStrokeColor(Color.parseColor("#806FA6FF"), Color.WHITE)
                .setSelectedSolidColor(Color.parseColor("#025BE8"), Color.WHITE)
                .setStrokeWidth(2.dp.toFloat())
                .build()
        }

    }


}