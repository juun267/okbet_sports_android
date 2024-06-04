package org.cxct.sportlottery.ui.money.withdraw.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ViewAnnouncementBinding
import org.cxct.sportlottery.net.message.AnnouncementRepository
import org.cxct.sportlottery.network.message.Row
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.maintab.publicity.MarqueeAdapter
import splitties.systemservices.layoutInflater

class AnnouncementView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
 : LinearLayout(context, attrs, defStyle) {

    val binding by lazy { ViewAnnouncementBinding.inflate(layoutInflater,this) }
    private lateinit var marqueeAdapter: MarqueeAdapter
    init {
        orientation = VERTICAL
    }

    fun setUp(lifecycleOwner: LifecycleOwner,viewModel: BaseViewModel,typeList: Array<Int>,msgType: Int?){
        initMarquee(lifecycleOwner)
        viewModel.launch {
            callApi({ AnnouncementRepository.getWithdrawAnnouncement(typeList,msgType) }) {
               it.getData()?.filter { it.type.toInt() == 1 }?.sortedWith(compareByDescending<Row> { it.sort }.thenByDescending { it.addTime })?.map { it.message }.let {
                   setList(it?.toMutableList())
               }
            }
        }
    }
    private fun initMarquee(lifecycleOwner: LifecycleOwner)=binding.run {
        rvMarquee.bindLifecycler(lifecycleOwner)
        rvMarquee.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        marqueeAdapter = object : MarqueeAdapter() {
            override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val viewHolder = super.onCreateViewHolder(viewGroup, viewType)
                if (viewHolder is MarqueeVH) {
                    viewHolder.textView.setTextColor(context.getColor(R.color.color_313F56))
                }
                return viewHolder
            }
        }
        rvMarquee.adapter = marqueeAdapter
    }


    private fun setList(items: MutableList<String>?)=binding.run{
        if (items.isNullOrEmpty()){
            linAnnouncement.gone()
        }else{
            linAnnouncement.visible()
            marqueeAdapter.setData(items)
            rvMarquee.startAuto(false)
        }
    }
}