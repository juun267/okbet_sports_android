package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import org.cxct.sportlottery.databinding.ViewUploadBinding
import splitties.systemservices.layoutInflater

class UploadImageView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defaultStyle: Int = 0
) : LinearLayout(context, attributeSet, defaultStyle) {

    var uploadListener: UploadListener? = null
    val binding by lazy {  ViewUploadBinding.inflate(layoutInflater) }
    init {
        val view = binding.root
        addView(view)

        setupClickEvent()
    }

    private fun setupClickEvent() {
        binding.bgUpload.setOnClickListener{uploadListener?.upload() }
    }

    class UploadListener(private val uploadClick: () -> Unit){
        fun upload() = uploadClick.invoke()
    }
}