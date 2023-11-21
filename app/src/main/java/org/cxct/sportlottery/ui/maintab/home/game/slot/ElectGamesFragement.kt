package org.cxct.sportlottery.ui.maintab.home.game.slot

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.onConfirm
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.maintab.home.game.GameVenueFragment
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.util.enterThirdGame
import org.cxct.sportlottery.view.transform.TransformInDialog

open class ElectGamesFragement<M, VB>: GameVenueFragment<OKGamesViewModel, FragmentGamevenueBinding>() {

    private val tabAdapter = ElectTabAdapter() {
        gameAdapter2.findFirmPosition(it.id)?.let { rightManager.scrollToPositionWithOffset(it, 0) }
    }
    private val gameAdapter2 = ElectGameAdapter()
    val rightManager by lazy { GridLayoutManager(requireContext(),2) }

    private fun applySearch(context: Context): EditText {
        val etSearch = AppCompatEditText(context)
        etSearch.layoutParams = LinearLayout.LayoutParams(-1, 32.dp).apply { bottomMargin = 8.dp }
        12.dp.let { etSearch.setPadding(it, 0, it, 0) }
        etSearch.textSize = 14f
        etSearch.gravity = Gravity.CENTER_VERTICAL
        etSearch.setHint(R.string.N900)
        etSearch.setHintTextColor(context.getColor(R.color.color_BEC7DC))
        etSearch.isSingleLine = true
        val icon = context.getDrawable(R.drawable.ic_search_home)!!
        icon.setTint(context.getColor(R.color.color_BEC7DC))
        icon.setBounds(0, 0, 90.dp, 90.dp)
        etSearch.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
        etSearch.background = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.transparent, R.color.color_66E0E3EE)
        etSearch.onConfirm(::onSearch)

        val lin = LinearLayout(context)
        lin.orientation = LinearLayout.VERTICAL
        lin.addView(etSearch)

        val parent = (binding.rvcGameList.parent as ViewGroup)
        val index = parent.indexOfChild(binding.rvcGameList)
        parent.addView(lin, index, binding.rvcGameList.layoutParams)

        parent.removeView(binding.rvcGameList)
        lin.addView(binding.rvcGameList, LinearLayout.LayoutParams(-1, -2))

        return etSearch
    }

    protected open fun onSearch(key: String) {
        (requireActivity() as MainTabActivity).apply {
            jumpToOKGames()
            binding.root.postDelayed(500){
                (getCurrentFragment() as? OKGamesFragment)?.search(key)
            }
        }
    }

    override fun onInitView(view: View) {
        super.onInitView(view)
        applySearch(view.context)
        binding.rvcGameType.adapter = tabAdapter
        binding.rvcGameList.layoutManager = rightManager
        binding.rvcGameList.adapter = gameAdapter2
        gameAdapter2.setOnItemClickListener{ _, _, position ->
            val okGameBean = gameAdapter2.getItem(position)
            if (okGameBean !is OKGameBean || okGameBean.isMaintain()) {
                return@setOnItemClickListener
            }
            if (okGameBean.isShowMore){
                (activity as MainTabActivity).jumpToOKGames()
                return@setOnItemClickListener
            }

            if (LoginRepository.isLogined()) {
                viewModel.homeOkGamesEnterThirdGame(okGameBean, this@ElectGamesFragement)
                viewModel.homeOkGameAddRecentPlay(okGameBean)
            } else {
                //请求试玩路线
                loading()
                viewModel.requestEnterThirdGameNoLogin(okGameBean)
            }
        }

        //实现左侧联动
        binding.rvcGameList.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val firstItemPosition = rightManager.findFirstVisibleItemPosition()
                //这块判断dy！=0是防止左侧联动右侧影响
                if (firstItemPosition == -1 || dy == 0) {
                    return
                }

                val item = gameAdapter2.getItem(firstItemPosition)
                val firmId = if (item is OKGamesCategory) {
                    item.id
                } else {
                    (item as OKGameBean).categoryId
                }
                val leftPosition = tabAdapter.data.indexOfFirst { firmId == it.id }
                if (leftPosition >= 0) {
                    tabAdapter.setSelected(leftPosition)
                }
            }
        })
    }

    override fun onBindViewStatus(view: View) {
        initObserver()
    }

    override fun onInitData() {
        loading()
        viewModel.getOKGamesHall()
    }

    private fun initObserver() {
        viewModel.gameHall.observe(viewLifecycleOwner) {

            val categoryList = it.categoryList?.toMutableList()
            if (categoryList.isNullOrEmpty()) {
                hideLoading()
                return@observe
            }
            
            gameAdapter2.setupData(categoryList, it.firmList)
            tabAdapter.setNewInstance(categoryList)
            hideLoading()
        }

        viewModel.enterThirdGameResult.observe(viewLifecycleOwner) {
            if (isVisibleToUser()) enterThirdGame(it.second, it.first)
        }

        viewModel.gameBalanceResult.observe(viewLifecycleOwner) {
            val event = it.getContentIfNotHandled() ?: return@observe
            TransformInDialog(event.first, event.second, event.third) { enterResult ->
                enterThirdGame(enterResult, event.first)
            }.show(childFragmentManager, null)
        }
    }
}