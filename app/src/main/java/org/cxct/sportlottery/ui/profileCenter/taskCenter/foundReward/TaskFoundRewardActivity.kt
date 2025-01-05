package org.cxct.sportlottery.ui.profileCenter.taskCenter.foundReward

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.databinding.ActivityTaskFoundRewardBinding
import org.cxct.sportlottery.network.quest.info.RewardType
import org.cxct.sportlottery.network.quest.info.rewardValueFormat
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.taskCenter.TaskCenterDialogEvent
import org.cxct.sportlottery.util.showAllowingStateLoss
import org.cxct.sportlottery.view.dialog.TaskRewardDialog

class TaskFoundRewardActivity :
    BaseActivity<TaskFoundRewardViewModel, ActivityTaskFoundRewardBinding>() {
    private val taskFoundRewardAdapter =
        TaskFoundRewardAdapter(
            viewListener = TaskFoundRewardAdapter.TaskFoundRewardViewListener(
                onClaimButtonClick = {
                    viewModel.claimTaskReward(it)
                },
                onCountDownTimerFinished = {
                    viewModel.onTaskTimeUp()
                })
        )

    private var isEmptyReward = false

    private val dialogDismissListener by lazy {
        {
            if (isEmptyReward) {
                TaskRewardEmptyDialog.newInstance(dismissListener = {
                    finish()
                }).showAllowingStateLoss(supportFragmentManager)
            }
        }
    }

    override fun pageName() = "任务中心找回任务"

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF, true)

        binding.customToolBar.setOnBackPressListener {
            onBackPressedDispatcher.onBackPressed()
        }
        initObserve()
        initRecyclerView()
        binding.tvConfirm.setOnClickListener {
            viewModel.claimAllTaskReward()
        }
    }

    private fun initRecyclerView() {
        //region 任務清單
        binding.rvFoundReward.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = taskFoundRewardAdapter
        }
        //endregion 任務清單
    }

    private fun initObserve() {
        viewModel.questInfoStateObservable()
        viewModel.foundRewardList.collectWith(lifecycleScope) {
            isEmptyReward = it.isEmpty()
            taskFoundRewardAdapter.setupData(it)
        }
        viewModel.foundRewardTotalValue.collectWith(lifecycleScope) {
            val pointValue = it?.first ?: 0.0
            val cashValue = it?.second ?: 0.0
            binding.viewRewardValue.apply {
                ivPlus.isVisible = pointValue > 0 && cashValue > 0
                tvPointValue.isVisible = pointValue > 0
                tvCashValue.isVisible = cashValue > 0
                tvPointValue.text = rewardValueFormat(RewardType.POINT, pointValue)
                tvCashValue.text = rewardValueFormat(RewardType.CASH, cashValue)
            }
        }

        //region 領取後彈窗事件
        viewModel.viewEvent.collectWith(lifecycleScope) { event ->
            when (event) {
                is TaskCenterDialogEvent.RewardSuccess -> {
                    var rewardPointValue: Double? = null
                    var rewardCashValue: Double? = null
                    when (event.rewardType) {
                        RewardType.POINT -> rewardPointValue = event.rewardValue
                        RewardType.CASH -> rewardCashValue = event.rewardValue
                        else -> {
                            //do nothing
                        }
                    }
                    TaskRewardDialog.newInstance(
                        rewardType = event.rewardType,
                        rewardPointValue = rewardPointValue,
                        rewardCashValue = rewardCashValue,
                        dismissListener = dialogDismissListener
                    ).showAllowingStateLoss(supportFragmentManager)
                }

                is TaskCenterDialogEvent.RewardAllSuccess -> TaskRewardDialog.newInstance(
                    isRewardAll = true,
                    rewardPointValue = event.pointRewardValue,
                    rewardCashValue = event.cashRewardValue,
                    dismissListener = dialogDismissListener
                ).showAllowingStateLoss(supportFragmentManager)

                TaskCenterDialogEvent.RewardFail -> TaskRewardDialog.newInstance(isFailed = true)
                    .showAllowingStateLoss(supportFragmentManager)
            }
        }
        //endregion 領取後彈窗事件
    }
}