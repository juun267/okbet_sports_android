package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogScanCameraPhotoBinding
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils

class ScanPhotoDialog(mContext: Context) : Dialog(mContext) {

    init {
        initDialog()
    }

    var tvAlbumClickListener: (() -> Unit)? = null
    var tvCameraScanClickListener: (() -> Unit)? = null


    private fun initDialog() {
        val binding = DialogScanCameraPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        val tvAlbum = findViewById<TextView>(R.id.tvAlbum)
        val tvCameraScan = findViewById<TextView>(R.id.tvCameraScan)

        tvAlbum.background = DrawableCreatorUtils.getCommonBackgroundStyle(
            20,
            R.color.color_025BE8,
        )

        tvCameraScan.background = DrawableCreatorUtils.getCommonBackgroundStyle(
            20,
            R.color.color_ff8A00,
        )

        tvAlbum.setOnClickListener {
            tvAlbumClickListener?.invoke()
            dismiss()
        }

        tvCameraScan.setOnClickListener {
            tvCameraScanClickListener?.invoke()
            dismiss()
        }

        val lp = window?.attributes
        lp?.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp?.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp?.gravity = Gravity.CENTER
        window?.attributes = lp
    }


}