package org.cxct.sportlottery.ui.profileCenter.invite

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.tbruyelle.rxpermissions2.RxPermissions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.common.extentions.toast
import org.cxct.sportlottery.databinding.DialogInviteBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileModel
import org.cxct.sportlottery.util.*


class InviteDialog: BaseDialog<ProfileModel,DialogInviteBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }
    companion object{
        fun newInstance(inviteCode: String) = InviteDialog().apply {
            arguments = Bundle().apply { putString("inviteCode", inviteCode) }
        }
    }
    private val inviteUrl by lazy { "https://www.okbet.com/?inviteCode=${arguments?.getString("inviteCode")}" }
    private val quote = "Enjoy a bigger bonus of up to 15%. Join us, and let's WIN TOGETHER!"
    private val content by lazy { "$quote $inviteUrl" }

    override fun onInitView() {
        runWithCatch {
            createQRCodeBitmap(inviteUrl)?.let {
                binding.ivQRCode.setImageBitmap(it)
            }
        }
       initClick()
    }
    private fun initClick()=binding.run{
        ivClose.setOnClickListener {
            dismiss()
        }
        tvFacebook.setOnClickListener {
            ShareUtil.shareFacebook(requireActivity(),content,inviteUrl)
        }
        tvMessenger.setOnClickListener {
            ShareUtil.shareMessenger(requireActivity(),content,inviteUrl)
        }
        tvInstagram.setOnClickListener {
            ShareUtil.shareInstagram(requireActivity(),content)
        }
        tvViber.setOnClickListener {
            ShareUtil.shareViber(requireActivity(),content)
        }
        tvCopyLink.setOnClickListener {
            requireContext().copyText(inviteUrl)
            ToastUtil.showToastInCenter(activity, getString(R.string.text_money_copy_success))
        }
        tvSave.setOnClickListener {
            RxPermissions(requireActivity()).requestWriteStorageWithApi33(grantFun = {
                binding.linCode.viewToBitmap()?.let {
                    BitmapUtil.saveBitmapToGallery(requireActivity(),it){
                        if (it)
                           ToastUtil.showToast(requireActivity(),R.string.chat_photo_download_done)
                        else
                            toast(getString(R.string.unknown_error))
                    }

                }
            }, unGrantFun = {
                    toast(getString(R.string.unknown_error))
                }
            )
        }
        tvSms.setOnClickListener {
            ShareUtil.sendSMS(requireActivity(),content)
        }
        tvMore.setOnClickListener {
            ShareUtil.shareBySystem(requireActivity(),content)
        }
    }
    /**
     * 生成二维码图片大小
     */
    fun createQRCodeBitmap(
        content: String?,
    ): Bitmap? {
        val QRCODE_SIZE = 300
        // 用于设置QR二维码参数
        val qrParam= mutableMapOf<EncodeHintType, Any>()
        // 设置QR二维码的纠错级别——这里选择最高H级别
        qrParam.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
        // 设置编码方式
        qrParam.put(EncodeHintType.CHARACTER_SET, "UTF-8")

        // 生成QR二维码数据——这里只是得到一个由true和false组成的数组
        // 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
        try {
            val bitMatrix = MultiFormatWriter().encode(content,
                BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE, qrParam)

            // 开始利用二维码数据创建Bitmap图片，分别设为黑白两色
            val w = bitMatrix.width
            val h = bitMatrix.height
            val data = IntArray(w * h)
            for (y in 0 until h) {
                for (x in 0 until w) {
                    if (bitMatrix[x, y]) data[y * w + x] = -0x1000000 // 黑色
                    else data[y * w + x] = 0x00ffffff // -1 相当于0xffffffff 白色
                }
            }
            // 创建一张bitmap图片，采用最高的图片效果ARGB_8888
            val bitmap: Bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            // 将上面的二维码颜色数组传入，生成图片颜色
            bitmap.setPixels(data, 0, w, 0, 0, w, h)
            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        return null
    }
    private fun View.viewToBitmap():Bitmap? {
        var screenshot: Bitmap?
        screenshot = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
        val canvas = Canvas(screenshot);
        draw(canvas)
        return screenshot
    }
}