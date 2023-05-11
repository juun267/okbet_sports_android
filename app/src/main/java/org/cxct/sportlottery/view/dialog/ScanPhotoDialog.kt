package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogScanCameraPhotoBinding
import org.cxct.sportlottery.ui.base.BaseBindingDialog
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils

class ScanPhotoDialog(mContext: Context) : BaseBindingDialog<DialogScanCameraPhotoBinding>(mContext,
DialogScanCameraPhotoBinding::inflate) {


    var tvAlbumClickListener: (() -> Unit)? = null
    var tvCameraScanClickListener: (() -> Unit)? = null



    override fun initHeightParams(): Int = 115.dp

    override fun initWidthParams(): Int = 245.dp

    override fun initView() {
        binding.apply {
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
        }

    }


}