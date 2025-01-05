package org.cxct.sportlottery.ui.profileCenter.taskCenter

import androidx.annotation.StringRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.quest.info.TaskType

data class TaskListType(
    val taskType: TaskType,
    var isSelected: Boolean = false,
    var hasAvailableRewards: Boolean = false
) {
    @get:StringRes
    val taskTypeNameStrRes: Int? by lazy {
        when (taskType) {
            TaskType.TOP_PICKS -> R.string.A028
            TaskType.BASIC -> R.string.A029
            TaskType.DAILY -> R.string.A030
            TaskType.LIMITED_TIME -> R.string.A031
        }
    }
}
