package org.cxct.sportlottery.ui.profileCenter.pointshop


import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.databinding.ActivityPointRulesBinding
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.Spanny
import org.cxct.sportlottery.util.TextUtil

class PointRulesActivity : BaseActivity<PointRuleViewModel, ActivityPointRulesBinding>() {

    override fun pageName() = "积分规则"

    override fun onInitView() {
        setStatusbar(R.color.color_F6F7F8, true)
        binding.toolBar.binding.root.setBackgroundResource(R.color.color_F6F7F8)
        binding.toolBar.setOnBackPressListener {
            finish()
        }
        initObserver()
    }

    fun initObserver() {
        viewModel.pointRule.collectWith(lifecycleScope) {
            binding.tvGetPointContent.text = Spanny().apply {
                if (it?.rechStatus?.toString() == FLAG_OPEN) {
                    val rechMoney = "$showCurrencySign${TextUtil.formatMoney2(it.rechMoney)}"
                    val rechPoint = TextUtil.formatMoney2(it.rechPoint)
                    append(Spanny(getString(R.string.C044,rechMoney,rechPoint)).apply {
                        findAndSpan(rechMoney){ ForegroundColorSpan(ContextCompat.getColor(this@PointRulesActivity,R.color.color_F06A75)) }
                        findAndSpan(rechPoint){ ForegroundColorSpan(ContextCompat.getColor(this@PointRulesActivity,R.color.color_F06A75)) }
                    })
                    append("\n\n")
                }

                if (it?.validBetStatus?.toString() == FLAG_OPEN) {
                    val validBet ="$showCurrencySign${TextUtil.formatMoney2(it.validBet)}"
                    val validBetPoint = TextUtil.formatMoney2(it.validBetPoint)
                    append(Spanny(getString(R.string.C045,validBet,validBetPoint)).apply {
                        findAndSpan(validBet){ ForegroundColorSpan(ContextCompat.getColor(this@PointRulesActivity,R.color.color_F06A75)) }
                        findAndSpan(validBetPoint){ ForegroundColorSpan(ContextCompat.getColor(this@PointRulesActivity,R.color.color_F06A75)) }
                    })
                    append("\n\n")
                }

                if (StaticData.taskCenterOpened()) {
                    append(getString(R.string.C046))
                    append("\n\n")
                }
                append(getString(R.string.A097))
            }
        }
    }

}