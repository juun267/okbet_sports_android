package org.cxct.sportlottery.ui.maintab.games

import android.content.Intent
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_home_slot.*
import kotlinx.android.synthetic.main.view_toolbar_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.extentions.fitsSystemStatus
import org.cxct.sportlottery.extentions.gone
import org.cxct.sportlottery.extentions.isEmptyStr
import org.cxct.sportlottery.extentions.visible
import org.cxct.sportlottery.network.third_game.third_games.GameCategory
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues
import org.cxct.sportlottery.repository.ThirdGameRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.maintab.HomeFragment
import org.cxct.sportlottery.ui.maintab.HomeTabAdapter
import org.cxct.sportlottery.ui.maintab.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.*

class ThirdGamesFragment: BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    private val GAME_CODE by lazy { requireArguments().getString("GAME_CODE")!! }
    private val CODE_LIVE = "LIVE"
    private val CODE_POKER = "QP"
    private val LIVE_OPEN_FILTER = mutableListOf<String>()  // 如果是'真人'游戏则进行本地过滤

    override fun layoutId() = R.layout.fragment_home_slot

    private val gamesAdapter by lazy { ThirdGamesAdapter2().apply {
        val datas = when(GAME_CODE) {
            CODE_LIVE -> ThirdGames.live
            CODE_POKER -> ThirdGames.qipai
            else -> ThirdGames.caipiao
        }

        datas.forEach { LIVE_OPEN_FILTER.add(it.playCode) }
        setNewInstance(datas)
    } }

    private val homeTabAdapter by lazy {
        HomeTabAdapter(HomeTabAdapter.getItems(),
            requireArguments().getInt("position"),
            (parentFragment as HomeFragment))
    }

    override fun onBindView(view: View) {
        view.fitsSystemStatus()
        initToolBar()
        initTab()
        initGamesList()
        initObserver()
        viewModel.getThirdGame()
    }

    fun initToolBar() {
        view?.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        iv_menu_left.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            (activity as MainTabActivity).showLeftFrament(0, 5)
        }
        iv_logo.setOnClickListener {
            (activity as MainTabActivity).jumpToHome(0)
        }
        btn_register.setOnClickListener {
            startActivity(Intent(requireActivity(), RegisterOkActivity::class.java))
        }
        btn_login.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }
        iv_money_refresh.setOnClickListener {
            iv_money_refresh.startAnimation(
                RotateAnimation(0f,
                720f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f).apply {
                duration = 1000
            })
            viewModel.getMoney()
        }

        setupLogin()
    }

    private fun setupLogin() {
        viewModel.isLogin.value?.let {
            btn_register.isVisible = !it
            btn_login.isVisible = !it
            ll_user_money.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun initGamesList() {
        rv_slot.visible()
        lin_empty_game.gone()

        rv_slot.layoutManager = LinearLayoutManager(context)
        rv_slot.adapter = gamesAdapter
        gamesAdapter.setOnItemClickListener { _, _, position ->
            val item = gamesAdapter.getItem(position)
            viewModel.requestEnterThirdGame("${item.firmType}", "${item.playCode}", GAME_CODE)
        }
    }

    private fun initTab() = rv_tab_home.run {
        layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = homeTabAdapter
        post {
            (layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(this,
                RecyclerView.State(),
                homeTabAdapter.selectPos)
        }
    }

    private fun initObserver() {

        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.let {
                tv_home_money.text = "${sConfigData?.systemCurrencySign} ${TextUtil.format(it)}"
            }
        }
        viewModel.isLogin.observe(viewLifecycleOwner) {
            setupLogin()
        }
        ThirdGameRepository.thirdGameData.observe(this) {
            if (it == null || it.gameCategories.isNullOrEmpty() || it.gameFirmMap.isNullOrEmpty()) {
                return@observe
            }

            filterData(it.gameCategories, it.gameFirmMap)
        }

        viewModel.enterThirdGameResult.observe(viewLifecycleOwner) {
            if (isVisible)
                enterThirdGame(it)
        }

    }

    private fun filterData(gameCategories: List<GameCategory>, gameFirmMap: Map<String, GameFirmValues>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val liveCategory = gameCategories.find { GAME_CODE == it.code } ?: return@launch
            if (liveCategory.gameFirmIds.isEmptyStr()) {
                return@launch
            }

            val liveIds = liveCategory.gameFirmIds!!.split(",")
            if (liveIds.isNullOrEmpty()) {
                return@launch
            }

            val liveGames = mutableListOf<GameFirmValues>()

            gameFirmMap.values.forEach {
                if(liveIds.contains("${it.id}") && LIVE_OPEN_FILTER.contains("${it.playCode}")) {
                    liveGames.add(it)
                }
            }

            withContext(Dispatchers.Main) {
                gamesAdapter.update(liveGames)
            }

//            withContext(Dispatchers.Main) {
//                if (liveGames.isNotEmpty()) {
//                    gamesAdapter.setNewInstance(liveGames)
//                    rv_slot.visible()
//                    lin_empty_game.gone()
//                } else {
//                    lin_empty_game.visible()
//                    rv_slot.gone()
//                }
//            }
        }
    }

    private fun enterThirdGame(result: EnterThirdGameResult) {
        hideLoading()
        when (result.resultType) {
            EnterThirdGameResult.ResultType.SUCCESS -> context?.run {
                JumpUtil.toThirdGameWeb(
                    this,
                    result.url ?: "",
                    thirdGameCategoryCode = result.thirdGameCategoryCode
                )
            }
            EnterThirdGameResult.ResultType.FAIL -> showErrorPromptDialog(
                getString(R.string.prompt),
                result.errorMsg ?: ""
            ) {}
            EnterThirdGameResult.ResultType.NEED_REGISTER -> context?.startActivity(
                Intent(
                    context,
                    if (isOKPlat()) RegisterOkActivity::class.java else RegisterActivity::class.java)
            )
            EnterThirdGameResult.ResultType.GUEST -> showErrorPromptDialog(
                getString(R.string.error),
                result.errorMsg ?: ""
            ) {}
            EnterThirdGameResult.ResultType.NONE -> {
            }
        }
        if (result.resultType != EnterThirdGameResult.ResultType.NONE)
            viewModel.clearThirdGame()
    }

}