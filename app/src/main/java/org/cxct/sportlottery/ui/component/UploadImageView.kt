package org.cxct.sportlottery.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_upload.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.profileCenter.profile.RechargePicSelectorDialog
import timber.log.Timber

class UploadImageView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defaultStyle: Int = 0
) : LinearLayout(context, attributeSet, defaultStyle) {
    var uploadListener: UploadListener? = null
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_upload, this, false)
        addView(view)

        setupClickEvent()
    }

    private fun setupClickEvent() {
        bg_upload.setOnClickListener{uploadListener?.upload() }
    }

    class UploadListener(private val uploadClick: () -> Unit){
        fun upload() = uploadClick.invoke()
    }
}