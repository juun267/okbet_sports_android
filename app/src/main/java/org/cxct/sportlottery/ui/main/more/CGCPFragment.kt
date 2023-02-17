package org.cxct.sportlottery.ui.main.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_cgcp.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.startRegister

class CGCPFragment(private val gameCateData: GameCateData) :
    BaseFragment<MainViewModel>(MainViewModel::class) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cgcp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBtnLottery()
        initObserve()
    }

    private fun setupBtnLottery() {
        btn_lottery.setOnClickListener {
            //20200226 紀錄：CG 彩票應該只有一筆遊戲資料
            val selectFirstGame = gameCateData.tabDataList.firstOrNull()?.gameList?.firstOrNull()
            loading()
            viewModel.requestEnterThirdGame(selectFirstGame?.thirdGameData)
        }
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
            EnterThirdGameResult.ResultType.SUCCESS -> context?.run {
                JumpUtil.toThirdGameWeb(
                    this,
                    result.url ?: ""
                )
            }
            EnterThirdGameResult.ResultType.FAIL -> showErrorPromptDialog(
                getString(R.string.error),
                result.errorMsg ?: ""
            ) {}
            EnterThirdGameResult.ResultType.NEED_REGISTER ->
                requireActivity().startRegister()

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