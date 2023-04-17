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
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator

class GamesTabAdapter(val onSelected: (Int) -> Unit)
    : BaseQuickAdapter<Pair<Int, Int>, GamesTabAdapter.VH>(0), OnItemClickListener {

    private val dataList = mutableListOf(Pair(R.drawable.selector_tab_home, R.string.bottom_nav_home),
        Pair(R.drawable.selector_tab_sport, R.string.main_tab_sport),
        Pair(R.drawable.selector_tab_betlist, R.string.main_tab_betlist),
        Pair(R.drawable.selector_tab_fav, R.string.main_tab_favorite),
        Pair(R.drawable.selector_tab_user, R.string.main_tab_mine))
    private var selectedPosition = 0
    private val textColor by lazy { context.getColor(R.color.color_6D7693) }

    init {
        setNewInstance(dataList)
        setOnItemClickListener(this)
        onSelected(selectedPosition)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent.context)
    }

    override fun convert(holder: VH, item: Pair<Int, Int>) = holder.run {
        val isSelected = dataList.indexOf(item) == selectedPosition
        icon.setImageResource(item.first)
        name.setText(item.second)
        name.setTextColor(if (isSelected) Color.WHITE else textColor)
        root.isSelected = isSelected
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        if (selectedPosition != position) {
            onSelectChanged(position)
            onSelected.invoke(position)
        }
    }

    private fun onSelectChanged(position: Int) {
        val last = selectedPosition
        selectedPosition = position
        notifyItemChanged(last)
        notifyItemChanged(selectedPosition)
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