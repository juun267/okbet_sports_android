package org.cxct.sportlottery.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cxct.sportlottery.network.quest.info.Info
import org.cxct.sportlottery.network.quest.info.QuestInfo
import org.cxct.sportlottery.network.quest.info.TaskOverallStatus
import org.cxct.sportlottery.network.quest.info.TimeType
import org.cxct.sportlottery.util.isTimeOut

object TaskCenterRepository {
    private var mIsBlocked = false
    val isBlocked: Boolean get() = mIsBlocked

    private var mQuestInfo = MutableStateFlow<QuestInfo?>(null)
    val questInfoState = mQuestInfo.asStateFlow()

    private val _taskRedDotEvent = MutableStateFlow<Boolean>(false)
    val taskRedDotEvent = _taskRedDotEvent.asStateFlow()

    /**
     * 当前用户的任务数量 未登录是null，登录后为非null
     */
    var currentTaskCount: Int? =null

    /**
     * 先檢查是否有過期的限時任務或領取時間過期的獎勵找回任務, 再更新任務資訊
     */
    suspend fun updateQuestInfo(questInfo: QuestInfo?) {
        onTaskTimeUp(questInfo)
    }

    /**
     * 更新任務資訊
     */
    private suspend fun updateClearQuestInfo(questInfo: QuestInfo?) {
        mQuestInfo.emit(questInfo)
        questInfo?.let {
            mIsBlocked = it.isBlocked
        }

        if (mIsBlocked) {
            postTaskRedDotEvent(false)
        } else {
            postTaskRedDotEvent(
                listOf(
                    questInfo?.topPicks,
                    questInfo?.basic,
                    questInfo?.daily,
                    questInfo?.limitedTime
                ).any { list ->
                    list?.any { it?.taskOverallStatusEnum == TaskOverallStatus.CLAIMABLE } ?: false
                } || !questInfo?.rewards.isNullOrEmpty())
        }
    }

    suspend fun postTaskRedDotEvent(event: Boolean) {
        _taskRedDotEvent.emit(event)
    }

    /**
     * 檢查是否有過期的限時任務或領取時間過期的獎勵找回任務
     * @param questInfo 任務資訊, 有傳入則以傳入的資料為主, 無則使用[questInfoState]
     */
    suspend fun onTaskTimeUp(
        questInfo: QuestInfo? = questInfoState.value,
        isOnlyCheckTimeUp: Boolean = false
    ) {
        val newTopPicksList = questInfo?.topPicks?.toMutableList()
        val newLimitedTimeList = questInfo?.limitedTime?.toMutableList()
        val newRewardList = questInfo?.rewards?.toMutableList()

        /**
         * 是否有精選任務被移除
         * 任務的時間類型為限時([TimeType.LIMITED_TIME])才需要判斷是否有超時
         */
        val anyTopPickRemoved =
            newTopPicksList?.removeIf {
                when (it?.timeTypeEnum) {
                    TimeType.LIMITED_TIME -> true
                    else -> false
                } && it?.endDate?.isTimeOut() == true
            } ?: false

        /**
         * 是否有限時任務被移除
         */
        val anyLimitedTimeRemoved =
            newLimitedTimeList?.removeIf { it?.endDate?.isTimeOut() == true } ?: false

        /**
         * 是否有獎勵找回被移除
         */
        val anyNewRewardRemoved =
            newRewardList?.removeIf { it?.expiredDate?.isTimeOut() == true } ?: false

        if (anyTopPickRemoved || anyLimitedTimeRemoved || anyNewRewardRemoved) {
            updateClearQuestInfo(
                questInfo?.copy(
                    topPicks = newTopPicksList,
                    limitedTime = newLimitedTimeList,
                    rewards = newRewardList
                )
            )
        } else if (!isOnlyCheckTimeUp) {
            updateClearQuestInfo(questInfo)
        }
    }

    /**
     * 更新指定任務Id的任務狀態及領取狀態
     * @return 回傳更新後的任務物件
     */
    suspend fun updateQuestStatus(
        questId: Long,
        status: Long? = null,
        deliverStatus: Long? = null
    ): Info? {
        val questInfo = questInfoState.value

        val newQuestInfo = questInfo?.copy(
            topPicks = questInfo.topPicks?.copyInfoAndUpdateStatus(questId, status, deliverStatus),
            basic = questInfo.basic?.copyInfoAndUpdateStatus(questId, status, deliverStatus),
            daily = questInfo.daily?.copyInfoAndUpdateStatus(questId, status, deliverStatus),
            limitedTime = questInfo.limitedTime?.copyInfoAndUpdateStatus(
                questId, status, deliverStatus
            )
        )
        val targetInfo = newQuestInfo?.topPicks?.firstOrNull { it?.questId == questId }
            ?: newQuestInfo?.basic?.firstOrNull { it?.questId == questId }
            ?: newQuestInfo?.daily?.firstOrNull { it?.questId == questId }
            ?: newQuestInfo?.limitedTime?.firstOrNull { it?.questId == questId }
        updateClearQuestInfo(newQuestInfo)

        return targetInfo
    }

    private fun List<Info?>.copyInfoAndUpdateStatus(
        updatedQuestId: Long,
        status: Long? = null,
        deliverStatus: Long? = null
    ): List<Info?> {
        return map {
            if (it?.questId == updatedQuestId) {
                when {
                    status != null && deliverStatus != null -> it.copy(
                        status = status,
                        deliverStatus = deliverStatus
                    )

                    status != null -> it.copy(status = status)
                    deliverStatus != null -> it.copy(deliverStatus = deliverStatus)
                    else -> it.copy()
                }
            } else {
                it?.copy()
            }
        }
    }
}