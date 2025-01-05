package org.cxct.sportlottery.ui.profileCenter.taskCenter.foundReward

import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogTaskRewardEmptyBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel

class TaskRewardEmptyDialog : BaseDialog<BaseViewModel, DialogTaskRewardEmptyBinding>() {
    private var mDismissListener: (() -> Unit?)? = null

    init {
        setStyle(R.style.FullScreen)
    }

    companion object {
        fun newInstance(dismissListener: (() -> Unit?)? = null) = TaskRewardEmptyDialog().apply {
            arguments = Bundle().apply {
                mDismissListener = dismissListener
            }
        }
    }

    override fun onInitView() = binding.run {
        root.setOnClickListener {
            dismissAllowingStateLoss()
            mDismissListener?.invoke()
        }
        tvConfirm.setOnClickListener {
            dismissAllowingStateLoss()
            mDismissListener?.invoke()
        }
    }

}