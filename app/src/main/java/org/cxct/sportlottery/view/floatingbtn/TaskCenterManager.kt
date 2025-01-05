package org.cxct.sportlottery.view.floatingbtn

import android.content.Intent
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.children
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.quest.timeLine.QuestCompleteVO
import org.cxct.sportlottery.network.quest.timeLine.TimeLineResult
import org.cxct.sportlottery.repository.DEVICE_TYPE
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.ui.profileCenter.taskCenter.TaskCenterActivity
import org.cxct.sportlottery.ui.splash.LaunchActivity
import org.cxct.sportlottery.ui.splash.SplashActivity
import org.cxct.sportlottery.util.CountDownUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LogUtil
import splitties.activities.start

class TaskCenterManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.NONE) {
            TaskCenterManager()
        }
    }
    private var activity: BaseActivity<*,*>? = null
    private var floatRootView: TaskCenterFloatingButton? = null
    private var closeTaskId = mutableListOf<Long>()
    private var timelineJob: Job?=null
    private var questCompleteVOList: List<QuestCompleteVO>?=null
    private var questCheckJob: Job?=null
    private val filterDeliverStatus = listOf("1","2","4")

    open fun bind(activity: BaseActivity<*,*>) {
        this.activity = activity
        bindview()
    }

    /**
     * 限定指定页面不能显示红包相关的
     */
    fun allowdShow(): Boolean =
        when (activity!!::class) {
        SplashActivity::class,
        LaunchActivity::class,
        MaintenanceActivity::class, -> false
        else -> true
    }

    private fun bindview() {
        if (!allowdShow()){
            return
        }
        if (timelineJob==null&&LoginRepository.isLogined()){
            getTimeLine()
        }
        if (questCheckJob==null&&LoginRepository.isLogined()){
            postQuestCheck()
        }
        if (floatRootView!=null){
            GlobalScope.launch(Dispatchers.Main) {
                showFloatingBtn()
            }
        }
    }

    private fun showFloatingBtn() {
        //处理退出登录的特殊情况
        if (!LoginRepository.isLogined()) return removeFloatingBtn()
        var viewGroup = activity!!.findViewById<ViewGroup>(android.R.id.content)
        var targetView = viewGroup.children.firstOrNull{ it is TaskCenterFloatingButton} as TaskCenterFloatingButton?
        if (targetView!=null) {
            floatRootView = targetView
        } else {
            if (floatRootView == null) {
                floatRootView = TaskCenterFloatingButton(activity!!)
            } else {
                (floatRootView?.parent as ViewGroup).removeView(floatRootView)
            }
            viewGroup.addView(floatRootView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM
                bottomMargin = 70.dp
            })
        }
        floatRootView?.udateLang()
    }
    private fun removeFloatingBtn(){
        if (floatRootView!=null){
            var viewGroup = activity!!.findViewById<ViewGroup>(android.R.id.content)
            viewGroup.removeView(floatRootView)
            floatRootView=null
        }
    }

    fun clickCloseFloatBtn() {
        if (!questCompleteVOList.isNullOrEmpty()){
            closeTaskId.addAll(questCompleteVOList!!.map { it.rewardId })
        }
        removeFloatingBtn()
    }
    fun clickReceived(questCompleteVO: QuestCompleteVO) {
        clickCloseFloatBtn()
        activity?.start<TaskCenterActivity>{
            putExtra("timeType",questCompleteVO.timeType)
        }
    }
    private fun delayQuestCheck(){
        questCheckJob?.cancel()
        questCheckJob = CountDownUtil.runOnDelay(5 * 60 * 1000) {
            postQuestCheck()
        }
    }
    private fun postQuestCheck(){
        if (LoginRepository.isLogined()){
            GlobalScope.launch{
                activity?.viewModel?.doNetwork(MultiLanguagesApplication.appContext){
                    OneBoSportApi.questService.postQuestCheck(DEVICE_TYPE)
                }
            }
            delayQuestCheck()
        }else{
            questCheckJob=null
        }
    }
    private fun delayGetTimeLine(){
        timelineJob?.cancel()
        timelineJob = CountDownUtil.runOnDelay(10* 1000) {
            getTimeLine()
        }
    }
    private fun getTimeLine(){
        if (LoginRepository.isLogined()){
            GlobalScope.launch{
                activity?.viewModel?.doNetwork(MultiLanguagesApplication.appContext){
                    OneBoSportApi.questService.getTimeLine()
                }?.let {
                    GlobalScope.launch(Dispatchers.Main) {
                        dealWithResult(it)
                    }
                }
            }
            delayGetTimeLine()
        }else{
            timelineJob=null
        }
    }
    private fun dealWithResult(result: TimeLineResult) {
         questCompleteVOList = result.t?.questCompleteVOList?.filter { !closeTaskId.contains(it.rewardId)&& filterDeliverStatus.contains(it.deliverStatus)}
        if (!questCompleteVOList.isNullOrEmpty()) {
            val questCompleteVO = questCompleteVOList!!.first()
            //0	審核中
            //1	待領取獎勵
            //2	自動發放獎勵
            //3	獎勵已領取
            //4	審核不通過
            //5	已過期
            LogUtil.toJson(questCompleteVOList)
            showFloatingBtn()
            floatRootView?.setQuestComplete(questCompleteVO)
        }
    }
}