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
import org.cxct.sportlottery.ui.maintab.games.bean.OKGameTab
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator

class GamesTabAdapter(private val onSelected: (OKGameTab) -> Boolean)
    : BaseQuickAdapter<OKGameTab, GamesTabAdapter.VH>(0), OnItemClickListener {

    private val textColor by lazy { context.getColor(R.color.color_6D7693) }
    private val localTabs = mutableListOf<OKGameTab>(GameTab.TAB_ALL, GameTab.TAB_FAVORITES, GameTab.TAB_RECENTLY)

    var selectedTab: OKGameTab
        private set

    init {
        setNewInstance(localTabs)
        setOnItemClickListener(this)
        selectedTab = localTabs[0]
    }

    override fun addData(newData: Collection<OKGameTab>) {
        val newTabs = mutableListOf<OKGameTab>()
        newTabs.addAll(localTabs)
        newTabs.addAll(newData)
        super.setNewInstance(newTabs)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent.context)
    }

    override fun convert(holder: VH, item: OKGameTab) = holder.run {
        val isSelected = selectedTab == item
        item.bindNameText(name)
        item.bindTabIcon(icon, isSelected)
        name.setTextColor(if (isSelected) Color.WHITE else textColor)
        root.isSelected = isSelected
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val item = getItem(position)
        if (selectedTab != item && onSelected.invoke(item)) {
            onSelectChanged(position, item)
        }
    }

    private fun onSelectChanged(position: Int, item: OKGameTab) {
        val last = data.indexOf(selectedTab)
        selectedTab = item
        notifyItemChanged(last)
        notifyItemChanged(position)
    }

    fun backToAll() {
        val item = data[0]
        if (selectedTab != item) {
            onSelectChanged(0, item)
        }
        onSelected.invoke(item)
    }

    fun changeSelectedTab(tab: OKGameTab): Int {

        data.forEachIndexed { index, okGameTab ->
            if (tab.getKey() == okGameTab.getKey()) {
                if (selectedTab != okGameTab && onSelected.invoke(okGameTab)) {
                    onSelectChanged(index, okGameTab)
                }
                return index
            }
        }

        return -1
    }

    class VH(context: Context, val root: LinearLayout = LinearLayout(context)): BaseViewHolder(root) {

        var icon: ImageView
        var name: TextView

        init {
            root.layoutParams = ViewGroup.LayoutParams(-2, 36.dp)
            root.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
            root.minimumWidth = 71.dp
            8.dp.let { root.setPadding(it, 0 , it, 0) }
            root.background = getItemBackground(context)

            icon = AppCompatImageView(context)
            val parms = 20.dp.run { LayoutParams(this,this) }
            parms.rightMargin = 5.dp
            root.addView(icon, parms)

            name = AppCompatTextView(context)
            name.textSize = 14f
            root.addView(name)
        }

        private fun getItemBackground(context: Context): Drawable {
            return DrawableCreator.Builder()
                .setCornersRadius(8.dp.toFloat())
                .setSelectedStrokeColor(context.getColor(R.color.color_806FA6FF), Color.WHITE)
                .setSelectedSolidColor(context.getColor(R.color.color_025BE8), Color.WHITE)
                .setStrokeWidth(2.dp.toFloat())
                .build()
        }

    }


}