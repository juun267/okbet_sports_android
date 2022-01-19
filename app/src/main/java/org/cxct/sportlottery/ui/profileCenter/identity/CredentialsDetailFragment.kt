package org.cxct.sportlottery.ui.profileCenter.identity

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.navigation.fragment.navArgs
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.fragment_credentials_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.YMD_FORMAT
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
        viewModel.getCredentialCompleteResult(args.transactionId)
        initObserve()
        setupView()

    }

    private fun initObserve() {
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

                    Glide.with(img_face_scan.context)
                        .asBitmap()
                        .load(faceImg)
                        .apply(requestOptions)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                img_face_scan.setImageBitmap(resource)
                            }
                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                        /*.into(object : BitmapImageViewTarget(img_face_scan) {
                            override fun setResource(resource: Bitmap?) {
                                val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(
                                    context!!.resources, resource
                                )
                                circularBitmapDrawable.cornerRadius = 8f
                                img_face_scan.setImageDrawable(circularBitmapDrawable)
                            }
                        })*/
//                    }
                }
            }
        }
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