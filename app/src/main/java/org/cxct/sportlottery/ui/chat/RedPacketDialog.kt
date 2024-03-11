package org.cxct.sportlottery.ui.chat

import android.annotation.SuppressLint
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hide
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.databinding.DialogChatRedEnpBinding
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.*

/**
 * @author Bill
 * @create 2023/3/19
 * @description
 * 聊天室紅包彈窗
 * */

class RedPacketDialog(
    var packetClickListener: PacketListener,
    var packetId: String,
    var packetType: Int,
) : BaseDialog<BaseViewModel,DialogChatRedEnpBinding>() {


    enum class PacketType(val code: Int) {
        PW_PACKET(5) //口令紅包
    }

    class PacketListener(
        private val onClickListener: (packetId: Int, watchWord: String) -> Unit,
        private val onCancelListener: () -> Unit,
        private val onCompleteListener: (packetId: String) -> Unit,
        private val goRegisterPageListener: () -> Unit,
    ) {
        fun onClick(packetId: Int, watchWord: String) = onClickListener(packetId, watchWord)
        fun onCancel() = onCancelListener()
        fun onComplete(packetId: String) = onCompleteListener(packetId)
        fun goRegisterPage() = goRegisterPageListener()
    }

    //是否登入
    private val isLogin: Boolean
        get() = LoginRepository.isLogin.value == true

    init {
        setStyle(R.style.CustomDialogStyle)
    }


    override fun onInitView() {
        initView()
        initButtonAndAnimation()
    }

    private fun initButtonAndAnimation() {
        val flipIn = AnimationUtils.loadAnimation(context, R.anim.flip_in)
        flipIn.repeatCount = Animation.INFINITE
        flipIn.interpolator = LinearInterpolator()

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun initView()=binding.run {

        btnRedPacketOpen.setOnClickListener {
            if (!isLogin || isGuest()) {
                packetClickListener.goRegisterPage()
            } else {
                if (packetType == PacketType.PW_PACKET.code &&
                    watchWordInput.text.trim().toString().isEmpty()
                ) {
                    showPWErrorHint()
                    watchWordInput.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_watchword_input)
                } else
                    packetClickListener.onClick(
                        packetId.toInt(),
                        watchWordInput.text.trim().toString()
                    )
            }
        }

        cardView.setOnClickListener {
            dismiss()
        }

        //口令紅包，顯示輸入口令欄位
        if (packetType == PacketType.PW_PACKET.code) {
            titleLayout.hide()
            watchWordLayout.show()
        } else {
            titleLayout.show()
            watchWordLayout.hide()
        }
    }

    //紅包成功開啟 dialog
    @SuppressLint("SetTextI18n")
    fun showRedPacketOpenDialog(money: Double) {
        binding.redEnpOpen.visibility = View.VISIBLE
        binding.redEnpClose.visibility = View.GONE
        binding.tvMoney.text = ArithUtil.toMoneyFormat(money)
    }

    fun showPWErrorHint() {
//        if (watchWordInput.text.isNotEmpty()) {
        binding.watchWordMessage.text = requireContext().getString(R.string.chat_error_input)
//        } else
//            watchWordMessage.text = context.getString(R.string.chat_enter_password)
    }

}
