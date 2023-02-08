package org.cxct.sportlottery.ui.main.more

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_qp.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.util.JumpUtil

class QPFragment(private val gameCateData: GameCateData) : BaseFragment<MainViewModel>(MainViewModel::class) {

    private var mOnSelectThirdGameListener: OnSelectItemListener<ThirdDictValues?> = object : OnSelectItemListener<ThirdDictValues?> {
        override fun onClick(select: ThirdDictValues?) {
            loading()
            viewModel.requestEnterThirdGame(select)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_qp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycleView()
        initObserve()
    }

    private fun initRecycleView() {
        //20200226 紀錄：棋牌遊戲只會有一個 tab
        val gameList = gameCateData.tabDataList.firstOrNull()?.gameList
        val adapter = RvQPAdapter()
        adapter.setData(gameList)
        adapter.setOnSelectThirdGameListener(mOnSelectThirdGameListener)
        rv_qp.adapter = adapter
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
            EnterThirdGameResult.ResultType.FAIL -> showErrorPromptDialog(getString(R.string.error),
                result.errorMsg ?: "") {}
            EnterThirdGameResult.ResultType.NEED_REGISTER ->
                context?.startActivity(Intent(context, RegisterOkActivity::class.java))
            EnterThirdGameResult.ResultType.GUEST -> showErrorPromptDialog(getString(R.string.error),
                result.errorMsg ?: "") {}
            EnterThirdGameResult.ResultType.NONE -> {
            }
        }
        if (result.resultType != EnterThirdGameResult.ResultType.NONE)
            viewModel.clearThirdGame()
    }

}