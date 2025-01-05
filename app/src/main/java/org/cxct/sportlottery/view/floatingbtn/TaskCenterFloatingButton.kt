package org.cxct.sportlottery.view.floatingbtn

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.postDelayed
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.databinding.PopTaskCenterMainBinding
import org.cxct.sportlottery.network.quest.timeLine.QuestCompleteVO
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.view.setColors

class TaskCenterFloatingButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    LinearLayout(context, attributeSet, defStyle){


    private lateinit var binding: PopTaskCenterMainBinding
    private var questCompleteVO: QuestCompleteVO?=null

    init {
        initView(context)
    }

    /**
     * 初始化控件
     *
     * @param context
     */
    private fun initView(context: Context) {
        binding = PopTaskCenterMainBinding.inflate(LayoutInflater.from(context),this,true)
        binding.ivClose.setOnClickListener { TaskCenterManager.instance.clickCloseFloatBtn() }
        binding.tvContent.text = context.getString(R.string.A043)
        binding.tvStatus.setOnClickListener {
            questCompleteVO?.let { it1 -> TaskCenterManager.instance.clickReceived(it1) }
        }
    }
    fun setQuestComplete(questCompleteVO: QuestCompleteVO){
        this.questCompleteVO = questCompleteVO
        if(questCompleteVO==null) return
        binding.tvContent.text = String.format(MultiLanguagesApplication.getInstance().getString(R.string.A043),TextUtil.formatMoney2(questCompleteVO!!.rewardValue?:0),questCompleteVO!!.questName)
        binding.tvStatus.apply {
            if (questCompleteVO!!.deliverStatus.toIntS(0)==1){
                setBackgroundResource(R.drawable.img_task_claim_button)
                text = MultiLanguagesApplication.getInstance().getString(R.string.A038)
                setColors(R.color.color_FFFFFF)
            }else{
                background = null
                text = MultiLanguagesApplication.getInstance().getString(R.string.A045)
                setColors(R.color.color_F06A75)
                postDelayed(3000){ TaskCenterManager.instance.clickCloseFloatBtn() }
            }
        }
    }

    /**
     * 解决语言切换显示
     */
    fun udateLang(){
        if(questCompleteVO==null) return
        binding.tvContent.text = String.format(MultiLanguagesApplication.getInstance().getString(R.string.A043),TextUtil.formatMoney2(questCompleteVO!!.rewardValue?:0),questCompleteVO!!.questName)
        binding.tvStatus.apply {
            if (questCompleteVO!!.deliverStatus.toIntS(0)==1){
                text = MultiLanguagesApplication.getInstance().getString(R.string.A038)
            }else{
                text = MultiLanguagesApplication.getInstance().getString(R.string.A045)
            }
        }
    }
}