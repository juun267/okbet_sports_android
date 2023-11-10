package org.cxct.sportlottery.ui.maintab.home.game.sport

import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp

class MatchTableAdapter: BaseQuickAdapter<Pair<Int, Sport>, BaseViewHolder>(0) {

    private val selColor = MultiLanguagesApplication.appContext.getColor(R.color.color_025BE8)
    private val norColor = MultiLanguagesApplication.appContext.getColor(R.color.color_6D7693)
    private val numberId = View.generateViewId()
    private val nameId = View.generateViewId()
    private var currentItem: Pair<Int, Sport>? = null

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val lin = LinearLayout(parent.context)
        lin.orientation = LinearLayout.VERTICAL
        lin.gravity = Gravity.CENTER
        lin.layoutParams = LinearLayout.LayoutParams(-1, 68.dp)

        val numText = AppCompatTextView(parent.context)
        numText.id = numberId
        numText.textSize = 16f
        numText.typeface = AppFont.inter_bold
        numText.gravity = Gravity.CENTER_HORIZONTAL
        lin.addView(numText)

        val nameText = AppCompatTextView(parent.context)
        nameText.id = nameId
        nameText.textSize = 12f
        nameText.typeface = AppFont.helvetica
        nameText.gravity = Gravity.CENTER_HORIZONTAL
        4.dp.let { nameText.setPadding(it, 0, it, 0) }
        nameText.maxLines = 1
        nameText.ellipsize = TextUtils.TruncateAt.END
        lin.addView(nameText)

        return BaseViewHolder(lin)
    }

    override fun convert(holder: BaseViewHolder, item: Pair<Int, Sport>) {
        val textColor = if (item == currentItem) selColor else norColor
        val numText = holder.getView<TextView>(numberId)
        numText.text = item.second.num.toString()
        numText.setTextColor(textColor)
        val nameText = holder.getView<TextView>(nameId)
        nameText.setText(item.first)
        nameText.setTextColor(textColor)
        if (item == currentItem) {
            holder.itemView.setBackgroundResource(R.drawable.bg_sportvenue_type)
        } else {
            holder.itemView.background = null
        }
    }

    override fun setNewInstance(list: MutableList<Pair<Int, Sport>>?) {
        currentItem = list?.first()
        super.setNewInstance(list)
    }


}