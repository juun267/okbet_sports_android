package org.cxct.sportlottery.ui.profileCenter.taskCenter

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.quest.info.DeliverStatus
import org.cxct.sportlottery.network.quest.info.Info
import org.cxct.sportlottery.network.quest.info.QuestInfo
import org.cxct.sportlottery.network.quest.info.TaskOverallStatus
import org.cxct.sportlottery.network.quest.info.TaskType
import org.cxct.sportlottery.repository.DEVICE_TYPE
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.TaskCenterRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.LogUtil

class TaskCenterViewModel(
    androidContext: Application
) : BaseViewModel(
    androidContext
) {
    private val mQuestInfo get() = TaskCenterRepository.questInfoState.value

    private var _taskTypeList = MutableStateFlow<List<TaskListType>>(listOf())
    val taskTypeList = _taskTypeList.asStateFlow()

    private var _unFinishedQuestNum = MutableStateFlow<Int?>(null)
    val unFinishedQuestNum = _unFinishedQuestNum.asStateFlow()

    private var _unFinishedLimitedTimeQuest = MutableStateFlow<Info?>(null)
    val unFinishedLimitedTimeQuest = _unFinishedLimitedTimeQuest.asStateFlow()

    private var _showTaskList =
        MutableStateFlow<Pair<TaskType, List<Info?>>>(Pair(TaskType.TOP_PICKS, listOf()))
    val showTaskList = _showTaskList.asStateFlow()

    private var _viewEvent = MutableSharedFlow<TaskCenterDialogEvent>()
    val viewEvent = _viewEvent.asSharedFlow()

    private var _foundRewardsCount = MutableStateFlow(0)
    val foundRewardsCount = _foundRewardsCount.asStateFlow()

    private val _taskCenterEvent = MutableSharedFlow<TaskCenterEvent>()
    val taskCenterEvent = _taskCenterEvent.asSharedFlow()

    //预设选中的类型
    var defaultTaskType: TaskType = TaskType.TOP_PICKS

   fun questInfoStateObservable(){
       TaskCenterRepository.questInfoState.collectWith(viewModelScope) { questInfo ->
           _foundRewardsCount.emit(questInfo?.rewards?.size ?: 0)

           questInfo?.getTaskListTypes()?.let { newTaskTypeList ->
               newTaskTypeList.forEach { taskListType ->
                   taskListType.isSelected =
                       taskTypeList.value.firstOrNull { it.isSelected }?.taskType == taskListType.taskType
               }


               if (newTaskTypeList.all { !it.isSelected }) {
                   newTaskTypeList.firstOrNull()?.let { selectedType ->
                       selectedType.isSelected = true
                       updateSortedTaskList(selectedType.taskType)
                   }
               }

               _taskTypeList.emit(newTaskTypeList)

           }

           updateEndingSoonTask(questInfo)
       }
   }

    fun getTaskDetail() {
        viewModelScope.launch {
            doNetwork {
                if (LoginRepository.isLogined()) {
                    OneBoSportApi.questService.getQuestInfo(DEVICE_TYPE)
                } else {
                    OneBoSportApi.questService.getQuestGuestInfo(DEVICE_TYPE)
                }
            }?.let { result ->
                if (result.success) {
                    TaskCenterRepository.updateQuestInfo(result.t) //暫存任務資訊以供切換任務類型使用
                    getTaskTypeList(result.t)
                    //預設顯示精選任務
                    updateSortedTaskList(defaultTaskType)
//                    _showTaskList.emit(Pair(TaskType.TOP_PICKS, sortTaskList(result.t?.topPicks)))
                    _unFinishedQuestNum.emit(result.t?.unfinished ?: 0)

                    //即將結束任務
                    updateEndingSoonTask(result.t)

                    _foundRewardsCount.emit(result.t?.rewards?.size ?: 0)
                }
            }
        }
    }

    private fun getTaskTypeList(questInfo: QuestInfo?) {
        val taskTypeList = questInfo?.getInitTaskListTypes(defaultTaskType) ?: mutableListOf()

        viewModelScope.launch {
            _taskTypeList.emit(taskTypeList)
        }
    }

    /**
     * 任務排序規則: 已完成未领取>未完成（包含审核中）>不符合资格>已完成（包含审核不通过）
     */
//    private val taskSortedMap = TaskOverallStatus.values().associateWith {
//        when (it) {
//            TaskOverallStatus.CLAIMABLE -> 0
//            TaskOverallStatus.TODO, TaskOverallStatus.IN_PROGRESS -> 1
//            TaskOverallStatus.COMPLETED, TaskOverallStatus.EXPIRED -> 2
//            TaskOverallStatus.REJECTED -> 3
//        }
//    }

    /**
     * 重新排序任務列表
     * @see taskSortedMap
     */
//    private fun sortTaskList(taskList: List<Info?>?): List<Info?> {
//        return taskList?.sortedWith(
//            nullsLast<Info>(compareBy { info -> taskSortedMap[info.taskOverallStatusEnum] }).then(
//                nullsLast(compareBy { it.sort })
//            ).then(nullsLast(compareBy { it.questId }))
//        ) ?: listOf()
//
//    }

    /**
     * 重新排序並且更新顯示的任務列表
     */
    private fun updateSortedTaskList(taskType: TaskType) {
        viewModelScope.launch {
            _showTaskList.emit(
                Pair(
                    taskType, when (taskType) {
                        TaskType.TOP_PICKS -> mQuestInfo?.topPicks
                        TaskType.BASIC -> mQuestInfo?.basic
                        TaskType.DAILY -> mQuestInfo?.daily
                        TaskType.LIMITED_TIME -> mQuestInfo?.limitedTime
                    }?: listOf()
                )
            )
        }
    }

    /**
     * 更新即將結束任務
     */
    private fun updateEndingSoonTask(questInfo: QuestInfo?) {
        //即將結束任務: 限時任務中狀態為未完成且時間最靠近者
        val endingSoon = questInfo?.limitedTime?.filter { it?.isUnFinished() ?: false }
            ?.minByOrNull { it?.endDate ?: Long.MAX_VALUE }
        viewModelScope.launch {
            _unFinishedLimitedTimeQuest.emit(endingSoon)
        }
    }

    fun selectTaskType(tab: TaskListType) {
        viewModelScope.launch {
            val newTaskTypeList = taskTypeList.value.map {
                it.copy(isSelected = it.taskType == tab.taskType)
            }
            _taskTypeList.emit(newTaskTypeList)

            updateSortedTaskList(taskType = tab.taskType)
        }
    }

    //region 加入任務
    /**
     * 加入任務API
     * 請求後回傳該任務加入後的新狀態, 並更新任務及領取狀態
     */
    fun joinTask(info: Info) {
        info.taskOverallStatusEnum == TaskOverallStatus.TODO
        if (LoginRepository.isLogined()){
            info.questId?.let { questId ->
                viewModelScope.launch {
                    doNetwork {
                        OneBoSportApi.questService.postQuestJoin(questId,DEVICE_TYPE)
                    }?.let { result ->
                        if (result.success) {
                            val updatedInfo = TaskCenterRepository.updateQuestStatus(
                                questId = questId,
                                status = result.t?.status,
                                deliverStatus = result.t?.deliverStatus
                            )
                            updatedInfo?.let { jumpLink(it) }
                        } else {
                            _taskCenterEvent.emit(TaskCenterEvent.InfoTodoBehavior(info))
                        }
                    }
                }
            }
        }else{
            viewModelScope.launch{
                jumpLink(info)
            }
        }

    }
    suspend fun jumpLink(info: Info){
        when (info.taskOverallStatusEnum) {
            TaskOverallStatus.TODO, null -> {
                _taskCenterEvent.emit(TaskCenterEvent.InfoTodoBehavior(info))
            }
            TaskOverallStatus.CLAIMABLE,
            TaskOverallStatus.IN_PROGRESS,
            TaskOverallStatus.COMPLETED,
            TaskOverallStatus.REJECTED,
            TaskOverallStatus.EXPIRED -> {
                taskTypeList.value.firstOrNull { it.isSelected }
                    ?.let { selectedType ->
                        updateSortedTaskList(selectedType.taskType)
                    }
            }
        }
    }
    //endregion 加入任務

    //region 領取獎勵
    fun claimTaskReward(info: Info) {
        info.rewardId?.let { rewardId ->
            viewModelScope.launch {
                doNetwork {
                    OneBoSportApi.questService.postClaimReward(rewardId)
                }?.let { result ->
                    if (result.success) {
                        //region 更新任務資訊為完成
                        taskTypeList.value.firstOrNull { it.isSelected }?.let { selectedType ->
                            val questInfo: QuestInfo? = when (selectedType.taskType) {
                                TaskType.TOP_PICKS -> mQuestInfo?.copy(
                                    topPicks = mQuestInfo?.topPicks?.copyInfoAndUpdateStatus(info.questId),
                                    basic = mQuestInfo?.basic?.copyInfoAndUpdateStatus(info.questId),
                                    daily = mQuestInfo?.daily?.copyInfoAndUpdateStatus(info.questId),
                                    limitedTime = mQuestInfo?.limitedTime?.copyInfoAndUpdateStatus(
                                        info.questId
                                    )
                                )

                                TaskType.BASIC -> mQuestInfo?.copy(
                                    topPicks = mQuestInfo?.topPicks?.copyInfoAndUpdateStatus(
                                        info.questId
                                    ),
                                    basic = mQuestInfo?.basic?.copyInfoAndUpdateStatus(info.questId)
                                )

                                TaskType.DAILY -> mQuestInfo?.copy(
                                    topPicks = mQuestInfo?.topPicks?.copyInfoAndUpdateStatus(
                                        info.questId
                                    ),
                                    daily = mQuestInfo?.daily?.copyInfoAndUpdateStatus(info.questId)
                                )

                                TaskType.LIMITED_TIME -> mQuestInfo?.copy(
                                    topPicks = mQuestInfo?.topPicks?.copyInfoAndUpdateStatus(info.questId),
                                    limitedTime = mQuestInfo?.limitedTime?.copyInfoAndUpdateStatus(
                                        info.questId
                                    )
                                )
                            }
                            //endregion 更新任務資訊為完成

                            TaskCenterRepository.updateQuestInfo(questInfo)
                            updateSortedTaskList(selectedType.taskType)
                        }
                        _viewEvent.emit(
                            TaskCenterDialogEvent.RewardSuccess(
                                info.rewardTypeEnum,
                                info.rewardValue
                            )
                        )
                    } else {
                        _viewEvent.emit(TaskCenterDialogEvent.RewardFail)
                    }
                }
            }
        }
    }
    //endregion 領取獎勵

    private fun List<Info?>.copyInfoAndUpdateStatus(updatedQuestId: Long?): List<Info?> {
        return map {
            if (it?.questId == updatedQuestId) {
                it?.copy(deliverStatus = DeliverStatus.COMPLETED.code)
            } else {
                it?.copy()
            }
        }
    }

    fun onTaskTimeUp() {
        getTaskDetail()
    }
}