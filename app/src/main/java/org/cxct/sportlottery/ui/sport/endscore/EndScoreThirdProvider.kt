package org.cxct.sportlottery.ui.sport.endscore

import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.oddsbtn.EndingOddsButton
import org.cxct.sportlottery.util.DisplayUtil.dp

class EndScoreThirdProvider(val adapter: EndScoreAdapter,
                            val onItemClick:(Int, View, BaseNode) -> Unit,
                            override val itemViewType: Int = 3,
                            override val layoutId: Int = 0): BaseNodeProvider() {

    private val padding8 = 8.dp
    private val padding10 = 10.dp
    private val padding12 = 12.dp
    private val lines = 4  // 列数

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(EndingOddsButton(parent.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(-1, 36.dp)
        })
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) = (helper.itemView as EndingOddsButton).run  {
        val odd = item as Odd
        setupOdd(odd, adapter.oddsType)
        oddStatus = odd.oddState

        val position = item.parentNode.childNode?.indexOf(item) ?: return@run

        when (position % lines) {
            0 -> setMargins(this, padding12, padding8, 0, 0)
            lines - 1 -> setMargins(this, padding10, padding8, padding12, 0)
            else -> setMargins(this, padding10, padding8, 0, 0)
        }
    }

    private fun setMargins(btn: View, left: Int, top: Int, right: Int, bottom: Int) {
        val lParams = btn.layoutParams as ViewGroup.MarginLayoutParams
        lParams.leftMargin = left
        lParams.topMargin = top
        lParams.rightMargin = right
        lParams.bottomMargin = bottom
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        onItemClick.invoke(position, view, data)
    }

}
