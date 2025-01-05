package org.cxct.sportlottery.ui.profileCenter.taskCenter

import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.util.TypedValue
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityTaskCenterBinding
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.quest.info.*
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.repository.TaskCenterRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.chat.ChatActivity
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.profileCenter.taskCenter.foundReward.TaskFoundRewardActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.dialog.TaskRewardDialog
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import splitties.activities.start
import timber.log.Timber


class TaskCenterActivity :
    BaseActivity<TaskCenterViewModel, ActivityTaskCenterBinding>() {
    override fun pageName() = "任务中心"

    private val taskTypeAdapter = TaskTypeAdapter(viewListener = TaskTypeAdapter.TaskTypeViewListener {
        scrollCenterTaskType(it)
        it.taskType.setupDailyTipsVisible()
        viewModel.selectTaskType(it)
    })

    private val taskInfoViewListener = TaskInfoViewListener(onClickTaskIntroduction = {
        TaskCommonDialog.newInstance(
            title = getString(R.string.A046), content = it.introduction ?: ""
        ).showAllowingStateLoss(supportFragmentManager)
    }, onToDoButtonClick = { info ->
        viewModel.joinTask(info)
    }, onClaimButtonClick = { info ->
        viewModel.claimTaskReward(info)
    }, onCountDownTimerFinished = {
        viewModel.onTaskTimeUp()
    })

    private val taskInfoAdapter: TaskInfoAdapter =
        TaskInfoAdapter(viewListener = taskInfoViewListener)

    private fun RedirectType.navigatePage() {
        when (this) {
            RedirectType.HOME -> {
                finish()
                MainTabActivity.activityInstance?.backMainHome()
            }

            RedirectType.HALL,RedirectType.NONE -> {
                finish()
                MainTabActivity.activityInstance?.jumpToTheSport()
            }

            RedirectType.RECHARGE ->
                loginedRun(this@TaskCenterActivity,true){
                    jumpToDeposit("任务中心，充值任务")
                }
            RedirectType.REGISTER ->
                if (LoginRepository.isLogined()){ //登录后，注册页面重定向至主页
                    finish()
                    MainTabActivity.activityInstance?.backMainHome()
                }else{
                    LoginOKActivity.startRegist(this@TaskCenterActivity)
                }
            RedirectType.PERSONAL_INFORMATION -> loginedRun(this@TaskCenterActivity,true){
                startActivity<ProfileActivity>()
            }

            RedirectType.KYC -> {
                jumpToKYC()
            }
            RedirectType.MAYA -> {
                runWithCatch{
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.MAYA_APP_LINK)))
                }

            }
            RedirectType.GLIFE -> {
                runWithCatch{
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GCASH_APP_LINK)))
                }
            }
        }
    }

    private var mFoundRewardCount: Int = 0
    private var mSelectedTaskType: TaskType? = null

    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF, true)
        binding.customToolBar.binding.ivToolbarEnd.setOnClickListener {
            TaskCommonDialog.newInstance(
                title = getString(R.string.A033),
                content = getString(R.string.A034)
            ).showAllowingStateLoss(supportFragmentManager)
        }
        binding.customToolBar.setOnBackPressListener {
            onBackPressedDispatcher.onBackPressed()
        }
        initRecyclerView()
        initObserve()

        binding.ivFloatingReward.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    TaskFoundRewardActivity::class.java
                )
            )
        }
    }

    private fun initRecyclerView() {
        //region 任務類別清單
        binding.rvTaskType.apply {
            layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = taskTypeAdapter
        }
        //endregion 任務類別清單

        //region 任務清單
        binding.rvTaskInfo.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = taskInfoAdapter
        }
        //endregion 任務清單
    }

    override fun onInitData() {
        super.onInitData()
        viewModel.defaultTaskType = getDefaultTaskType(intent)
        viewModel.getTaskDetail()
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            viewModel.defaultTaskType = getDefaultTaskType(it)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.viewLimitedInfo.cmEndDate.stop()
        binding.cmTopEndDate.stop()
    }
    /**
     *  从任务页面回到任务中心，需要刷新一下任务数据
     */
    var isFirstResume =true
    override fun onResume() {
        super.onResume()
//        binding.viewLimitedInfo.cmEndDate.start()
//        binding.cmTopEndDate.start()
        if (isFirstResume){
            isFirstResume = false
        }else{
            viewModel.getTaskDetail()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewLimitedInfo.cmEndDate.onChronometerTickListener = null //移除監聽器
        binding.cmTopEndDate.onChronometerTickListener = null
    }

    private fun initObserve() {
        viewModel.questInfoStateObservable()
        //region 任務未完成個數更新
        viewModel.unFinishedQuestNum.collectWith(lifecycleScope) {
            it?.let { unFinishedQuestNumStr ->
                if (unFinishedQuestNumStr > 0) {
                    binding.ivTipsHead.setImageResource(R.drawable.ic_task_tips_head)
                    binding.ivTipsContent.setImageResource(R.drawable.ic_task_tips_content)
                    binding.ivTipsTail.setImageResource(R.drawable.ic_task_tips_tail)
                    val unFinishedQuestNum = unFinishedQuestNumStr.toString()
                    val spannableString =
                        SpannableString(getString(R.string.A026, unFinishedQuestNum))
                    val start = spannableString.indexOf(unFinishedQuestNum)
                    val end = start + unFinishedQuestNum.length
                    // 設定參數的字體大小為 12dp
                    val paramSize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        12f,
                        resources.displayMetrics
                    ).toInt()
                    spannableString.setSpan(
                        AbsoluteSizeSpan(paramSize),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    // 設定剩餘字串的字體大小為 10dp
                    val defaultSize = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        10f,
                        resources.displayMetrics
                    ).toInt()
                    spannableString.setSpan(
                        AbsoluteSizeSpan(defaultSize),
                        0,
                        start,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannableString.setSpan(
                        AbsoluteSizeSpan(defaultSize),
                        end,
                        spannableString.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    binding.tvTipsContent.text = spannableString
                } else {
                    binding.ivTipsHead.setImageResource(R.drawable.ic_task_tips_finished_head)
                    binding.ivTipsContent.setImageResource(R.drawable.ic_task_tips_finished_content)
                    binding.ivTipsTail.setImageResource(R.drawable.ic_task_tips_finished_tail)
                    binding.tvTipsContent.text = getString(R.string.A027)
                }
            }
        }
        //endregion 任務未完成個數更新
        //region 即將結束任務
        viewModel.unFinishedLimitedTimeQuest.collectWith(lifecycleScope) { info ->
            LogUtil.toJson(info)
            binding.blockLimitedTimeTask.isVisible = info != null
            //倒數計時器
            info.setupTaskCountDownTimer(binding.ivClock, binding.cmTopEndDate, {
                viewModel.onTaskTimeUp()
            })
            //此處設計將倒數計時放在標題旁, 故不使用TaskContent內部的計時器. See: showCountDownTimer
            info.setupWithViewTaskContent(
                binding = binding.viewLimitedInfo,
                viewListener = taskInfoViewListener,
                showCountDownTimer = false
            )

            binding.viewLimitedInfo.apply {
                //region 任務名稱 + 任務獎勵簡介
                tvTaskName.text = info?.questName
                ivTaskInfoDetail.isVisible = info?.showIntroduction == 1
                //endregion 任務名稱 + 任務獎勵簡介
            }
        }
        //endregion 即將結束任務

        //region 任務類別清單更新
        viewModel.taskTypeList.collectWith(lifecycleScope) { list ->
            taskTypeAdapter.setupData(list)
            mSelectedTaskType = list.firstOrNull { it.isSelected }?.taskType
            mSelectedTaskType?.setupDailyTipsVisible()
            displayFloatingReward()
        }
        //endregion 任務類別清單更新

        //region 任務資料清單更新
        viewModel.showTaskList.collectWith(lifecycleScope) { pair ->
            taskInfoAdapter.setupData(pair.first, pair.second)
        }
        //endregion 任務資料清單更新

        //region 領取後彈窗事件
        viewModel.viewEvent.collectWith(lifecycleScope) { event ->
            when (event) {
                is TaskCenterDialogEvent.RewardSuccess -> {
                    var rewardPointValue: Double? = null
                    var rewardCashValue: Double? = null
                    when (event.rewardType) {
                        RewardType.POINT -> rewardPointValue = event.rewardValue
                        RewardType.CASH -> rewardCashValue = event.rewardValue
                        else -> {
                            //do nothing
                        }
                    }
                    TaskRewardDialog.newInstance(
                        rewardType = event.rewardType,
                        rewardPointValue = rewardPointValue,
                        rewardCashValue = rewardCashValue
                    ).showAllowingStateLoss(supportFragmentManager)
                }

                is TaskCenterDialogEvent.RewardAllSuccess -> {
                    //do nothing, 此處不會有一次領取多類型獎勵的情境
                }

                TaskCenterDialogEvent.RewardFail -> TaskRewardDialog.newInstance(isFailed = true)
                    .showAllowingStateLoss(supportFragmentManager)
            }
        }
        //endregion 領取後彈窗事件

        //region 找回獎勵數量
        viewModel.foundRewardsCount.collectWith(lifecycleScope) {
            mFoundRewardCount = it
            displayFloatingReward()
        }
        //endregion 找回獎勵數量

        //region 任務中心事件
        viewModel.taskCenterEvent.collectWith(lifecycleScope) { event ->
            when (event) {
                is TaskCenterEvent.InfoTodoBehavior -> {
                    val info = event.info
                    val limitedGames = info.limitedGame
                    when (info.conditionSubTypeFundEnum) {
                        ConditionSubType.BET ->
                            if(limitedGames.isNullOrEmpty()){
                                finish()
                                MainTabActivity.activityInstance?.jumpToTheSport()
                            } else if (limitedGames.size == 1) {
                                jumpToLimitedGame(limitedGames.first())
                            } else {
                                LimitedGamesDialog.newInstance(ArrayList(limitedGames))
                                    .show(supportFragmentManager)
                            }

                        else -> {
                            info.redirectTypeEnum?.navigatePage()
                        }
                    }
                }
            }
        }
        //endregion 任務中心事件
    }

    private fun displayFloatingReward() {
        Timber.e("[T] mFoundRewardCount: $mFoundRewardCount, mSelectedTaskType: $mSelectedTaskType")
        if (!TaskCenterRepository.isBlocked && mFoundRewardCount > 0) {
            binding.ivFloatingReward.isVisible = true
            binding.tvFloatingRewardCount.isVisible = true
            binding.tvFloatingRewardCount.text = mFoundRewardCount.toString()
        } else {
            binding.ivFloatingReward.isVisible = false
            binding.tvFloatingRewardCount.isVisible = false
        }
    }

    private fun TaskType.setupDailyTipsVisible() {
        binding.tvDailyTips.isVisible = when (this) {
            TaskType.DAILY -> true
            TaskType.TOP_PICKS, TaskType.BASIC, TaskType.LIMITED_TIME -> false
        }
    }

    fun jumpToLimitedGame(limitedGame: LimitedGame) {
        LogUtil.toJson(limitedGame)
        when (limitedGame.type) {
            LimitedGame.TYPE_SPORT -> {
                finish()
                val gameType = GameType.getGameType(limitedGame.gameType)
                if (gameType==null){
                    MainTabActivity.activityInstance?.jumpToTheSport()
                }else{
                    MainTabActivity.activityInstance?.jumpToSport(gameType = gameType)
                }
            }

            LimitedGame.TYPE_THIRD -> {
                //优先判断是否沙巴体育类型
                when(limitedGame.firmType){
                    Constants.FIRM_TYPE_SBTY->{
                        if (StaticData.sbSportOpened()){
                            finish()
                            setupOKPlay { okPlayBean ->
                                if (okPlayBean != null) {
                                    MainTabActivity.activityInstance?.enterThirdGame(okPlayBean, "任务中心-体育分类SABA SPORTS")
                                }
                            }
                        }else{
                            showPromptDialog(message = getString(R.string.shaba_no_open)){}
                        }
                    }
                    Constants.FIRM_TYPE_OKMINI->{
                        finish()
                        MainTabActivity.activityInstance?.jumpToPerya()
                    }
                    else ->{
                        OKGamesRepository.gameFiremEvent.value?.first { it.firmType == limitedGame.firmType && it.firmCode == limitedGame.firmCode}
                            ?.let {
                                LogUtil.toJson(it)
                                val okGamesFirm = OKGamesFirm(
                                    id = it.id!!,
                                    firmName = if(limitedGame.firmType==limitedGame.firmCode) it.firmShowName else it.firmCode,
                                    img = null,
                                    imgMobile = null,
                                    remark = null,
                                    maintain = null,
                                    sort = it.sort,
                                    firmShowName = it.firmShowName,
                                    open = null,
                                    gameEntryTypeEnum = if(limitedGame.gameCategory==LimitedGame.GAME_CATEGORY_LIVE) GameEntryType.OKLIVE else GameEntryType.OKGAMES
                                )
                                finish()
                                if (okGamesFirm.gameEntryTypeEnum==GameEntryType.OKLIVE){
                                    MainTabActivity.activityInstance?.jumpOKLiveWithProvider(okGamesFirm)
                                }else{
                                    MainTabActivity.activityInstance?.jumpOKGameWithProvider(okGamesFirm)
                                }
                            }
                    }
                }
            }
            else -> {

            }
        }
    }
    private fun getDefaultTaskType(intent: Intent): TaskType{
        val timeType = intent.getIntExtra("timeType",-1)
        if (timeType>=0) {
            //0 永久, 1 每日, 2 限时
            return when (timeType) {
                0 -> TaskType.BASIC
                1 -> TaskType.DAILY
                2 -> TaskType.LIMITED_TIME
                else -> TaskType.TOP_PICKS
            }
        }
        return TaskType.TOP_PICKS
    }
    private fun scrollCenterTaskType(taskListType: TaskListType){
        val selectedPos = taskTypeAdapter.mTaskTypeList.indexOf(taskListType)
        (binding.rvTaskType.layoutManager as ScrollCenterLayoutManager).scrollToPosition(selectedPos)
    }
}