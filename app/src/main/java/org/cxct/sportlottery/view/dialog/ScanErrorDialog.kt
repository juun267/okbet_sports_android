package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils

class ScanErrorDialog(mContext: Context) : Dialog(mContext) {

    init {
        initDialog()
    }

    var onIvCloseClickListener:(()->Unit )? = null

    private fun initDialog() {
        setContentView(R.layout.dialog_photo_error)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(false)

        findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            dismiss()
            onIvCloseClickListener?.invoke()
        }
    }


}