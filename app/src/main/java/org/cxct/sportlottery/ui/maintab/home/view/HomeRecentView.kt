package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ViewHomeRecentBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.transform.TransformInDialog
import splitties.systemservices.layoutInflater

class HomeRecentView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val binding = ViewHomeRecentBinding.inflate(layoutInflater,this,true)
    lateinit var fragment: HomeHotFragment
    val maxItemCount = 20
    private val homeRecentAdapter = HomeRecentAdapter().apply {
        setOnItemClickListener{ adapter, view, position ->
            val item = data[position]
            if (item.recordType==0){
                GameType.getGameType(item.gameType)?.let {
                        if (item.gameType==GameType.ES.key){
                            fragment.getMainTabActivity().jumpToESport(gameType = it)
                        }else{
                            fragment.getMainTabActivity().jumpToSport(gameType = it)
                        }
                    }
            }else{
                item.gameBean?.let { fragment.viewModel.homeOkGamesEnterThirdGame(it, fragment) }
            }
        }
    }
    init {
        initView()
    }

    private fun initView() =binding.run {
        binding.rvRecent.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.rvRecent.adapter = homeRecentAdapter
    }
    fun setup(fragment: HomeHotFragment) {
        this.fragment = fragment
        fragment.apply {
            viewModel.enterThirdGameResult.observe(this) {
                if (isVisibleToUser()) enterThirdGame(it.second, it.first)
            }
            viewModel.gameBalanceResult.observe(this) {
                val event = it.getContentIfNotHandled() ?: return@observe
                TransformInDialog(event.first, event.second, event.third) { enterResult ->
                    enterThirdGame(enterResult, event.first)
                }.show(childFragmentManager, null)
            }
            RecentDataManager.recentEvent.observe(fragment){
                homeRecentAdapter.setList(subMaxCount(it))
                this@HomeRecentView.isVisible = visibleRecent()
            }
        }
        homeRecentAdapter.setList(subMaxCount(RecentDataManager.getRecentList()))
        isVisible = visibleRecent()
    }
    private fun subMaxCount(list: MutableList<RecentRecord>):MutableList<RecentRecord>{
        return if (list.size>maxItemCount) list.subList(0,maxItemCount-1) else list
    }
    private fun visibleRecent():Boolean{
        return homeRecentAdapter.dataCount()!=0&&LoginRepository.isLogined()
    }
}