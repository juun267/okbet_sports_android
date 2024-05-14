package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.showPromptDialog
import org.cxct.sportlottery.databinding.ViewHomeRecentBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.util.*
import splitties.systemservices.layoutInflater

class HomeRecentView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ViewHomeRecentBinding.inflate(layoutInflater,this)
    private lateinit var fragment: HomeHotFragment
    private val maxItemCount = 20
    private val homeRecentAdapter = HomeRecentAdapter().apply {
        setOnItemClickListener{ _, _, position ->
            val item = data[position]
            if (item.recordType != 0) {
                if (item.gameBean?.firmType==Constants.FIRM_TYPE_SBTY&&!StaticData.sbSportOpened()){
                    fragment.getMainTabActivity().showPromptDialog(message = context.getString(R.string.shaba_no_open)){}
                }else{
                    item.gameBean?.let { fragment.getMainTabActivity().enterHomeGame(it) }
                }
                return@setOnItemClickListener
            }

            if (item.gameType == GameType.ES.key){
                item.categoryCode?.let { fragment.getMainTabActivity().jumpToESport(it) }
            } else {
                GameType.getGameType(item.gameType)?.let {
                    fragment.getMainTabActivity().jumpToSport(gameType = it)
                }
            }
        }
    }
    init {
        orientation = VERTICAL
        initView()
    }

    private fun initView() =binding.run {
        binding.rvRecent.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.rvRecent.adapter = homeRecentAdapter
    }
    fun setup(fragment: HomeHotFragment) {
        this.fragment = fragment
        RecentDataManager.recentEvent.observe(fragment){
            homeRecentAdapter.setList(subMaxCount(it))
            this@HomeRecentView.isVisible = visibleRecent()
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