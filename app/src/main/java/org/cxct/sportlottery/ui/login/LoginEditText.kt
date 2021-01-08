package org.cxct.sportlottery.ui.login

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.edittext_login.view.*
import org.cxct.sportlottery.R

class LoginEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    var eyeVisibility
        get() = btn_eye.visibility
        set(value) {
            btn_eye.visibility = value
        }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.edittext_login, this, false)
        addView(view)

        try {
            val typedArray = context.theme
                .obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)

            view.tv_title.text = typedArray.getText(R.styleable.CustomView_cvTitle)
            view.et_input.setText(typedArray.getText(R.styleable.CustomView_cvText))
            view.et_input.hint = typedArray.getText(R.styleable.CustomView_cvHint)


            val inputType = typedArray.getInt(R.styleable.CustomView_cvInputType, 0x00000001)
            view.et_input.inputType = inputType

            view.btn_eye.visibility = if (inputType == 0x00000081) View.VISIBLE else View.GONE
            setupEye(view.btn_eye)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupEye(v: View) {
        v.setOnClickListener {
            if (v.isSelected) {
                v.isSelected = false
                et_input.transformationMethod = PasswordTransformationMethod.getInstance() //不顯示
            } else {
                v.isSelected = true
                et_input.transformationMethod = HideReturnsTransformationMethod.getInstance() //顯示
            }
        }
    }

    fun setError(value: String?) {
        tv_error.text = value
    }

    fun setText(value: String?) {
        et_input.setText(value)
    }

    fun getText(): String {
        return et_input.text.toString()
    }

    fun getEditText(): EditText {
        return et_input
    }

    fun afterTextChanged(afterTextChanged: (String) -> Unit) {
        et_input.afterTextChanged { afterTextChanged.invoke(it) }
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}