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
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator

class GamesTabAdapter(val onSelected: (Int) -> Unit)
    : BaseQuickAdapter<Pair<Int, String>, GamesTabAdapter.VH>(0), OnItemClickListener {

    private val dataList = mutableListOf<Pair<Int, String>>()
    private var selectedPosition = 0

    init {
        setNewInstance(dataList)
        setOnItemClickListener(this)
        onSelected(selectedPosition)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent.context)
    }

    override fun convert(holder: VH, item: Pair<Int, String>) = holder.run {
        icon.setImageResource(item.first)
        name.text = item.second
        root.isSelected = holder.bindingAdapterPosition == selectedPosition
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
            root.addView(icon, 20.dp.run { LayoutParams(this,this) })
            name = AppCompatTextView(context)
            name.textSize = 14f
            name.background = DrawableCreator.Builder()
                .setUnSelectedTextColor(Color.parseColor("#6D7693"))
                .setSelectedTextColor(Color.WHITE)
                .build()

        }

        private fun getItemBackground(): Drawable {
            return DrawableCreator.Builder()
                .setCornersRadius(8.dp.toFloat())
                .setSelectedStrokeColor(Color.parseColor("#6FA6FF"), Color.WHITE)
                .setSelectedSolidColor(Color.parseColor("#025BE8"), Color.WHITE)
                .build()
        }

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


}