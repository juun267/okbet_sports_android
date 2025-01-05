package org.cxct.sportlottery.ui.profileCenter.taskCenter.foundReward

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.quest.claimAllReward.ClaimAllRewardRequest
import org.cxct.sportlottery.network.quest.info.QuestInfo
import org.cxct.sportlottery.network.quest.info.Reward
import org.cxct.sportlottery.network.quest.info.RewardType
import org.cxct.sportlottery.repository.TaskCenterRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.profileCenter.taskCenter.TaskCenterDialogEvent
import org.cxct.sportlottery.util.LogUtil

class TaskFoundRewardViewModel(
    androidContext: Application
) : BaseViewModel(
    androidContext
) {
    private val _foundRewardList = MutableStateFlow<List<Reward?>>(listOf())
    val foundRewardList = _foundRewardList.asStateFlow()

    private var _viewEvent = MutableSharedFlow<TaskCenterDialogEvent>()
    val viewEvent = _viewEvent.asSharedFlow()

    private var _foundRewardTotalValue = MutableStateFlow<Pair<Double?, Double?>?>(null)
    val foundRewardTotalValue = _foundRewardTotalValue.asStateFlow()

    fun questInfoStateObservable(){
        TaskCenterRepository.questInfoState.collectWith(viewModelScope) { questInfo ->
            (questInfo?.rewards ?: listOf()).let { rewardList ->
                _foundRewardTotalValue.emit(getRewardValues(rewardList))
                _foundRewardList.emit(rewardList)
            }
        }
    }

    fun claimTaskReward(reward: Reward) {
        reward.rewardId?.let { rewardId ->
            viewModelScope.launch {
                doNetwork {
                    OneBoSportApi.questService.postClaimReward(rewardId)
                }?.let { result ->
                    if (result.success) {
                        val questInfo: QuestInfo? =
                            TaskCenterRepository.questInfoState.value?.copy()
                        val newRewards = questInfo?.rewards?.toMutableList()
                        newRewards?.removeIf { it?.rewardId == rewardId }
                        questInfo?.rewards = newRewards
                        TaskCenterRepository.updateQuestInfo(questInfo)

                        _viewEvent.emit(
                            TaskCenterDialogEvent.RewardSuccess(
                                reward.rewardTypeEnum,
                                reward.rewardValue
                            )
                        )
                    } else {
                        _viewEvent.emit(TaskCenterDialogEvent.RewardFail)
                    }
                }
            }
        }
    }

    fun claimAllTaskReward() {
        TaskCenterRepository.questInfoState.value?.rewards?.let { rewardList ->
            rewardList.mapNotNull { it?.rewardId }.let { rewardIdList ->
                viewModelScope.launch {
                    doNetwork {
                        OneBoSportApi.questService.postClaimAllReward(
                            ClaimAllRewardRequest(
                                rewardIdList
                            )
                        )
                    }?.let { result ->
                        if (result.success && !result.t?.successRewardIds.isNullOrEmpty()) {
                            val questInfo: QuestInfo? =
                                TaskCenterRepository.questInfoState.value?.copy()

                            /**
                             * 新找回獎勵陣列物件
                             */
                            val newRewardList =
                                questInfo?.rewards?.map { it?.copy() }?.toMutableList()

                            /**
                             * 已領取獎勵
                             */
                            val rewardedList = mutableListOf<Reward>()

                            //根據成功領取的獎勵ID移除陣列, 並加入已領取獎勵清單以計算彈窗顯示領取金額
                            result.t?.successRewardIds?.forEach { successRewardId ->
                                newRewardList?.firstOrNull { it?.rewardId == successRewardId }
                                    ?.let { targetReward ->
                                        newRewardList.remove(targetReward)
                                        rewardedList.add(targetReward)
                                    }
                            }

                            questInfo?.rewards = newRewardList
                            TaskCenterRepository.updateQuestInfo(questInfo)

                            newRewardList?.let {
                                val totalRewardValue = getRewardValues(rewardedList)
                                _viewEvent.emit(
                                    TaskCenterDialogEvent.RewardAllSuccess(
                                        pointRewardValue = totalRewardValue.first,
                                        cashRewardValue = totalRewardValue.second
                                    )
                                )
                            }
                        } else {
                            _viewEvent.emit(TaskCenterDialogEvent.RewardFail)
                        }
                    }
                }
            }
        }

    }

    /**
     * @return Pair(積分領取總額, 現金領取總額)
     */
    private fun getRewardValues(rewardList: List<Reward?>): Pair<Double?, Double?> {
        var pointValue = 0.0
        var cashValue = 0.0
        rewardList.forEach { reward ->
            reward?.rewardTypeEnum?.let { rewardTypeEnum ->
                when (rewardTypeEnum) {
                    RewardType.POINT -> {
                        pointValue += reward.rewardValue ?: 0.0
                    }

                    RewardType.CASH -> {
                        cashValue += reward.rewardValue ?: 0.0
                    }
                }
            }
        }

        return Pair(pointValue, cashValue)
    }

    fun onTaskTimeUp() {
        viewModelScope.launch {
            TaskCenterRepository.onTaskTimeUp(isOnlyCheckTimeUp = true)
        }
    }
}