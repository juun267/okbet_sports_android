package org.cxct.sportlottery.ui.main.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_dz.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.GameTabData
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.startRegister

class DZFragment(private val gameCateData: GameCateData, private val defaultSelectFirmCode: String) : BaseFragment<MainViewModel>(MainViewModel::class) {

    private var mOnSelectThirdGameListener: OnSelectItemListener<ThirdDictValues?> = object : OnSelectItemListener<ThirdDictValues?> {
        override fun onClick(select: ThirdDictValues?) {
            loading()
            viewModel.requestEnterThirdGame(select)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycleView()
        initObserve()
    }

    private fun initRecycleView() {
        var defaultSelectPosition = 0
        var defaultSelectGameList = gameCateData.tabDataList.firstOrNull()?.gameList
        gameCateData.tabDataList.forEachIndexed { index, gameTabData ->
            if (gameTabData.gameFirm?.firmCode == defaultSelectFirmCode) {
                defaultSelectPosition = index
                defaultSelectGameList = gameTabData.gameList
            }
        }

        val rvDZAdapter = RvDZAdapter()
        rvDZAdapter.setData(defaultSelectGameList)
        rvDZAdapter.setOnSelectThirdGameListener(mOnSelectThirdGameListener)
        rv_dz.adapter = rvDZAdapter

        val rvDZTabAdapter = RvDZTabAdapter(defaultSelectPosition)
        rvDZTabAdapter.setData(gameCateData.tabDataList)
        rvDZTabAdapter.setOnSelectThirdGameListener(object : OnSelectItemListener<GameTabData?> {
            override fun onClick(select: GameTabData?) {
                rvDZAdapter.setData(select?.gameList)
                rv_tab.smoothToCenter(rvDZTabAdapter.mSelectPosition)
            }
        })
        rv_tab.adapter = rvDZTabAdapter
    }

    private fun initObserve() {
        viewModel.enterThirdGameResult.observe(viewLifecycleOwner, Observer {
            if (isVisible)
                enterThirdGame(it)
        })
    }

    private fun enterThirdGame(result: EnterThirdGameResult) {
        hideLoading()
        when (result.resultType) {
            EnterThirdGameResult.ResultType.SUCCESS -> context?.run { JumpUtil.toThirdGameWeb(this, result.url ?: "") }
            EnterThirdGameResult.ResultType.FAIL -> showErrorPromptDialog(getString(R.string.error), result.errorMsg ?: "") {}
            EnterThirdGameResult.ResultType.NEED_REGISTER ->
                requireActivity().startRegister()

            EnterThirdGameResult.ResultType.GUEST -> showErrorPromptDialog(getString(R.string.error), result.errorMsg ?: "") {}
            EnterThirdGameResult.ResultType.NONE -> {
            }
        }
        if (result.resultType != EnterThirdGameResult.ResultType.NONE)
            viewModel.clearThirdGame()
    }

}