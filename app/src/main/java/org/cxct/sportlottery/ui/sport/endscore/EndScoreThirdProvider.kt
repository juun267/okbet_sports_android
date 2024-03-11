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

class EndScoreThirdProvider(val adapter: EndScoreAdapter,
                            val onItemClick:(Int, View, BaseNode) -> Unit,
                            override val itemViewType: Int = 3,
                            override val layoutId: Int = R.layout.item_endscore_button): BaseNodeProvider() {

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val endingBtn = helper.getView<EndingOddsButton>(R.id.endingBtn)
        val odd = item as Odd

        endingBtn.apply {
            setupOdd(odd, adapter.oddsType)
            oddStatus = odd.oddState
        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        onItemClick.invoke(position, view, data)
    }

}
