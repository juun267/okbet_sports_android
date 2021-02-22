package org.cxct.sportlottery.ui.profileCenter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import kotlinx.android.synthetic.main.dialog_setting_tips.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel

class SettingTipsDialog(private val androidContext: Context, private val dialogListener: SettingTipsDialogListener) : BaseDialog<BaseViewModel>(BaseViewModel::class) {

    private var tipsTitle: String? = null
    private var tipsContent: String? = null

    init {
        setStyle(R.style.CustomDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_setting_tips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initButton()
    }

    private fun initView() {
        txv_title.text = tipsTitle
        tv_tips.text = tipsContent
    }


    fun setTipsTitle(@StringRes titleStringRes: Int) {
        tipsTitle = androidContext.getString(titleStringRes)
    }

    fun setTipsContent(@StringRes tipsContentStringRes: Int) {
        tipsContent = androidContext.getString(tipsContentStringRes)
    }

    fun initButton() {
        img_close.setOnClickListener {
            dismiss()
        }
        tv_go_setting.setOnClickListener {
            dialogListener.goSettingPage()
            dismiss()
        }
    }

    class SettingTipsDialogListener(private val goSettingPageEvent: () -> Unit) {
        fun goSettingPage() {
            goSettingPageEvent.invoke()
        }
    }
}