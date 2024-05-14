package org.cxct.sportlottery.ui.maintab.home.game.slot

import android.content.Context
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.onConfirm
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.net.games.data.OKGamesHall
import org.cxct.sportlottery.ui.chat.hideSoftInput
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.maintab.home.game.GameVenueFragment
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.GameCollectManager
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.util.loginedRun
import splitties.views.rightPadding

open class ElectGamesFragment<VM, VB>: GameVenueFragment<OKGamesViewModel, FragmentGamevenueBinding>() {

    private val tabAdapter = ElectTabAdapter() {
        gameAdapter2.findFirmPosition(it.id)?.let { rightManager.scrollToPositionWithOffset(it, 0) }
    }
    val gameAdapter2 = ElectGameAdapter(onFavoriate = { view, gameBean ->
        if (collectGame(gameBean)) {
            view.animDuang(1.3f)
        }
    })
    val rightManager by lazy { GridLayoutManager(requireContext(),2) }

    private fun applySearch(context: Context): EditText {
        val etSearch = AppCompatEditText(context)
        etSearch.layoutParams = LinearLayout.LayoutParams(-1, 32.dp).apply { bottomMargin = 10.dp }
        12.dp.let { etSearch.setPadding(it, 0, it, 0) }
        etSearch.textSize = 14f
        etSearch.gravity = Gravity.CENTER_VERTICAL
        etSearch.setHint(R.string.C001)
        etSearch.setHintTextColor(context.getColor(R.color.color_BEC7DC))
        etSearch.isSingleLine = true
        val icon = context.getDrawable(R.drawable.ic_search_home)!!
        icon.setTint(context.getColor(R.color.color_BEC7DC))
        etSearch.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
        etSearch.background = DrawableCreatorUtils.getCommonBackgroundStyle(8, R.color.transparent, R.color.color_66E0E3EE)
        etSearch.onConfirm { onSearch(it, etSearch) }
        etSearch.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val rightDrawable = etSearch.compoundDrawables[2]
                if (rightDrawable != null && event.x > (etSearch.measuredWidth - rightDrawable.intrinsicWidth - etSearch.rightPadding)) {
                    val key = etSearch.text.toString()
                    if (!key.isEmptyStr()) {
                        hideSoftInput()
                        onSearch(key, etSearch)
                    }
                    return@setOnTouchListener true
                }
            }

            return@setOnTouchListener false
        }

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

    protected open fun onSearch(key: String, editText: EditText) {
        editText.setText("")
        (requireActivity() as MainTabActivity).apply {
            jumpToOKGames()
            binding.root.postDelayed(500){
                (getCurrentFragment() as? OKGamesFragment)?.search(key)
            }
        }
    }

    protected open fun showMoreGames(okGameBean: OKGameBean) {
        (activity as MainTabActivity).jumpToOKGames()
    }

    override fun onInitView(view: View) {
        super.onInitView(view)
        applySearch(view.context)
        binding.rvcGameType.adapter = tabAdapter
        binding.rvcGameList.layoutManager = rightManager
        binding.rvcGameList.adapter = gameAdapter2
        gameAdapter2.setOnItemClickListener{ _, _, position ->
            val okGameBean = gameAdapter2.getItem(position)
            if (okGameBean !is OKGameBean || (!okGameBean.isShowMore && okGameBean.isMaintain())) {
                return@setOnItemClickListener
            }
            if (okGameBean.isShowMore){
                showMoreGames(okGameBean)
                return@setOnItemClickListener
            }

            getMainTabActivity()?.enterThirdGame(okGameBean)
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
        if(gameAdapter2.itemCount > 0) {
            return
        }

        val okGamesHall = OKGamesViewModel.okGamesHall
        if (okGamesHall == null || !setData(okGamesHall.first)) {
//            showLoadingView()
            if (okGamesHall != null && okGamesHall.second - System.currentTimeMillis() > 10_000) {
                viewModel.getOKGamesHall()
            }
        }
    }

    private fun initObserver() {
        viewModel.gameHall.observe(viewLifecycleOwner) {
//            hideLoadingView()
            setData(it)
        }
        viewModel.collectOkGamesResult.observe(viewLifecycleOwner) { result ->
            gameAdapter2.data.forEachIndexed { index, item ->
                (item as? OKGameBean)?.let {
                    if (it.id == result.first) {
                        it.markCollect = result.second.markCollect
                        gameAdapter2.notifyItemChanged(index, it)
                        return@observe
                    }
                }
            }
        }
        GameCollectManager.gameCollectNum.observe(viewLifecycleOwner) {
            gameAdapter2.notifyDataSetChanged()
        }
    }

    private var lastOKGamesHall: OKGamesHall? = null
    protected fun setData(okGamesHall: OKGamesHall): Boolean {
        if (lastOKGamesHall == okGamesHall) {
            return true
        }
        val categoryList = okGamesHall.categoryList?.toMutableList()?.filter { !it.gameList?.isNullOrEmpty() }?.toMutableList()
        if (categoryList.isNullOrEmpty()) {
            return false
        }
        gameAdapter2.setupData(categoryList)
        tabAdapter.setNewInstance(categoryList)
        return true
    }
    fun collectGame(gameData: OKGameBean): Boolean {
        return loginedRun(binding.root.context) { viewModel.collectGame(gameData) }
    }
    fun onFavorite(view: View, bean: OKGameBean){

    }
}