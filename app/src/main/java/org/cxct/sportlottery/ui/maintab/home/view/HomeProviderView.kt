package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewHomeProviderBinding
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.util.DisplayUtil.dp
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

    fun applyHalloweenStyle() = binding.run {
        (layoutParams as MarginLayoutParams).bottomMargin = 0

        val imageView = AppCompatImageView(context)
        imageView.setImageResource(R.drawable.ic_halloween_logo_4)
        val dp24 = 24.dp
        val lp = LayoutParams(dp24, dp24)
        lp.gravity = Gravity.CENTER_VERTICAL
        linTitle.addView(imageView, 0, lp)

        val frameLayout = FrameLayout(context)
        val ivMonster = ImageView(context)
        ivMonster.setBackgroundResource(R.drawable.img_monster_1_h)
        val monsterLP = FrameLayout.LayoutParams(158.dp, 80.dp)
        monsterLP.topMargin = (-13).dp
        monsterLP.gravity = Gravity.RIGHT
        frameLayout.addView(ivMonster, monsterLP)

        (linTitle.parent as ViewGroup).removeView(linTitle)
        linTitle.setPadding(0, 5.dp, 0, 0)
        frameLayout.addView(linTitle)

        val rcvLP = FrameLayout.LayoutParams(-1, -2)
        rcvLP.topMargin = 35.dp
        rcvLP.leftMargin = 12.dp
        (rvProvider.parent as ViewGroup).removeView(rvProvider)
        frameLayout.addView(rvProvider, rcvLP)

        addView(frameLayout)
    }
}