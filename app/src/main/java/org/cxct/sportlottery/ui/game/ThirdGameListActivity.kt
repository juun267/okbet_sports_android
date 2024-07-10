package org.cxct.sportlottery.ui.game

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.youth.banner.itemdecoration.MarginDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.common.extentions.postDelayed
import org.cxct.sportlottery.common.extentions.showErrorPromptDialog
import org.cxct.sportlottery.databinding.ActivityNewgameListBinding
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.GameCollectManager
import org.cxct.sportlottery.util.GridItemDecoration
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.RecentDataManager
import org.cxct.sportlottery.util.RecentRecord
import org.cxct.sportlottery.util.RefreshHelper
import org.cxct.sportlottery.util.RefreshHelper.LoadMore
import org.cxct.sportlottery.util.isThirdTransferOpen
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.util.startLogin
import org.cxct.sportlottery.view.dialog.TrialGameDialog
import org.cxct.sportlottery.view.transform.TransformInDialog

class ThirdGameListActivity: BaseActivity<OKGamesViewModel, ActivityNewgameListBinding>() {

    private val adapter = GameListAdapter(::enterThirdGame, ::onFavorite)
    private val refreshHelper by lazy { RefreshHelper.of(binding.recyclerView, this@ThirdGameListActivity) }
    private val pageSize = 30

    override fun onInitView() = binding.run {
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        customToolBar.titleText = "New Game"
        customToolBar.setOnBackPressListener { finish() }
        initRecyclerView()
        initRefresh()
    }

    private fun initRecyclerView() = binding.run {
        recyclerView.layoutManager = GridLayoutManager(this@ThirdGameListActivity, 3)
        recyclerView.addItemDecoration(GridItemDecoration(10.dp, 12.dp, Color.TRANSPARENT, true))
        recyclerView.adapter = adapter
    }

    private fun initRefresh() {
        refreshHelper.setPageSize(pageSize)
        refreshHelper.setRefreshListener {
            loadData(1, pageSize)
        }
        refreshHelper.setLoadMoreListener(object : LoadMore {
            override fun onLoadMore(pageIndex: Int, pageSize: Int) {
                loadData(pageIndex, pageSize)
            }

        })
    }

    private fun loadData(pageIndex: Int, pageSize: Int) {
        viewModel.getNewGameList(pageIndex, pageSize)
    }

    override fun onInitData() {
        initObserver()
        refreshHelper.startRefresh()
    }

    private fun initObserver() {
        viewModel.newGameList.observe(this) {
            val gameList = it?.toMutableList()
            if (refreshHelper.isRefreshing) {
                adapter.setNewInstance(gameList)
                refreshHelper.finishRefresh()
            } else {
                gameList?.let { it1 -> adapter.addData(it1) }
                if (gameList == null || gameList.size < pageSize) {
                    refreshHelper.finishRefreshWithNoMoreData()
                } else {
                    refreshHelper.finishLoadMore()
                }
            }
        }

        viewModel.enterThirdGameResult.observe(this) {
            enterThirdGame(it.second, it.first)
        }

        viewModel.gameBalanceResult.observe(this) {
            it.getContentIfNotHandled()?.let { event ->
                TransformInDialog.newInstance(event.first, event.second, event.third).show(supportFragmentManager)
            }
        }

        viewModel.enterTrialPlayGameResult.observe(this) {
            hideLoading()
            if (it == null) {
                //不支持试玩
                startLogin()
            } else {
                //试玩弹框
                val trialDialog = TrialGameDialog(this, it.first, it.second) { firmType, thirdGameResult->
                    enterThirdGame(thirdGameResult, firmType)
                }
                trialDialog.show()
            }
        }

        viewModel.guestLoginGameResult.observe(this) {
            hideLoading()
            if (it == null) {
                //不支持访客
                startLogin()
            } else {
                enterThirdGame(it.second, it.first)
            }
        }

        GameCollectManager.collectStatus.observe(this) {
            adapter.data.forEachIndexed { index, item ->
                if (item.id == it.first) {
                    item.markCollect = it.second
                    adapter.notifyItemChanged(index, it)
                    return@observe
                }
            }
        }
    }

    fun enterThirdGame(result: EnterThirdGameResult, firmType: String) {
        hideLoading()
        if (result.okGameBean == null){
            return
        }
        when (result.resultType) {
            EnterThirdGameResult.ResultType.SUCCESS -> {
                JumpUtil.toThirdGameWeb(this, result.url ?: "", firmType, result.okGameBean, result.guestLogin)
                if (LoginRepository.isLogined()&&!OKGamesRepository.isSingleWalletType(firmType) && isThirdTransferOpen()) viewModel.transfer(firmType)
            }

            EnterThirdGameResult.ResultType.FAIL -> showErrorPromptDialog(
                getString(R.string.prompt), result.errorMsg ?: ""
            ) {}

            EnterThirdGameResult.ResultType.NEED_REGISTER -> LoginOKActivity.startRegist(this)

            EnterThirdGameResult.ResultType.GUEST -> showErrorPromptDialog(
                getString(R.string.error), result.errorMsg ?: ""
            ) {}

            EnterThirdGameResult.ResultType.NONE -> {
            }
        }
        if (result.resultType != EnterThirdGameResult.ResultType.NONE) viewModel.clearThirdGame()
    }

    private fun onFavorite(view: View, okGameBean: OKGameBean) {
        if (loginedRun(binding.root.context) { viewModel.collectGame(okGameBean) }) {
            view.animDuang(1.3f)
        }

    }

    private fun enterThirdGame(gameData: OKGameBean) {
        if(LoginRepository.isLogined()) {
            viewModel.requestEnterThirdGame(gameData, this)
            //有些是手动构造的OKGameBean，需要排除
            //&& (gameData.gameEntryType == GameEntryType.OKGAMES || gameData.gameEntryType==GameEntryType.OKLIVE)
            if (gameData.id > 0){
                OKGamesRepository.addRecentPlayGame(gameData.id.toString())
            }
            RecentDataManager.addRecent(RecentRecord(1, gameBean = gameData))
        } else {
            //请求试玩路线
            loading()
            viewModel.requestEnterThirdGameNoLogin(gameData)
        }
    }

    fun collectGame(gameData: OKGameBean,gameEntryType: String = gameData.gameEntryType ?: GameEntryType.OKGAMES): Boolean {
        return loginedRun(binding.root.context) { viewModel.collectGame(gameData,gameEntryType) }
    }
}