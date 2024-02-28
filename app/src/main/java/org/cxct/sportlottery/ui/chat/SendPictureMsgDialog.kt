package org.cxct.sportlottery.ui.chat

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.WindowManager
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.databinding.DialogSendPictureMsgBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ScreenUtil

/**
 * @author Louis
 * @create 202/4/13
 * @description 傳送圖片訊息彈窗
 */
class SendPictureMsgDialog : BaseDialog<BaseViewModel,DialogSendPictureMsgBinding>() {

    private val IMAGE_PATH = "IMAGE_PATH"
    var sendMsgListener: SendMsgListener? = null

    init {
        setStyle(R.style.CustomDialogStyle)
    }

    companion object {

        @JvmStatic
        fun newInstance(pictureUrl: String, sendListener: SendMsgListener) =
            SendPictureMsgDialog().apply {
                sendMsgListener = sendListener
                arguments = Bundle().apply {
                    putString(IMAGE_PATH, pictureUrl)
                }
            }
    }

    interface SendMsgListener {
        fun onSend(msg: String, chatType: ChatType)
    }

    override fun onInitView() {
        dialog?.window?.setLayout(
            ScreenUtil.getScreenWidth(requireContext()),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT),
            40.dp))
        initView()
    }

    private fun initView()=binding.run {
        val imagePath = arguments?.getString(IMAGE_PATH).orEmpty()

        val resServerHost: String = sConfigData?.resServerHost.orEmpty()
        val url = if (imagePath.startsWith("http")) imagePath else "$resServerHost/$imagePath"
        ivImg.load(url, R.drawable.ic_image_load)

        ivSend.setOnClickListener {
            if (etInput.text.isNullOrEmpty()) {
                sendMsgListener?.onSend(imagePath, ChatType.CHAT_SEND_PIC_MSG)
            } else {
                //content:[img:{imagePath}]{message}
                //  ex:
                //     content:[img:/p/20230409/cx_sports/1/img/469/jpg/1681007650426.jpg]你好
                val message = etInput.text.toString().replace("\n", "")
                val content = "[img:$imagePath]$message"
                sendMsgListener?.onSend(content, ChatType.CHAT_SEND_PIC_AND_TEXT_MSG)
            }
            dismissAllowingStateLoss()
        }

        ivClose.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }
}
