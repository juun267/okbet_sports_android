package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.extentions.showPromptDialog
import org.cxct.sportlottery.databinding.ViewHomeRecentBinding
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import splitties.systemservices.layoutInflater

class HomeRecentView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ViewHomeRecentBinding.inflate(layoutInflater,this)
    private lateinit var fragment: HomeHotFragment
    private val homeRecentAdapter = HomeRecentAdapter().apply {
        setOnItemClickListener{ _, _, position ->
            val item = data[position]
            LogUtil.toJson(item)
            when(item.gameType){
                GameEntryType.SPORT->  GameType.getGameType(item.gameId)?.let { fragment.getMainTabActivity().jumpToSport(gameType = it) }
                GameEntryType.ES-> item.gameId?.let { fragment.getMainTabActivity().jumpToESport(it) }
                else-> fragment.getMainTabActivity().enterThirdGame(item, "首页-最近游戏列表")
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
        OKGamesRepository.recentGamesEvent.observe(fragment){
            homeRecentAdapter.setList(it.toMutableList())
            this@HomeRecentView.isVisible = visibleRecent()
        }
        homeRecentAdapter.setList(OKGamesRepository.recentGamesEvent.value?: listOf())
        isVisible = visibleRecent()
        loadData()
    }
    fun loadData(){
        fragment.viewModel.getHomeRecentPlay()
    }
    private fun visibleRecent():Boolean{
        return homeRecentAdapter.dataCount()!=0&&LoginRepository.isLogined()
    }

    fun applyHalloweenStyle() = binding.run {
        val lin = LinearLayout(context)
        lin.gravity = Gravity.CENTER_VERTICAL
        lin.setPadding(12.dp, 0, 0 , 0)
        val imageView = ImageView(context)
        imageView.setImageResource(R.drawable.ic_halloween_logo_0)
        val dp24 = 24.dp
        lin.addView(imageView, LayoutParams(dp24, dp24))
        removeView(tvCateName)
        val lp = LayoutParams(-2, -2)
        lp.leftMargin = 4.dp
        lin.addView(tvCateName, lp)
        addView(lin, 0, LayoutParams(-1, -2).apply { bottomMargin = 4.dp })
    }
}