package org.cxct.sportlottery.ui.profileCenter.identity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_verify_identity.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.setServiceClick

class VerifyIdentityDialog: DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCustomDialogStyle()
        return inflater.inflate(R.layout.dialog_verify_identity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()
    }

    private fun initButton() {
        btn_close.setOnClickListener { dismiss() }
        btn_service.setServiceClick(childFragmentManager)
        btn_check.setOnClickListener {
            startActivity(Intent(context, VerifyIdentityActivity::class.java))
            dismiss()
        }
    }

    private fun setCustomDialogStyle() {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setGravity(Gravity.CENTER)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        dialog?.setCanceledOnTouchOutside(false)
    }

}