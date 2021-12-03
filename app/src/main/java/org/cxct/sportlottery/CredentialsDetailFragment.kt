package org.cxct.sportlottery

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import kotlinx.android.synthetic.main.fragment_credentials_detail.*
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.YMD_FORMAT
import java.util.*

class CredentialsDetailFragment : BaseSocketFragment<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

    private lateinit var dateTimePicker: TimePickerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_credentials_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFakeData()

        initObserve()
        setupView()

    }

    private fun initObserve() {
        viewModel.uploadVerifyPhotoResult.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                if (result.success) {
                    showPromptDialog(getString(R.string.prompt), resources.getString(R.string.complete)) { activity?.finish() }
                } else {
                    showErrorPromptDialog(getString(R.string.promotion), result.msg) {}
                }
            }
        })
    }

    private fun setupView() {
        cv_recharge_time.setOnClickListener {
            dateTimePicker.show()
        }

        btn_submit.setOnClickListener {
            viewModel.uploadIdentityDoc()
        }

        initTimePicker()
    }

    private fun initTimePicker() {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_MONTH, -30)
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, +30)
        (TimePickerBuilder(
            activity
        ) { date, _ ->
            try {
                //                    depositDate = date
                txv_recharge_time.text = TimeUtil.timeFormat(date.time, YMD_FORMAT)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
            .setLabel("", "", "", "", "", "")
            .setDate(Calendar.getInstance())
            .setTimeSelectChangeListener { }
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .setTitleText(resources.getString(R.string.identity_birth))
            .setCancelText(getString(R.string.picker_cancel))
            .setSubmitText(getString(R.string.picker_submit))
            .setSubmitColor(ContextCompat.getColor(cv_recharge_time.context, R.color.colorGrayLight))
            .setCancelColor(ContextCompat.getColor(cv_recharge_time.context, R.color.colorGrayLight))
            .isDialog(true)
            .build() as TimePickerView).also { dateTimePicker = it }

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        )

        params.leftMargin = 0
        params.rightMargin = 0
        dateTimePicker.dialogContainerLayout.layoutParams = params
        val dialogWindow = dateTimePicker.dialog.window
        if (dialogWindow != null) {
            dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim)
            dialogWindow.setGravity(Gravity.BOTTOM)
            dialogWindow.setDimAmount(0.1f)
        }
    }

    private fun initFakeData() {
        et_identity_id.setText("18101QW1011141710")
        et_identity_last_name.setText("WANG")
        et_identity_first_name.setText("QIBIN")
        et_identity_country.setText("CHINA")
        txv_recharge_time.text = "09.Sep.1987"
        et_identity_marital_status.setText("SINGLE")
        et_identity_sex.setText("MALE")
        et_identity_work.setText("WORKER")
    }
}