package org.cxct.sportlottery.ui.profileCenter.identity

import android.util.Base64
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentVerifyIdentityDetailNewBinding
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.util.setTitleLetterSpacing

class VerifyIdentityDetailFragment :
    BindingFragment<ProfileCenterViewModel,FragmentVerifyIdentityDetailNewBinding>() {

    private val args: VerifyIdentityDetailFragmentArgs? by navArgs()

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

    override fun onInitView(view: View) {
        initView()
        initOnclick()
        (activity as VerifyIdentityActivity).setToolBarTitleForDetail()
    }

    private fun setInfoInText()=binding.run {
        args?.data?.extIdInfo?.ocrResult?.apply {
            etIdentityId?.isVisible = idNumber != null
            etIdentityLastName?.isVisible = lastName != null
            etIdentityFirstName?.isVisible = firstName != null
            etIdentityOtherName?.isVisible = middleName != null
            etIdentitySex?.isVisible = sex != null
            etBirth?.isVisible = dateOfBirth != null
            etExpireDate?.isVisible = expireDate != null
            etCountry?.isVisible = country != null
            etAddress?.isVisible = address != null

            etIdentityId.setText(idNumber)
            etIdentityLastName.setText(lastName)
            etIdentityFirstName.setText(firstName)
            etIdentityOtherName.setText(middleName)
            etIdentitySex.setText(sex)
            etBirth.setText(dateOfBirth)
            etExpireDate.setText(expireDate)
            etCountry.setText(country)
            etAddress.setText(address)
        }

    }

    private fun initView() {
        setInfoInText()
        setFaceImg()
        binding.btnSubmit.setTitleLetterSpacing()
    }

    private fun initOnclick()=binding.run{
        imgIdCard.setOnClickListener {
            imgText.text = getString(R.string.front_side_result)
            setBigPic(frontImageByteArray)
            setMask(imgIdCardMask.id)
        }

        imgIdCardBack.setOnClickListener {
            imgText.text = getString(R.string.reverse_side_result)
            setBigPic(backImageByteArray)
            setMask(imgIdCardBackMask.id)
        }

        imgFaceSmall.setOnClickListener {
            imgText.text = getString(R.string.face_scan_result)
            setBigPic(faceImgByteArray)
            setMask(imgFaceSmallMask.id)
        }

        btnSubmit.setOnClickListener {
            viewModel.getUserInfo()

            showPromptDialog(
                getString(R.string.prompt),
                getString(R.string.submit_success)
            ) {
                activity?.finish()
            }
        }

    }

    private fun setMask(hideMaskImgId: Int) =binding.run{
        imgIdCardMask.isVisible = hideMaskImgId != imgIdCardMask.id
        imgIdCardBackMask.isVisible = hideMaskImgId != imgIdCardBackMask.id
        imgFaceSmallMask.isVisible = hideMaskImgId != imgFaceSmallMask.id
    }

    private fun setFaceImg()=binding.run {

        Glide.with(requireContext())
            .asBitmap()
            .load(frontImageByteArray)
            .apply(smallPicRequestOptions)
            .into(imgIdCardMask)

        if (args?.data?.extIdInfo?.backPageImg.isNullOrEmpty()) {
            imgIdCardBack.isVisible = false
        } else {
            imgIdCardBack.isVisible = true
            Glide.with(requireContext())
                .asBitmap()
                .load(backImageByteArray)
                .apply(smallPicRequestOptions)
                .into(imgIdCardBack)
        }

        Glide.with(imgFaceSmall.context)
            .asBitmap()
            .load(faceImgByteArray)
            .apply(smallPicRequestOptions)
            .into(imgFaceSmall)

        setBigPic()
    }

    private fun setBigPic(byteArray: ByteArray? = frontImageByteArray) {
        Glide.with(requireContext())
            .asBitmap()
            .load(byteArray)
            .apply(bigPicRequestOptions)
            .into(binding.imgBig)
    }

}