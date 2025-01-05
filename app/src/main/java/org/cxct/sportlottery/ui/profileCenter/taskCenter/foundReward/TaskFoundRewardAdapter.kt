package org.cxct.sportlottery.ui.profileCenter.taskCenter.foundReward

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemTaskFoundRewardContentBinding
import org.cxct.sportlottery.network.quest.info.Reward
import org.cxct.sportlottery.network.quest.info.RewardType
import org.cxct.sportlottery.network.quest.info.rewardValueFormat
import org.cxct.sportlottery.network.quest.info.setupTaskCountDownTimer

class TaskFoundRewardAdapter(private val viewListener: TaskFoundRewardViewListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mDataList: List<Reward?> = listOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setupData(dataList: List<Reward?>) {
        mDataList = dataList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TaskFoundRewardViewHolder(
            ItemTaskFoundRewardContentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TaskFoundRewardViewHolder -> {
                holder.bind(mDataList[position], viewListener)
            }
        }
    }

    inner class TaskFoundRewardViewHolder(val binding: ItemTaskFoundRewardContentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reward: Reward?, viewListener: TaskFoundRewardViewListener) {
            binding.viewInfo.apply {
                //region 任務類型圖示
                ivTaskType.setImageResource(
                    when (reward?.rewardTypeEnum) {
                        RewardType.POINT -> R.drawable.ic_task_type_point
                        RewardType.CASH, null -> R.drawable.ic_task_type_cash
                    }
                )
                //endregion 任務類型圖示

                //region 任務名稱
                tvTaskName.text = reward?.questName
                //endregion 任務名稱

                //region 任務獎勵 = 任務獎勵類別圖示 + 任務獎勵幣別符號 + 任務獎勵金額
                ivTaskValueType.setImageResource(
                    when (reward?.rewardTypeEnum) {
                        RewardType.POINT -> R.drawable.ic_task_value_point
                        RewardType.CASH, null -> R.drawable.ic_task_value_cash
                    }
                )
                tvTaskValue.apply {
                    setTextColor(
                        ContextCompat.getColor(
                            context, when (reward?.rewardTypeEnum) {
                                RewardType.POINT -> R.color.color_764FF5
                                RewardType.CASH, null -> R.color.color_FF6533
                            }
                        )
                    )
                    text = rewardValueFormat(reward?.rewardTypeEnum, reward?.rewardValue)
                }
                //endregion 任務獎勵 = 任務獎勵類別圖示 + 任務獎勵幣別符號 + 任務獎勵金額

                setupTaskCountDownTimer(
                    endDate = reward?.expiredDate,
                    ivClock = ivClock,
                    cmEndDate = cmEndDate,
                    onChronometerFinished = {
                        viewListener.onCountDownTimerFinished(reward?.questId)
                    },
                    showCountDownTimer = true
                )

                btnFeature.setOnClickListener {
                    reward?.let { rewardNotNull ->
                        viewListener.onClaimButtonClick(rewardNotNull)
                    }
                }
            }
        }
    }

    class TaskFoundRewardViewListener(
        private val onClaimButtonClick: (reward: Reward) -> Unit,
        private val onCountDownTimerFinished: (questId: Long?) -> Unit
    ) {
        fun onClaimButtonClick(reward: Reward) = onClaimButtonClick.invoke(reward)
        fun onCountDownTimerFinished(questId: Long?) = onCountDownTimerFinished.invoke(questId)
    }
}