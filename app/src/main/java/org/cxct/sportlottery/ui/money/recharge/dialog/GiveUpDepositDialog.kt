package org.cxct.sportlottery.ui.money.recharge.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogDepisteGiveupBinding
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BaseDialogFragment
import org.cxct.sportlottery.util.DisplayUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import splitties.bundle.put

class GiveUpDepositDialog private constructor(): BaseDialogFragment() {

    private lateinit var binding: DialogDepisteGiveupBinding

    companion object {

        private const val KEY_AMOUNT = "amount"

        fun show(fragmentManager: FragmentManager, amount: String) {
            val instance = GiveUpDepositDialog()
            val bundle = Bundle()
            bundle.put(KEY_AMOUNT, amount)
            instance.arguments = bundle
            instance.show(fragmentManager, instance.javaClass.name)
        }

    }

    init {
        setStyle(STYLE_NO_TITLE, R.style.CustomDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogDepisteGiveupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        view.background = ShapeDrawable().setSolidColor(Color.WHITE).setRadius(12.dp.toFloat())
        val cxt = view.context
        tvCancel.background = ShapeDrawable().setStrokeColor(cxt.getColor(R.color.color_BEC7DC)).setRadius(12.dp.toFloat()).setStrokeSize(1.dp)
        tvContinue.background = ShapeDrawable().setSolidColor(cxt.getColor(R.color.color_025BE8)).setRadius(12.dp.toFloat())
        tvAmount.text = "$showCurrencySign${arguments?.getString(KEY_AMOUNT)}"
        ivClose.setOnClickListener {
            DepositHintDialog().show(childFragmentManager, DepositHintDialog::javaClass.name)
//            dismiss()
        }
        tvContinue.setOnClickListener { dismiss() }
        tvCancel.setOnClickListener { activity?.finish() }

        setLayoutParams()
    }

    private fun setLayoutParams() {
        dialog?.window?.let { window->
            val lp = window.attributes
            lp.width = DisplayUtil.screenWith - 60.dp
            lp.height = 251.dp
            window.attributes = lp
        }
    }
}