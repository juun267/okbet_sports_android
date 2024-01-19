package org.cxct.sportlottery.ui.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding


abstract class BaseBindingDialog<VB : ViewBinding>(
    context: Context, private val inflate: (LayoutInflater) -> VB
) : Dialog(context) {

    lateinit var binding: VB

    var onFirstClickListener: (() -> Unit)? = null
    var onSecondClickListener: (() -> Unit)? = null
    var onThirdClickListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        initView()
        initLayoutParams()
    }

    private fun initLayoutParams() {
        val lp = window?.attributes
        lp?.apply {
            width = initWidthParams()
            height = initHeightParams()
            gravity = Gravity.CENTER
            window?.attributes = this
        }
    }

    abstract fun initHeightParams(): Int

    abstract fun initWidthParams(): Int

    abstract fun initView()
}