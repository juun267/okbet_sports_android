package org.cxct.sportlottery.view.dialog

import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogScanCameraPhotoBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils

class ScanPhotoDialog : BaseDialog<BaseViewModel,DialogScanCameraPhotoBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }
    var tvAlbumClickListener: (() -> Unit)? = null
    var tvCameraScanClickListener: (() -> Unit)? = null

    override fun onInitView() {
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