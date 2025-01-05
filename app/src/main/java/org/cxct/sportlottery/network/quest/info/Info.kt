package org.cxct.sportlottery.network.quest.info

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.Chronometer
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.databinding.ViewTaskContentBinding
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.TaskCenterRepository
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.loginedRun
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

@KeepMembers
data class Info(
    val conditionSubType: String?, //次任务条件类别
    val conditionType: String?, //任务条件类别
    val conditionValue: String?,
    val deliverStatus: Long?, //发放状态
    val deviceType: String?,
    val endDate: Long?, //任务结束时间
    val expiredDate: Any?,
    val introduction: String?,
    val limitedGame: List<LimitedGame>?, //限定游戏
    val progressValue: String?,
    val questId: Long?,
    val questName: String?,
    val questType: String?,
    val recordId: Any?,
    val redirectType: Long?, //跳转类别
    val rewardId: Long?,
    val rewardType: Long?, //任务奖励类别
    val rewardValue: Double?,
    val showIntroduction: Int?,
    val sort: Int?,
    val startDate: Long?,
    val status: Long?, //状态
    val timeType: Long? //時間類型
) {
    /**
     * 任务条件类别Enum
     * @see ConditionType
     */
    val conditionTypeEnum: ConditionType? by lazy { ConditionType.toEnum(conditionType) }

    /**
     * 次任务条件类别Enum (F-资金)
     * 若為資訊類型的會直接回傳null
     * @see ConditionSubType
     */
    val conditionSubTypeFundEnum: ConditionSubType? by lazy {
        when (conditionTypeEnum) {
            ConditionType.INFORMATION, null -> null
            ConditionType.FUND -> ConditionSubType.toEnum(conditionSubType)
        }
    }

    /**
     * 跳转类别Enum
     * @see RedirectType
     */
    val redirectTypeEnum: RedirectType? by lazy { RedirectType.toEnum(redirectType) }

    /**
     * 任务奖励类别Enum
     * @see RewardType
     */
    val rewardTypeEnum: RewardType? = RewardType.toEnum(rewardType)

    /**
     * 状态Enum
     * @see TaskStatus
     */
    private val taskStatusEnum: TaskStatus? by lazy { TaskStatus.toEnum(status) }

    /**
     * 发放状态Enum
     */
    private val deliverStatusEnum: DeliverStatus? get() = DeliverStatus.toEnum(deliverStatus)

    /**
     * 任務總狀態Enum
     * @see TaskOverallStatus
     */
    val taskOverallStatusEnum: TaskOverallStatus
        get() = when (taskStatusEnum) {
            TaskStatus.TODO, TaskStatus.IN_PROGRESS, null -> TaskOverallStatus.TODO
            TaskStatus.COMPLETED -> {
                when (deliverStatusEnum) {
                    DeliverStatus.PENDING, null -> TaskOverallStatus.IN_PROGRESS
                    DeliverStatus.CLAIMABLE -> TaskOverallStatus.CLAIMABLE
                    DeliverStatus.DELIVERING, DeliverStatus.COMPLETED -> TaskOverallStatus.COMPLETED
                    DeliverStatus.REJECTED -> TaskOverallStatus.REJECTED
                    DeliverStatus.EXPIRED -> TaskOverallStatus.EXPIRED
                }
            }
        }

    val timeTypeEnum: TimeType? by lazy { TimeType.toEnum(timeType) }

    fun isUnFinished(): Boolean = taskOverallStatusEnum != TaskOverallStatus.COMPLETED
}

//region View Builder
fun Info?.setupWithViewTaskContent(
    binding: ViewTaskContentBinding,
    viewListener: TaskInfoViewListener,
    showCountDownTimer: Boolean = true
) {
    val info = this
    binding.apply {
        //region 任務類型圖示
        ivTaskType.setImageResource(
            when (info?.rewardTypeEnum) {
                RewardType.POINT -> R.drawable.ic_task_type_point
                RewardType.CASH, null -> R.drawable.ic_task_type_cash
            }
        )
        //endregion 任務類型圖示
        //region 任務名稱 + 任務獎勵簡介
        tvTaskName.text = info?.questName
        ivTaskInfoDetail.isVisible = info?.showIntroduction == 1
        ivTaskInfoDetail.setOnClickListener {
            info?.let {
                viewListener.onClickTaskIntroduction(it)
            }
        }
        //endregion 任務名稱 + 任務獎勵簡介

        //region 進度 => 資金類型 = 幣種+進度值/幣種+條件達成值(百分比), 資訊完善類型 = 0/1
        tvProgress.apply {
            text = when (info?.conditionTypeEnum) {
                ConditionType.FUND -> getFundProgress(context, info)
                ConditionType.INFORMATION, null -> getCommonProgress(context, info)
            }
        }
        //endregion 進度 => 資金類型 = 幣種+進度值/幣種+條件達成值(百分比), 資訊完善類型 = 0/1

        //region 任務獎勵 = 任務獎勵類別圖示 + 任務獎勵幣別符號 + 任務獎勵金額
        ivTaskValueType.setImageResource(
            when (info?.rewardTypeEnum) {
                RewardType.POINT -> R.drawable.ic_task_value_point
                RewardType.CASH, null -> R.drawable.ic_task_value_cash
            }
        )
        tvTaskValue.apply {
            setTextColor(
                ContextCompat.getColor(
                    context, when (info?.rewardTypeEnum) {
                        RewardType.POINT -> R.color.color_764FF5
                        RewardType.CASH, null -> R.color.color_FF6533
                    }
                )
            )
            text = rewardValueFormat(info?.rewardTypeEnum, info?.rewardValue)
        }
        //endregion 任務獎勵 = 任務獎勵類別圖示 + 任務獎勵幣別符號 + 任務獎勵金額

        //region 任務按鈕
        btnFeature.apply {
            setOnClickListener {
                    if (TaskCenterRepository.isBlocked) return@setOnClickListener
                    when (info?.taskOverallStatusEnum) {
                        TaskOverallStatus.TODO -> {
                            viewListener.onToDoButtonClick(info)
                        }

                        TaskOverallStatus.CLAIMABLE -> {
                            viewListener.onClaimButtonClick(info)
                        }

                        else -> {
                            //do nothing
                        }
                    }

            }
            background = ContextCompat.getDrawable(
                context, when (info?.taskOverallStatusEnum) {
                    TaskOverallStatus.TODO, null -> if (TaskCenterRepository.isBlocked) R.drawable.img_task_not_pass_button else R.drawable.img_task_go_finish_button
                    TaskOverallStatus.CLAIMABLE -> if (TaskCenterRepository.isBlocked) R.drawable.img_task_not_pass_button else R.drawable.img_task_claim_button
                    TaskOverallStatus.IN_PROGRESS, TaskOverallStatus.COMPLETED, TaskOverallStatus.REJECTED, TaskOverallStatus.EXPIRED -> R.drawable.img_task_not_pass_button
                }
            )
        }

        tvBtnContent.apply {
            setTextColor(
                when (info?.taskOverallStatusEnum) {
                    TaskOverallStatus.TODO, null -> if (TaskCenterRepository.isBlocked) {
                        ContextCompat.getColor(context, R.color.color_9DABC9)
                    } else {
                        ContextCompat.getColor(context, R.color.color_FFFFFF)
                    }

                    TaskOverallStatus.CLAIMABLE -> if (TaskCenterRepository.isBlocked) {
                        ContextCompat.getColor(context, R.color.color_9DABC9)
                    } else {
                        ContextCompat.getColor(context, R.color.color_FFFFFF)
                    }

                    TaskOverallStatus.IN_PROGRESS, TaskOverallStatus.COMPLETED, TaskOverallStatus.REJECTED, TaskOverallStatus.EXPIRED -> ContextCompat.getColor(
                        context,
                        R.color.color_9DABC9
                    )
                }
            )
            text = when (info?.taskOverallStatusEnum) {
                TaskOverallStatus.TODO, null -> context.getString(R.string.A037)
                TaskOverallStatus.CLAIMABLE -> context.getString(R.string.A038)
                TaskOverallStatus.IN_PROGRESS -> context.getString(R.string.B176)
                TaskOverallStatus.COMPLETED -> context.getString(R.string.A040)
                TaskOverallStatus.REJECTED -> context.getString(R.string.A042)
                TaskOverallStatus.EXPIRED -> context.getString(R.string.B174)
            }
        }

        ivBtnArrow.isVisible = when (info?.taskOverallStatusEnum) {
            TaskOverallStatus.TODO, null -> true
            TaskOverallStatus.CLAIMABLE,
            TaskOverallStatus.IN_PROGRESS,
            TaskOverallStatus.COMPLETED,
            TaskOverallStatus.REJECTED,
            TaskOverallStatus.EXPIRED -> false
        }
        //時間倒數計時
        info?.setupTaskCountDownTimer(ivClock, cmEndDate, {
            viewListener.onCountDownTimerFinished(info.questId)
        }, showCountDownTimer)
    }
}

fun Info?.setupTaskCountDownTimer(
    ivClock: ImageView,
    cmEndDate: Chronometer,
    onChronometerFinished: () -> Unit,
    showCountDownTimer: Boolean = true
) {
    setupTaskCountDownTimer(
        this?.endDate,
        ivClock,
        cmEndDate,
        onChronometerFinished,
        showCountDownTimer
    )
}

private fun formatTime(millis: Long): String {
    val hours = (millis / (1000 * 60 * 60))
    val minutes = (millis / (1000 * 60)) % 60
    val seconds = (millis / 1000) % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

fun setupTaskCountDownTimer(
    endDate: Long?,
    ivClock: ImageView,
    cmEndDate: Chronometer,
    onChronometerFinished: () -> Unit,
    showCountDownTimer: Boolean = true,
    showBeforeTimeStarted: Boolean = false,
    startDate: Long? = null
) {
    //region 時間倒數計時
    if (showCountDownTimer) {
        endDate?.let {
            if (cmEndDate.onChronometerTickListener==null){
                cmEndDate.onChronometerTickListener = object : Chronometer.OnChronometerTickListener {
                    //endregion
                    @SuppressLint("SetTextI18n")
                    override fun onChronometerTick(chronometer: Chronometer?) {
                        endDate.let { baseTime ->
                            val elapsedMillis = baseTime - System.currentTimeMillis()
                            val currentTime = formatTime(elapsedMillis)
                            if (elapsedMillis <= 0) {
                                cmEndDate.stop()
                                ivClock.isVisible = false
                                cmEndDate.isVisible = false
                                onChronometerFinished.invoke()
                            }
                            //畫面呈現最大為999:60:60, 即為時間差超過3600000000微秒
                            else if (elapsedMillis > 3600000000) {
                                chronometer?.text = "999:60:60"
                                ivClock.isVisible = true
                                cmEndDate.isVisible = true
                            } else {
                                chronometer?.text = currentTime // 設定 Chronometer 的文字
                                ivClock.isVisible = true
                                cmEndDate.isVisible = true
                            }

                            Timber.d("[Chronometer] currentTime: $currentTime, elapsedMillis: $elapsedMillis")
                        }
                    }
                }
            }
            //初始化时间的时候，先隐藏避免base后格式有问题，在onChronometerTick里面在显示出来
            cmEndDate.isVisible = false
            cmEndDate.stop()
            cmEndDate.base = it
            cmEndDate.isCountDown = true
            cmEndDate.start()
        } ?: run {
            ivClock.isVisible = false
            cmEndDate.isVisible = false
            cmEndDate.stop()
            cmEndDate.onChronometerTickListener = null
        }
    } else {
        ivClock.isVisible = false
        cmEndDate.isVisible = false
        cmEndDate.stop()
        cmEndDate.onChronometerTickListener = null
    }
    //endregion 時間倒數計時
}

private fun getFundProgress(context: Context, info: Info?): SpannableString {
    val progressValueBigDecimal =
        (info?.progressValue ?: "0").toBigDecimalOrNull() ?: BigDecimal.ZERO
    val progressString = "$showCurrencySign${TextUtil.formatMoney(progressValueBigDecimal,0)}"

    val conditionValueBigDecimal =
        (info?.conditionValue ?: "1").toBigDecimalOrNull() ?: BigDecimal.ONE
    val finishedPercentage = "(${
        if (conditionValueBigDecimal > BigDecimal.ZERO) {
            TextUtil.formatMoney2(progressValueBigDecimal.multiply(BigDecimal.valueOf(100)).divide(conditionValueBigDecimal, 1, RoundingMode.DOWN))
        } else {
            "100"
        }
    }%)"
    val conditionString =
        "/$showCurrencySign${TextUtil.formatMoney(conditionValueBigDecimal,0)}$finishedPercentage"

    val fundProgressSpannableString = SpannableString(progressString + conditionString)

    // 設定 progressString 的字體粗細為 600
    fundProgressSpannableString.setSpan(
        StyleSpan(Typeface.BOLD),
        0,
        progressString.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    fundProgressSpannableString.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_0D2245)),
        0,
        progressString.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    // 設定 conditionString 的字體粗細為 400
    fundProgressSpannableString.setSpan(
        StyleSpan(Typeface.NORMAL),
        progressString.length,
        fundProgressSpannableString.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    fundProgressSpannableString.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_6D7693)),
        progressString.length,
        fundProgressSpannableString.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    return fundProgressSpannableString
}

private fun getCommonProgress(context: Context, info: Info?): SpannableString {
    val taskStatusString = "${
        when (info?.taskOverallStatusEnum) {
            TaskOverallStatus.TODO, null -> 0
            TaskOverallStatus.CLAIMABLE,
            TaskOverallStatus.IN_PROGRESS,
            TaskOverallStatus.COMPLETED,
            TaskOverallStatus.REJECTED,
            TaskOverallStatus.EXPIRED -> 1
        }
    }/1"

    val spannableString = SpannableString(taskStatusString)
    val slashIndex = taskStatusString.indexOf('/')
    if (slashIndex != -1) {
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            slashIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_0D2245)),
            0,
            slashIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_6D7693)),
            slashIndex,
            taskStatusString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    return spannableString
}

fun rewardValueFormat(rewardType: RewardType?, rewardValue: Double?): String {
    return when (rewardType) {
        RewardType.POINT -> "+${TextUtil.formatMoney(rewardValue?:0, numAfterDot = 0)}"
        RewardType.CASH, null -> {
            showCurrencySign + TextUtil.formatMoney2(rewardValue ?: 0)
        }
    }
}

class TaskInfoViewListener(
    private val onToDoButtonClick: (info: Info) -> Unit,
    private val onClaimButtonClick: (info: Info) -> Unit,
    private val onCountDownTimerFinished: (questId: Long?) -> Unit,
    private val onClickTaskIntroduction: (info: Info) -> Unit
) {
    fun onClickTaskIntroduction(info: Info) = onClickTaskIntroduction.invoke(info)
    fun onToDoButtonClick(info: Info) = onToDoButtonClick.invoke(info)
    fun onClaimButtonClick(info: Info) = onClaimButtonClick.invoke(info)
    fun onCountDownTimerFinished(questId: Long?) = onCountDownTimerFinished.invoke(questId)
}

//endregion