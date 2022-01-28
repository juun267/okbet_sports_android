package org.cxct.sportlottery.ui.profileCenter.identity

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.bigkoo.pickerview.view.TimePickerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_verify_identity_detail_new.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.login.LoginEditText
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity

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
            et_identity_sex.setText(sex)
            et_birth.setText(dateOfBirth)
            et_expire_date.setText(expireDate)
            et_address.setText(address)
        }

        et_identity_id?.isVisible = et_identity_id?.getText()?.isNotEmpty() == true
        et_identity_last_name?.isVisible = et_identity_last_name?.getText()?.isNotEmpty() == true
        et_identity_first_name?.isVisible = et_identity_first_name?.getText()?.isNotEmpty() == true
        et_identity_other_name?.isVisible = et_identity_other_name?.getText()?.isNotEmpty() == true
        et_identity_sex?.isVisible = et_identity_sex?.getText()?.isNotEmpty() == true
        et_birth?.isVisible = et_birth?.getText()?.isNotEmpty() == true
        et_expire_date?.isVisible = et_expire_date?.getText()?.isNotEmpty() == true
        et_address?.isVisible = et_address?.getText()?.isNotEmpty() == true

    }

    private fun initView() {
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

        btn_submit.setOnClickListener {
            viewModel.getUserInfo()
            activity?.finish()
            startActivity(Intent(context, ProfileActivity::class.java))
        }

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

}