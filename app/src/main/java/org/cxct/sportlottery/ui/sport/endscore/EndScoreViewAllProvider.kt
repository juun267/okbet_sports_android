package org.cxct.sportlottery.ui.sport.endscore

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.oddsbtn.EndingOddsButton
import org.cxct.sportlottery.util.DisplayUtil.dp

class EndScoreViewAllProvider(val adapter: EndScoreAdapter,
                              val onItemClick:(Int, View, BaseNode) -> Unit,
                              override val itemViewType: Int = 4,
                              override val layoutId: Int = R.layout.item_endscore_viewall): BaseNodeProvider() {


    override fun convert(helper: BaseViewHolder, item: BaseNode) {

    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        onItemClick.invoke(position, view, (data as ViewAllNode).parentNode)
    }

}
