package org.cxct.sportlottery.ui.infoCenter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import org.cxct.sportlottery.R
import org.cxct.sportlottery.adapter.recyclerview.BindingAdapter
import org.cxct.sportlottery.databinding.ContentInfocenterListBinding
import org.cxct.sportlottery.network.infoCenter.InfoCenterData
import org.cxct.sportlottery.util.setDateTime

class InfoCenterAdapter(context: Context) : BindingAdapter<InfoCenterData, ContentInfocenterListBinding>() {

    init {
        setEmptyView(LayoutInflater.from(context).inflate(R.layout.item_footer_no_data, null, false))
    }

    override fun onBinding(position: Int, viewBinding: ContentInfocenterListBinding, item: InfoCenterData) = viewBinding.run {
        txvIndex.text = (position + 1).toString()
        txvTitle.text = item.title
        txvTime.setDateTime(item.addDate?.toLong())
    }

    override fun setNewData(data: MutableList<InfoCenterData>?) {
        super.setNewData(data)
        Log.e("For Test", "=====>>> setNewData ${data?.javaClass?.name}")
    }
}