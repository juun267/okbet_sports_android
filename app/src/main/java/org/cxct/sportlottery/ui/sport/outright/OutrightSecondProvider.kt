package org.cxct.sportlottery.ui.sport.outright

import android.text.TextUtils
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.outright.odds.CategoryOdds
import org.cxct.sportlottery.util.TimeUtil

// 冠军列表-玩法
class OutrightSecondProvider(val adapter: SportOutrightAdapter2,
                             val lifecycle: LifecycleOwner,
                             val onItemClick:(Int, View, BaseNode) -> Unit,
                             override val itemViewType: Int = 2,
                             override val layoutId: Int = R.layout.item_outright_type): BaseNodeProvider() {

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val bean = item as CategoryOdds
        val matchOdd = bean.matchOdd
        helper.setText(R.id.tv_match_name, bean.name)
        helper.itemView.tag = matchOdd
        var startDate = matchOdd?.startDate
        if (TextUtils.isEmpty(startDate)) { //不知道为啥第一条数据一开始的时候会空
            startDate = TimeUtil.timeFormat(matchOdd?.matchInfo?.endTime, TimeUtil.DMY_FORMAT)
            matchOdd?.startTime = TimeUtil.timeFormat(matchOdd?.matchInfo?.endTime, TimeUtil.HM_FORMAT)
        }
        helper.setText(R.id.tv_time,"${startDate} ${matchOdd?.startTime} " + context.getString(R.string.deadline))
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        adapter.expandOrCollapse(data)
        onItemClick.invoke(position, view, data)

    }

}