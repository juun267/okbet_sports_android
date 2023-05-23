package org.cxct.sportlottery.ui.chat

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.dialog_send_picture_msg.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialogFragment
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ScreenUtil

/**
 * @author Louis
 * @create 202/4/13
 * @description 傳送圖片訊息彈窗
 */
class SendPictureMsgDialog : BaseDialogFragment() {

    private val IMAGE_PATH = "IMAGE_PATH"
    var sendMsgListener: SendMsgListener? = null

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_send_picture_msg, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(
            ScreenUtil.getScreenWidth(requireContext()),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT),
            40.dp))
        initView()
    }

    private fun initView() {
        val imagePath = arguments?.getString(IMAGE_PATH).orEmpty()

        val resServerHost: String = sConfigData?.resServerHost.orEmpty()
        val url = if (imagePath.startsWith("http")) imagePath else "$resServerHost/$imagePath"
        Glide.with(ivImg.context)
            .load(url)
            .placeholder(ContextCompat.getDrawable(ivImg.context, R.drawable.ic_image_load))
            .error(ContextCompat.getDrawable(ivImg.context, R.drawable.ic_image_load))
            .into(ivImg)

        ivSend.setOnClickListener {
            if (etInput.text.isNullOrEmpty()) {
                sendMsgListener?.onSend(imagePath, ChatType.CHAT_SEND_PIC_MSG)
            } else {
                //content:[img:{imagePath}]{message}
                //  ex:
                //     content:[img:/p/20230409/cx_sports/1/img/469/jpg/1681007650426.jpg]你好
                val message = etInput.text.toString()
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
