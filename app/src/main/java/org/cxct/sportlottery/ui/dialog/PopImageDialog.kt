package org.cxct.sportlottery.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import kotlinx.android.synthetic.main.dialog_pop_image.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel

/**
 * 顯示棋牌彈窗
 */
class PopImageDialog(@DrawableRes val drawableResId: Int) :
    BaseDialog<BaseViewModel>(BaseViewModel::class) {

    init {
        setStyle(R.style.FullScreen)
    }

    companion object {
        var firstShow = true
    }

    var onClick: (() -> Unit)? = null
    var onDismiss: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_pop_image, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClose()
        initImage()
        firstShow = false
    }

    private fun setupClose() {
        btn_close.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss?.invoke()
    }

    private fun initImage() {
        image.setImageResource(drawableResId)
        image.setOnClickListener {
            dismiss()
            onClick?.invoke()
        }
    }

}