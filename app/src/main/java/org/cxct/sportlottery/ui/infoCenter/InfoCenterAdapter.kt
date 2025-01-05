package org.cxct.sportlottery.ui.infoCenter

import org.cxct.sportlottery.databinding.ContentInfocenterListBinding
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.util.setDateTime

class InfoCenterAdapter : BindingAdapter<InfoCenterData, ContentInfocenterListBinding>() {

    override fun onBinding(position: Int, viewBinding: ContentInfocenterListBinding, item: InfoCenterData) = viewBinding.run {
        txvIndex.text = (position + 1).toString()
        txvTitle.text = item.title
        txvTime.setDateTime(item.addDate?.toLongOrNull())
    }
}