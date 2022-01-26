package org.cxct.sportlottery.ui.profileCenter.identity

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_verify_identity_detail_new.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.YMD_FORMAT
import java.util.*

class VerifyIdentityDetailFragment :
    BaseSocketFragment<ProfileCenterViewModel>(ProfileCenterViewModel::class) {

    private val args: VerifyIdentityDetailFragmentArgs? by navArgs()

    private lateinit var dateTimePicker: TimePickerView

    private val faceImgByteArray: ByteArray by lazy {
        Base64.decode(
            args?.data?.extFaceInfo?.faceImg,
            Base64.DEFAULT
        )
    }
    private val frontImageByteArray: ByteArray by lazy {
        Base64.decode(
            args?.data?.extIdInfo?.frontPageImg,
            Base64.DEFAULT
        )
    }
    private val backImageByteArray: ByteArray? by lazy {
        if (args?.data?.extIdInfo?.backPageImg == null) null else
            Base64.decode(args?.data?.extIdInfo?.backPageImg, Base64.DEFAULT)
    }

    private val smallPicRequestOptions = RequestOptions()
        .override(300)
        .centerCrop()
        .sizeMultiplier(0.5f)

    private val bigPicRequestOptions = RequestOptions()
        .override(1000)
        .centerCrop()
        .sizeMultiplier(0.5f)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verify_identity_detail_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initOnclick()
    }

    private fun setInfoInText() {
        args?.data?.extIdInfo?.ocrResult?.apply {
            et_identity_id.setText(idNumber)
            et_identity_first_name.setText(firstName)
            et_identity_last_name.setText(lastName)
            et_identity_other_name.setText(middleName)
            tv_birth.text = dateOfBirth
            et_identity_sex.setText(sex)
        }
    }

    private fun initView() {
        initTimePicker()
        setInfoInText()
        setFaceImg()
    }

    private fun initOnclick() {
        img_id_card.setOnClickListener {
            img_text.text = getString(R.string.front_side_result)
            setBigPic(frontImageByteArray)
            setMask(img_id_card_mask.id)
        }

        img_id_card_back.setOnClickListener {
            img_text.text = getString(R.string.reverse_side_result)
            setBigPic(backImageByteArray)
            setMask(img_id_card_back_mask.id)
        }

        img_face_small.setOnClickListener {
            img_text.text = getString(R.string.face_scan_result)
            setBigPic(faceImgByteArray)
            setMask(img_face_small_mask.id)
        }

        cv_recharge_time.setOnClickListener {
            dateTimePicker.show()
        }

        btn_submit.setOnClickListener {
            if (noEmptyField(
                    et_identity_id,
                    et_identity_last_name,
                    et_identity_first_name,
                    et_identity_other_name,
                    et_identity_country,
                    tv_birth,
                    et_identity_marital_status,
                    et_identity_sex,
                    et_identity_work
                )
            ) {
                viewModel.getUserInfo()
                activity?.finish()
                startActivity(Intent(context, ProfileActivity::class.java))

            } else {
                showPromptDialog(getString(R.string.prompt), getString(R.string.error_input_empty)) {}
            }
        }

    }

    private fun noEmptyField(
        etIdentityId: LoginEditText?,
        etIdentityLastName: LoginEditText?,
        etIdentityFirstName: LoginEditText?,
        etIdentityOtherName: LoginEditText?,
        etIdentityCountry: LoginEditText?,
        tvBirth: TextView?,
        etIdentityMaritalStatus: LoginEditText?,
        etIdentitySex: LoginEditText?,
        etIdentityWork: LoginEditText?
    ): Boolean {

        return etIdentityId?.getText()?.isNotEmpty() == true &&
                etIdentityLastName?.getText()?.isNotEmpty() == true &&
                etIdentityFirstName?.getText()?.isNotEmpty() == true &&
                etIdentityOtherName?.getText()?.isNotEmpty() == true &&
                etIdentityCountry?.getText()?.isNotEmpty() == true &&
                tvBirth?.text?.isNotEmpty() == true &&
                etIdentityMaritalStatus?.getText()?.isNotEmpty() == true &&
                etIdentitySex?.getText()?.isNotEmpty() == true &&
                etIdentityWork?.getText()?.isNotEmpty() == true

    }

    private fun setMask(hideMaskImgId: Int) {
        img_id_card_mask.isVisible = hideMaskImgId != img_id_card_mask.id
        img_id_card_back_mask.isVisible = hideMaskImgId != img_id_card_back_mask.id
        img_face_small_mask.isVisible = hideMaskImgId != img_face_small_mask.id
    }

    private fun setFaceImg() {

        Glide.with(img_id_card.context)
            .asBitmap()
            .load(frontImageByteArray)
            .apply(smallPicRequestOptions)
            .into(img_id_card)

        if (args?.data?.extIdInfo?.backPageImg.isNullOrEmpty()) {
            img_id_card_back.isVisible = false
        } else {
            img_id_card_back.isVisible = true
            Glide.with(img_id_card_back.context)
                .asBitmap()
                .load(backImageByteArray)
                .apply(smallPicRequestOptions)
                .into(img_id_card_back)
        }

        Glide.with(img_face_small.context)
            .asBitmap()
            .load(faceImgByteArray)
            .apply(smallPicRequestOptions)
            .into(img_face_small)

        setBigPic()
    }

    private fun setBigPic(byteArray: ByteArray? = frontImageByteArray) {
        Glide.with(img_big.context)
            .asBitmap()
            .load(byteArray)
            .apply(bigPicRequestOptions)
            .into(img_big)
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
            .setSubmitColor(
                ContextCompat.getColor(
                    cv_recharge_time.context,
                    R.color.colorGrayLight
                )
            )
            .setCancelColor(
                ContextCompat.getColor(
                    cv_recharge_time.context,
                    R.color.colorGrayLight
                )
            )
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