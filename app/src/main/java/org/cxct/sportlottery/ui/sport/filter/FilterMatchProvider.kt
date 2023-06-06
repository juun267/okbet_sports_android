package org.cxct.sportlottery.ui.sport.filter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.list.MatchOdd

class FilterMatchProvider(val adapter: LeagueSelectAdapter,
                          val onItemClick:(Int, View, BaseNode) -> Unit,
                          override val itemViewType: Int = 2,
                          override val layoutId: Int = R.layout.item_filter_match): BaseNodeProvider() {

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val matchOdd = item as MatchOdd
        helper.setText(R.id.tvMatchName, matchOdd.matchInfo?.homeName+" VS "+matchOdd.matchInfo?.awayName)
        val ivCheck=helper.getView<ImageView>(R.id.ivCheck)
        ivCheck.isSelected = matchOdd.isSelected
        helper.itemView.setOnClickListener {
            matchOdd.isSelected = !matchOdd.isSelected
            ivCheck.isSelected = matchOdd.isSelected
            onItemClick.invoke(adapter.getItemPosition(item), it, matchOdd)
        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        onItemClick.invoke(position, view, data)
    }

}