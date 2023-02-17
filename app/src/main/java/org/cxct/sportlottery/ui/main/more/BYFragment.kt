package org.cxct.sportlottery.ui.main.more

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_by.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.MetricsUtil
import org.cxct.sportlottery.util.startRegister


class BYFragment(private val gameCateData: GameCateData) :
    BaseFragment<MainViewModel>(MainViewModel::class) {

    private var mOnSelectThirdGameListener: OnSelectItemListener<ThirdDictValues?> =
        object : OnSelectItemListener<ThirdDictValues?> {
            override fun onClick(select: ThirdDictValues?) {
                loading()
                viewModel.requestEnterThirdGame(select)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_by, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecycleView()
        initObserve()
    }

    private fun initRecycleView() {
        //20200226 紀錄：捕魚遊戲只會有一個 tab
        val gameList = gameCateData.tabDataList.firstOrNull()?.gameList
        val adapter = RvBYAdapter()
        adapter.setData(gameList)
        adapter.setOnSelectThirdGameListener(mOnSelectThirdGameListener)
        rv_by.adapter = adapter
        rv_by.addItemDecoration(mItemDecoration)
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

    //設定recycleView item 向上重疊高度
    private val mItemDecoration = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position > 1) outRect.top = MetricsUtil.convertDpToPixel(-86f, context).toInt()
        }
    }
}