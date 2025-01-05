package org.cxct.sportlottery.view.dialog

import android.os.Build
import android.os.Bundle
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogTaskRewardBinding
import org.cxct.sportlottery.network.quest.info.RewardType
import org.cxct.sportlottery.network.quest.info.rewardValueFormat
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import timber.log.Timber

private const val ARG_REWARD_TYPE = "arg_reward_type"
private const val ARG_IS_REWARD_ALL_TYPE = "arg_is_reward_all_type"
private const val ARG_IS_FAILED = "arg_is_failed"
private const val ARG_CASH_VALUE = "arg_cash_value"
private const val ARG_POINT_VALUE = "arg_point_value"

class TaskRewardDialog : BaseDialog<BaseViewModel, DialogTaskRewardBinding>() {

    @Suppress("DEPRECATION")
    private val rewardType: RewardType?
        get() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arguments?.getSerializable(ARG_REWARD_TYPE, RewardType::class.java)
            } else {
                arguments?.getSerializable(ARG_REWARD_TYPE) as? RewardType
            }

    private val isRewardAll: Boolean get() = arguments?.getBoolean(ARG_IS_REWARD_ALL_TYPE) ?: false

    private val cashValue: Double get() = arguments?.getDouble(ARG_CASH_VALUE) ?: 0.0

    private val pointValue: Double get() = arguments?.getDouble(ARG_POINT_VALUE) ?: 0.0

    private val isFailed: Boolean get() = arguments?.getBoolean(ARG_IS_FAILED) ?: false

    private var mDismissListener: (() -> Unit?)? = null

    init {
        setStyle(R.style.FullScreen)
    }

    companion object {
        fun newInstance(
            rewardType: RewardType? = null,
            isRewardAll: Boolean = false,
            rewardCashValue: Double? = 0.0,
            rewardPointValue: Double? = 0.0,
            isFailed: Boolean = false,
            dismissListener: (() -> Unit?)? = null
        ) = TaskRewardDialog().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_REWARD_TYPE, rewardType)
                putBoolean(ARG_IS_REWARD_ALL_TYPE, isRewardAll)
                putBoolean(ARG_IS_FAILED, isFailed)
                rewardCashValue?.let {
                    putDouble(ARG_CASH_VALUE, it)
                }
                rewardPointValue?.let {
                    putDouble(ARG_POINT_VALUE, it)
                }
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

        when {
            isFailed -> {
                binding.linBg.setBackgroundResource(R.drawable.bg_task_reward_fail)
                binding.ivType.setImageResource(R.drawable.ic_task_reward_type_fail)
                binding.ivTypeShadow.setImageResource(R.drawable.ic_task_reward_fail_shadow)
                binding.tvTipsContent.text = getString(R.string.A048)
                binding.viewRewardValue.let {
                    it.ivPlus.isVisible = false
                    it.tvPointValue.isVisible = false
                    it.tvCashValue.isVisible = false
                }
            }

            isRewardAll -> {
                binding.linBg.setBackgroundResource(R.drawable.bg_task_reward_success)
                binding.ivType.setImageResource(
                    when {
                        pointValue > 0 && cashValue > 0 -> R.drawable.ic_task_reward_type_both
                        pointValue > 0 -> R.drawable.ic_task_reward_type_point
                        else -> R.drawable.ic_task_reward_type_cash
                    }
                )
                binding.ivTypeShadow.setImageResource(
                    when {
                        pointValue > 0 && cashValue > 0 -> R.drawable.ic_task_reward_point_shadow
                        pointValue > 0 -> R.drawable.ic_task_reward_point_shadow
                        else -> R.drawable.ic_task_reward_cash_shadow
                    }
                )
                binding.tvTipsContent.text = getString(R.string.A047)
                binding.viewRewardValue.let {
                    it.ivPlus.isVisible = pointValue > 0 && cashValue > 0
                    it.tvPointValue.isVisible = pointValue > 0
                    it.tvCashValue.isVisible = cashValue > 0
                    it.tvPointValue.text = rewardValueFormat(RewardType.POINT, pointValue)
                    it.tvCashValue.text = rewardValueFormat(RewardType.CASH, cashValue)
                }
            }

            else -> {
                binding.linBg.setBackgroundResource(R.drawable.bg_task_reward_success)
                binding.viewRewardValue.ivPlus.isVisible = false
                binding.tvTipsContent.text = getString(R.string.A047)
                when (rewardType) {
                    RewardType.POINT -> {
                        binding.ivType.setImageResource(R.drawable.ic_task_reward_type_point)
                        binding.ivTypeShadow.setImageResource(R.drawable.ic_task_reward_point_shadow)
                        binding.viewRewardValue.let {
                            it.tvPointValue.isVisible = true
                            it.tvPointValue.text = rewardValueFormat(RewardType.POINT, pointValue)
                            it.tvCashValue.isVisible = false
                        }
                    }

                    RewardType.CASH -> {
                        binding.ivType.setImageResource(R.drawable.ic_task_reward_type_cash)
                        binding.ivTypeShadow.setImageResource(R.drawable.ic_task_reward_cash_shadow)
                        binding.viewRewardValue.let {
                            it.tvPointValue.isVisible = false
                            it.tvCashValue.isVisible = true
                            it.tvCashValue.text = rewardValueFormat(RewardType.CASH, cashValue)
                        }
                    }

                    null -> {
                        Timber.d("[Task Reward Dialog] 沒有匹配的獎勵類型")
                    }
                }
            }
        }
    }

}
