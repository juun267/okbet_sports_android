package org.cxct.sportlottery.ui.profileCenter.identity

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.FragmentActivity
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils

class OCRFailedDialog(private val context: FragmentActivity): Dialog(context) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createView())

        val attr = window!!.attributes
        attr.width = 310.dp
        attr.height = -2
        window!!.setBackgroundDrawable(DrawableCreatorUtils.getCommonBackgroundStyle(16, R.color.white))
    }

    private fun createView(): View {

        val lin = LinearLayout(context)
        lin.orientation = LinearLayout.VERTICAL
        lin.gravity = Gravity.CENTER_HORIZONTAL

        val iv = AppCompatImageView(context)
        iv.setImageResource(R.drawable.ic_close_circle_red)
        val ivLp = 52.dp.let { LinearLayout.LayoutParams(it, it) }
        ivLp.topMargin = 40.dp
        ivLp.bottomMargin = 16.dp
        lin.addView(iv, ivLp)

        val text = AppCompatTextView(context)
        text.textSize = 16f
        text.typeface = Typeface.DEFAULT_BOLD
        text.setTextColor(Color.BLACK)
        text.setText(R.string.P257)
        lin.addView(text, LinearLayout.LayoutParams(-2, -2).apply { gravity = Gravity.CENTER_HORIZONTAL })

        val btn = AppCompatButton(context)
        btn.setText(R.string.btn_sure)
        btn.setTextColor(Color.WHITE)
        btn.setBackgroundResource(R.drawable.bg_blue_radius_8)
        btn.setOnClickListener { dismiss() }
        val btnLp = LinearLayout.LayoutParams(-1, 40.dp)
        btnLp.topMargin = 24.dp
        btnLp.bottomMargin = 28.dp
        val dp20 = 20.dp
        btnLp.marginEnd = dp20
        btnLp.marginStart = dp20
        lin.addView(btn, btnLp)

        return lin
    }

}