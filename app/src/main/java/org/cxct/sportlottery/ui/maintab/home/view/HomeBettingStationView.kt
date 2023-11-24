package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewHomeBettingstationBinding
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.SpaceItemDecoration
import splitties.systemservices.layoutInflater

class HomeBettingStationView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ViewHomeBettingstationBinding.inflate(layoutInflater,this)
    private lateinit var viewModel:MainHomeViewModel
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
        rvBettingStation.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        rvBettingStation.addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_8))
        PagerSnapHelper().attachToRecyclerView(rvBettingStation)
        rvBettingStation.adapter = homeBettingStationAdapter
    }
    fun setup(fragment: HomeHotFragment) =binding.run{
        viewModel = fragment.viewModel
        viewModel.bettingStationList.observe(fragment) {
            homeBettingStationAdapter.setList(it)
        }
        viewModel.getBettingStationList()
    }

}