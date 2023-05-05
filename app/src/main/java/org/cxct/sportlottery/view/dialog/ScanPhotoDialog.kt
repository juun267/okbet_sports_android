package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator
import org.cxct.sportlottery.util.drawable.DrawableUtils

class ScanPhotoDialog(private val mContext: Context) : Dialog(mContext) {

    init {
        initDialog()
    }

    var tvAlbumClickListener: (() -> Unit)? = null
    var tvCameraScanClickListener: (() -> Unit)? = null

    private fun initDialog() {
        setContentView(R.layout.dialog_scan_camera_photo)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        val tvAlbum = findViewById<TextView>(R.id.tvAlbum)
        val tvCameraScan = findViewById<TextView>(R.id.tvCameraScan)

        tvAlbum.background = DrawableUtils.getCommonBackgroundStyle(
            cornerRadius = 20.dp.toFloat(),
            strokeColor = mContext.getColor(R.color.color_025BE8),
            solidColor = mContext.getColor(R.color.color_025BE8),
        )

        tvCameraScan.background = DrawableUtils.getCommonBackgroundStyle(
            cornerRadius = 20.dp.toFloat(),
            strokeColor = mContext.getColor(R.color.color_ff8A00),
            solidColor = mContext.getColor(R.color.color_ff8A00),
        )

        tvAlbum.setOnClickListener {
            tvAlbumClickListener?.invoke()
            dismiss()
        }
        tvCameraScan.setOnClickListener {
            tvCameraScanClickListener?.invoke()
            dismiss()
        }


    }


}