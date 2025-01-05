package org.cxct.sportlottery.network.quest.info

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.ui.profileCenter.taskCenter.TaskListType

@KeepMembers
data class QuestInfo(
    val basic: List<Info?>?,
    val daily: List<Info?>?,
    val limitedTime: List<Info?>?,
    var rewards: List<Reward?>?,
    val topPicks: List<Info?>?,
    val unfinished: Int?,
    val blocked: Long? //是否黑名單 0:否, 1:是
) {
    /**
     * 獲取初始化的任務類型清單, 預設選中精選任務
     */
    fun getInitTaskListTypes(defaultTaskType: TaskType=TaskType.TOP_PICKS): List<TaskListType> {
        return getTaskListTypes().onEach {
            it.isSelected = it.taskType == defaultTaskType //預設選中精選任務
        }
    }

    fun getTaskListTypes():List<TaskListType>{
        val taskTypeList = mutableListOf<TaskListType>()
        mapOf(
            topPicks to TaskType.TOP_PICKS,
            basic to TaskType.BASIC,
            daily to TaskType.DAILY,
            limitedTime to TaskType.LIMITED_TIME
        ).forEach { (taskInfoList, taskType) ->
            if (taskInfoList?.isEmpty() == false) {
                taskTypeList.add(TaskListType(taskType = taskType).apply {

                    hasAvailableRewards = if (isBlocked) {
                        false
                    } else {
                        taskInfoList.any {
                            when (it?.taskOverallStatusEnum) {
                                TaskOverallStatus.CLAIMABLE -> true
                                else -> false
                            }
                        }
                    }
                })
            }
        }
        return taskTypeList
    }
    fun getTaskClaimRedDot(): Boolean{
        return getTaskListTypes().any { it.hasAvailableRewards }
    }

    /**
     * 获取当前用户任务数量
     */
    fun getTaskCount():Int{
       return listOf(topPicks,basic,daily,limitedTime).sumOf { it?.size?:0 }
    }

    val isBlocked: Boolean = blocked == 1L
}