package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ViewHomeProviderBinding
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.util.LeftLinearSnapHelper
import splitties.systemservices.layoutInflater

class HomeProviderView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val binding = ViewHomeProviderBinding.inflate(layoutInflater,this)
    private lateinit var viewModel:MainHomeViewModel
    private lateinit var onProviderSelect:(OKGamesFirm)->Unit
    private val pageSize = 3
    private val homeProviderAdapter = HomeProviderAdapter {
        onProviderSelect.invoke(it)
    }
    init {
        orientation = VERTICAL
        initView()
    }

    private fun initView() =binding.run {
        rvProvider.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        rvProvider.adapter = homeProviderAdapter
        LeftLinearSnapHelper().attachToRecyclerView(rvProvider)
    }
    fun setup(fragment: HomeHotFragment,onProviderSelect: (OKGamesFirm)->Unit) {
        viewModel = fragment.viewModel
        this.onProviderSelect = onProviderSelect
        homeProviderAdapter.bindLifecycleOwner(fragment)
        viewModel.homeAllProvider.observe(fragment) { resultData ->
            val buildList = mutableListOf<MutableList<OKGamesFirm>>()
            resultData.forEachIndexed { index, okGamesFirm ->
               if(index%pageSize==0){
                   buildList.add(mutableListOf())
               }
                buildList.last().add(okGamesFirm)
            }
           val list=buildList.map { it.toList() }.toMutableList()
            homeProviderAdapter.setList(list)
        }

        binding.tvMore.setOnClickListener {
            (fragment.activity as MainTabActivity).jumpToOKGames()
        }
    }
    fun bindLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        homeProviderAdapter.bindLifecycleOwner(lifecycleOwner)
    }
    fun loadData(){
        viewModel.getGameFirms()
    }
}