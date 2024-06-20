package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewHomeBettingstationBinding
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.SpaceItemDecoration
import splitties.systemservices.layoutInflater

class HomeBettingStationView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ViewHomeBettingstationBinding.inflate(layoutInflater,this)
    private val homeBettingStationAdapter = HomeBettingStationAdapter().apply {
        setOnItemChildClickListener { adapter, view, position ->
            val data = (adapter as HomeBettingStationAdapter).data[position]
            JumpUtil.toExternalWeb(
                context,
                "https://maps.google.com/?q=@" + data.lat + "," + data.lon
            )
        }
    }
    init {
        orientation = VERTICAL
        initView()
    }

    private fun initView() =binding.run {
        rvBettingStation.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvBettingStation.addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_10))
        rvBettingStation.adapter = homeBettingStationAdapter
    }
    fun setup(lifecycleOwner: LifecycleOwner,viewModel: MainHomeViewModel) =binding.run{
        viewModel.bettingStationList.observe(lifecycleOwner) {
            homeBettingStationAdapter.setList(it)
        }
        viewModel.getBettingStationList()
    }

}