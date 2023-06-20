package org.cxct.sportlottery.ui.sport.outright

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.android.synthetic.main.button_odd_outright.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.CategoryOdds
import org.cxct.sportlottery.ui.sport.oddsbtn.OddsOutrightButton
import org.cxct.sportlottery.util.DisplayUtil.dp

// 冠军列表-赔率
class OutrightThirdProvider(val adapter: SportOutrightAdapter2,
                            val lifecycle: LifecycleOwner,
                            val onItemClick:(Int, View, BaseNode) -> Unit,
                            override val itemViewType: Int = 3,
                            override val layoutId: Int = 0): BaseNodeProvider() {

    val padding5 = 5.dp
    val padding10 = 10.dp

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(OddsOutrightButton(parent.context).apply {
            foreground = ContextCompat.getDrawable(context, R.drawable.fg_ripple)
            layoutParams = ViewGroup.MarginLayoutParams(-1, -2)
        })
    }

    override fun convert(helper: BaseViewHolder, item: BaseNode) = (helper.itemView as OddsOutrightButton).run  {
        setupOdd(item as Odd, adapter.oddsType)
        oddStatus = item.oddState
        tv_spread.text = ""
        val parentNode = item.parentNode as CategoryOdds

        val position = parentNode.indexOf(item)  //indexOfCurrentGroup(helper.bindingAdapterPosition)

        if (position < 2) { // 第一列
            if(position == 0) {
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

    }

    private fun setMargins(btn: OddsOutrightButton, left: Int, top: Int, right: Int, bottom: Int) {
        val lParams = btn.layoutParams as ViewGroup.MarginLayoutParams
        lParams.leftMargin = left
        lParams.topMargin = top
        lParams.rightMargin = right
        lParams.bottomMargin = bottom
    }

    // 所处当前分组的index
    private fun indexOfCurrentGroup(position: Int): Int {

        repeat(position) {
            val index = 1 + it
            if (adapter.getItem(position - index) !is Odd ){
                return it
            }
        }
        return 0
    }


    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        onItemClick.invoke(position, view, data)
    }


}
