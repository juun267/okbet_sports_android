package org.cxct.sportlottery.ui.profileCenter.taskCenter

import org.cxct.sportlottery.network.quest.info.Info
import org.cxct.sportlottery.network.quest.info.RewardType


sealed class TaskCenterDialogEvent {
    class RewardSuccess(val rewardType: RewardType?, val rewardValue: Double?) :
        TaskCenterDialogEvent()

    object RewardFail : TaskCenterDialogEvent()

    class RewardAllSuccess(val pointRewardValue: Double?, val cashRewardValue: Double?) :
        TaskCenterDialogEvent()
}

sealed class TaskCenterEvent {
    class InfoTodoBehavior(val info: Info) : TaskCenterEvent()
}
