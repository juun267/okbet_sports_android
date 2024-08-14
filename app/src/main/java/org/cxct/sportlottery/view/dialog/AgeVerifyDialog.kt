package org.cxct.sportlottery.view.dialog

import android.graphics.Color
import android.graphics.Typeface
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.drake.spannable.addSpan
import com.drake.spannable.movement.ClickableMovementMethod
import com.drake.spannable.replaceSpan
import com.drake.spannable.setSpan
import com.drake.spannable.span.ColorSpan
import com.drake.spannable.span.HighlightSpan
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.databinding.DialogAgeVerifyBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.KvUtils
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.makeLinks
import org.cxct.sportlottery.view.dialog.queue.BasePriorityDialog
import org.cxct.sportlottery.view.dialog.queue.PriorityDialog

class AgeVerifyDialog : BaseDialog<BaseViewModel,DialogAgeVerifyBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }

    companion object{
         var isAgeVerifyNeedShow :Boolean = true
             get() = KvUtils.decodeBooleanTure("isAgeVerifyNeedShow",true)
             set(value) {
                field = value
                KvUtils.put("isAgeVerifyNeedShow",value)
             }

        fun newInstance() = AgeVerifyDialog()

        fun buildAgeVerifyDialog(priority: Int, fm: () -> FragmentManager): PriorityDialog? {
            if (!isAgeVerifyNeedShow) {
                return null
            }

            isAgeVerifyNeedShow = false
            return object : BasePriorityDialog<AgeVerifyDialog>() {
                override fun getFragmentManager() = fm.invoke()
                override fun priority() = priority
                override fun createDialog() = AgeVerifyDialog.newInstance()
            }
        }
    }

    override fun onInitView()=binding.run {
        llContent.background = ShapeDrawable()
            .setStrokeColor(Color.parseColor("#c2c2c2"))
            .setStrokeSize(1.dp)
            .setSolidColor(Color.WHITE)
            .setRadius(12.dp.toFloat())
        val cxt = binding.root.context

        tvTitle.movementMethod = ClickableMovementMethod.getInstance()
        tvTitle.text = "Please read our "
            .setSpan(listOf(ColorSpan(Color.BLACK), StyleSpan(Typeface.BOLD)))
            .addSpan("Responsible Gaming", listOf(StyleSpan(Typeface.BOLD), HighlightSpan(cxt, R.color.color_025BE8) {
                JumpUtil.toInternalWeb(cxt, Constants.getDutyRuleUrl(cxt), cxt.getString(R.string.responsible))
            }))
            .addSpan("\nguidelines carefully:", listOf(ColorSpan(Color.BLACK), StyleSpan(Typeface.BOLD)))
        (sConfigData?.ageVerificationChecked==1).let {
            setAllSelect(it)
            updateCheckStatus(it)
        }
        cbAgree.text = getString(R.string.dialog_age_verify_hint)+" "+getString(R.string.M311)
        cbAgree.makeLinks(
            Pair(
                getString(R.string.dialog_age_verify_hint),
                View.OnClickListener {
                    val isSelect = !cbAgree.isSelected
                    setAllSelect(isSelect)
                    updateCheckStatus(isSelect)
                }),
            linkColor = cxt.getColor(R.color.color_0D2245)
        )
        cbAgree.makeLinks(
            Pair(
                getString(R.string.M311),
                View.OnClickListener {
                    JumpUtil.toInternalWeb(
                        cxt,
                        Constants.getAgreementRuleUrl(cxt),
                        resources.getString(R.string.login_terms_conditions)
                    )
                }),
            linkColor = cxt.getColor(R.color.color_025BE8)
        )

        tv4.movementMethod = ClickableMovementMethod.getInstance()
        tv4.text = "I have read and agree to OKBet's"
            .setSpan(HighlightSpan(cxt, R.color.color_0D2245) {
                binding.ivCheck4.performClick()
            })
            .addSpan("Terms & Conditions.",
                HighlightSpan(cxt, R.color.color_025BE8) {
                    JumpUtil.toInternalWeb(cxt, Constants.getAgreementRuleUrl(cxt), "Terms & Conditions")
                }
            )

        btnConfirm.setOnClickListener {
            dismiss()
        }
        btnExit.setOnClickListener {
            dismiss()
        }

        setOnClickListeners(binding.lin1, binding.lin2, binding.lin3) {
            val selectView = (it as ViewGroup).getChildAt(0)
            selectView.isSelected = !selectView.isSelected
            checkSelect()
        }

        binding.ivCheck4.setOnClickListener {
            binding.ivCheck4.isSelected = !binding.ivCheck4.isSelected
            checkSelect()
        }
    }


    private fun updateCheckStatus(isChecked: Boolean)=binding.run{
        cbAgree.isSelected = isChecked
        btnConfirm.isEnabled = isChecked
    }

    private fun setAllSelect(isSelected: Boolean) {
        binding.ivCheck1.isSelected = isSelected
        binding.ivCheck2.isSelected = isSelected
        binding.ivCheck3.isSelected = isSelected
        binding.ivCheck4.isSelected = isSelected
    }

    private fun checkSelect() {
        updateAgree(binding.ivCheck1.isSelected
                && binding.ivCheck2.isSelected
                && binding.ivCheck3.isSelected
                && binding.ivCheck4.isSelected)
    }

    private fun updateAgree(isChecked: Boolean) {
        binding.cbAgree.isSelected = isChecked
        binding.btnConfirm.isEnabled = isChecked
    }

}
