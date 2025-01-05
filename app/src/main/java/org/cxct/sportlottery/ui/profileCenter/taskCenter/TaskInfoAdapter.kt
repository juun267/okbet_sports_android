package org.cxct.sportlottery.ui.profileCenter.taskCenter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.ItemTaskContentBinding
import org.cxct.sportlottery.network.quest.info.Info
import org.cxct.sportlottery.network.quest.info.TaskInfoViewListener
import org.cxct.sportlottery.network.quest.info.TaskType
import org.cxct.sportlottery.network.quest.info.TimeType
import org.cxct.sportlottery.network.quest.info.setupWithViewTaskContent

class TaskInfoAdapter(private val viewListener: TaskInfoViewListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mTaskType = TaskType.TOP_PICKS
    private var mDataList: List<Info?> = listOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setupData(taskType: TaskType, dataList: List<Info?>) {
        mTaskType = taskType
        mDataList = dataList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TaskInfoViewHolder(
            ItemTaskContentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mDataList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TaskInfoViewHolder -> {
                holder.bind(mDataList[position], viewListener)
            }
        }
    }

    inner class TaskInfoViewHolder(val binding: ItemTaskContentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(info: Info?, viewListener: TaskInfoViewListener) {
            info.setupWithViewTaskContent(
                binding = binding.viewInfo,
                viewListener = viewListener,
                showCountDownTimer = when (info?.timeTypeEnum) {
                    TimeType.LIMITED_TIME -> true
                    TimeType.PERMANENT, TimeType.DAILY, null -> false
                }
            )
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is TaskInfoViewHolder -> {
                holder.binding.viewInfo.cmEndDate.apply {
                    stop()
                    onChronometerTickListener = null
                }
            }
        }

        super.onViewRecycled(holder)
    }
}