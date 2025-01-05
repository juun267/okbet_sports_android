package org.cxct.sportlottery.ui.profileCenter.taskCenter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemTaskListTypeBinding
import org.cxct.sportlottery.view.setColors
import timber.log.Timber

const val LogTag = "[Task Center] [ListType]"

class TaskTypeAdapter(private val viewListener: TaskTypeViewListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mTaskTypeList: List<TaskListType> = listOf()

    class TaskTypeViewListener(private val onTabClick: (itemData: TaskListType) -> Unit) {
        fun onTabClick(itemData: TaskListType) = onTabClick.invoke(itemData)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setupData(list: List<TaskListType>) {
        mTaskTypeList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        TaskTypeViewHolder(
            ItemTaskListTypeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = mTaskTypeList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TaskTypeViewHolder -> {
                holder.bind(mTaskTypeList[position], viewListener)
            }
        }
    }

    inner class TaskTypeViewHolder(val binding: ItemTaskListTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemData: TaskListType, viewListener: TaskTypeViewListener) {
            binding.root.setOnClickListener {
                viewListener.onTabClick(itemData)
            }

            try {
                binding.tvTaskType.apply {
                    itemData.taskTypeNameStrRes?.let {
                        text = context.getString(it)
                    }
                }
            } catch (e: Exception) {
                Timber.e("$LogTag: ${e.message}")
            }

            binding.ivNewDot.isVisible = itemData.hasAvailableRewards

            updateTabSelectedStatus(itemData)
        }

        private fun updateTabSelectedStatus(itemData: TaskListType) {
            binding.tvTaskType.setColors(if(itemData.isSelected) R.color.color_025BE8 else R.color.color_0D2245)
            binding.ivSelectedMark.isVisible = itemData.isSelected
        }
    }
}