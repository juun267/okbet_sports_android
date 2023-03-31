package org.cxct.sportlottery.ui.sport.endscore

import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.android.synthetic.main.button_odd_outright.view.*
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.oddsbtn.OddsOutrightButton
import org.cxct.sportlottery.util.DisplayUtil.dp

class EndScoreThirdProvider(val adapter: EndScoreAdapter,
                            val onItemClick:(Int, View, BaseNode) -> Unit,
                            override val itemViewType: Int = 3,
                            override val layoutId: Int = 0): BaseNodeProvider() {

    private val padding5 = 5.dp
    private val padding10 = 10.dp

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(OddsOutrightButton(parent.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(-1, -2)
        })
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) = (helper.itemView as OddsOutrightButton).run  {

        val odd = item as Odd
        setupOdd(odd, adapter.oddsType)
        oddStatus = odd.oddState
        tv_spread.text = ""

        val position = item.parentNode.childNode?.indexOf(item) ?: return@run

        if (position < 2) { // 第一列
            if(position % 2 == 0) {
                setMargins(this, padding10, 0, padding5, padding5)
            } else {
                setMargins(this, padding5, 0, padding10, padding5)
            }

        } else {
            if(position % 2 == 0) {
                setMargins(this, padding10, padding5, padding5, padding5)
            } else {
                setMargins(this, padding5, padding5, padding10, padding5)
            }
        }

//        oddStateViewHolder.setupOddState(this, item)  // 闪烁动画
    }

    private fun setMargins(btn: OddsOutrightButton, left: Int, top: Int, right: Int, bottom: Int) {
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
