package org.cxct.sportlottery.ui.profileCenter.identity

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.navigation.fragment.navArgs
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import kotlinx.android.synthetic.main.fragment_credentials_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.credential.CredentialCompleteData
import org.cxct.sportlottery.network.credential.ExtBasicInfo
import org.cxct.sportlottery.network.credential.ExtIdInfo
import org.cxct.sportlottery.network.interceptor.LogInterceptor.Logger.Companion.DEFAULT
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.YMD_FORMAT
import java.lang.Byte.decode
import java.util.*

class CredentialsDetailFragment : BaseSocketFragment<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

    private val args: CredentialsDetailFragmentArgs by navArgs()

    private lateinit var dateTimePicker: TimePickerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_credentials_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel.getCredentialCompleteResult(args.transactionId)
        initObserve()
        setupView()

    }

    private fun initObserve() {
        /*
        viewModel.credentialCompleteResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                //TODO Cheryl : resultStatus為失敗的情況處理

                it.data.extFaceInfo?.apply {
//                    if (ekycResultFace.equals("Success")) {
                    val requestOptions = RequestOptions()
                        .override(180)
                        .centerCrop()
                        .sizeMultiplier(0.5f)
                        .placeholder(R.drawable.picture_image_placeholder)

                    val imageByteArray: ByteArray = android.util.Base64.decode(faceImg, android.util.Base64.DEFAULT)
                    Glide.with(img_face_scan.context)
                        .asBitmap()
                        .load(imageByteArray)
                        .apply(requestOptions)
                        .into(img_face_scan)
                }
            }
        }
        */
    }

    private fun setInfoInText() {
        args.data?.extIdInfo?.ocrResult?.apply {
            et_identity_id.setText(idNumber)
            et_identity_first_name.setText(firstName)
            et_identity_last_name.setText(lastName)
            et_identity_other_name.setText(middleName)
            tv_birth.text = dateOfBirth
            et_identity_sex.setText(sex)
        }

    }


    private fun setupView() {

        setInfoInText()

        setCredentialImg()
        setFaceImg()

        cv_recharge_time.setOnClickListener {
            dateTimePicker.show()
        }

        btn_submit.setOnClickListener {
            viewModel.uploadIdentityDoc()
        }

        initTimePicker()
    }

    private fun setCredentialImg() {
        args.data?.extIdInfo?.apply {
            val requestOptions = RequestOptions()
                .override(180)
                .centerCrop()
                .sizeMultiplier(0.5f)
                .placeholder(R.drawable.picture_image_placeholder)

            val imageByteArray: ByteArray = Base64.decode(frontPageImg, Base64.DEFAULT)
            Glide.with(img_id_card.context)
                .asBitmap()
                .load(imageByteArray)
                .apply(requestOptions)
                .into(img_id_card)
        }
    }

    private fun setFaceImg() {
        args.data?.extFaceInfo?.apply {
            val requestOptions = RequestOptions()
                .override(180)
                .centerCrop()
                .sizeMultiplier(0.5f)
                .placeholder(R.drawable.picture_image_placeholder)

            val imageByteArray: ByteArray = Base64.decode(faceImg, Base64.DEFAULT)
            Glide.with(img_face_scan.context)
                .asBitmap()
                .load(imageByteArray)
                .apply(requestOptions)
                .into(img_face_scan)
        }
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
                tv_birth.text = TimeUtil.timeFormat(date.time, YMD_FORMAT)
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
        et_identity_marital_status.setText("SINGLE")
        et_identity_sex.setText("MALE")
        et_identity_work.setText("WORKER")
    }
}