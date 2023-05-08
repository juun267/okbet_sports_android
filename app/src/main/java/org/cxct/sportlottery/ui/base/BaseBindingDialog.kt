package org.cxct.sportlottery.ui.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding


abstract class BaseBindingDialog<VB : ViewBinding>(
    context: Context, private val inflate: (LayoutInflater) -> VB
) : Dialog(context) {

    lateinit var binding: VB

    var onFirstClickListener: (() -> Unit)? = null
    var onSecondClickListener: (() -> Unit)? = null
    var onThirdClickListener: (() -> Unit)? = null

    init {
        onCreateView()
    }

    fun onCreateView() {
        binding = inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        initView()
    }

    abstract fun initView()
}