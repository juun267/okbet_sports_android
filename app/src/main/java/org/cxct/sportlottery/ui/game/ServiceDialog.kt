package org.cxct.sportlottery.ui.game

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_service.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.JumpUtil

class ServiceDialog : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_service, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initServiceEvent()
    }

    private fun initView() {
        iv_close.setOnClickListener {
            dismiss()
        }

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 48.dp))
    }

    private fun initServiceEvent() {
        frame_service1.setOnClickListener {
            JumpUtil.toExternalWeb(context ?: requireContext(), sConfigData?.customerServiceUrl)
            dismiss()
        }

        frame_service2.setOnClickListener {
            JumpUtil.toExternalWeb(context ?: requireContext(), sConfigData?.customerServiceUrl2)
            dismiss()
        }
    }
}