package org.cxct.sportlottery.ui.login

import android.content.Context
import android.graphics.Bitmap
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.edittext_login.view.*
import org.cxct.sportlottery.R

class LoginEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private var mVerificationCodeBtnOnClickListener: OnClickListener? = null
    private var mOnFocusChangeListener = OnFocusChangeListener { _, hasFocus -> block_editText.isSelected = hasFocus }

    var eyeVisibility
        get() = btn_eye.visibility
        set(value) {
            btn_eye.visibility = value
        }

    var clearIsShow
        get() = btn_clear.visibility == View.VISIBLE
        set(value) {
            btn_clear.visibility = if (value) View.VISIBLE else View.GONE
        }

    var getAllIsShow
        get() = btn_withdraw_all.visibility == View.VISIBLE
        set(value) {
            btn_withdraw_all.visibility = if (value) View.VISIBLE else View.GONE
        }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.edittext_login, this, false)
        addView(view)

        val typedArray = context.theme
            .obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
        try {
            view.tv_title.text = typedArray.getText(R.styleable.CustomView_cvTitle)
            view.et_input.setText(typedArray.getText(R.styleable.CustomView_cvText))
            view.et_input.hint = typedArray.getText(R.styleable.CustomView_cvHint)
            view.block_verification_code.visibility = if (typedArray.getBoolean(R.styleable.CustomView_cvEnableVerificationCode, false)) View.VISIBLE else View.GONE

            val inputType = typedArray.getInt(R.styleable.CustomView_cvInputType, 0x00000001)
            view.et_input.inputType = inputType

            view.btn_withdraw_all.visibility = View.GONE //預設關閉 需要再打開
            view.btn_clear.visibility = if (inputType == 0x00000081) View.GONE else View.VISIBLE
            view.btn_eye.visibility = if (inputType == 0x00000081) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }

        setupFocus()
        setupEye()
        setupClear()
        setupVerificationCode()
        setError(null)
    }

    private fun setupFocus() {
        et_input.onFocusChangeListener = mOnFocusChangeListener
    }

    private fun setupEye() {
        btn_eye.setOnClickListener {
            if (cb_eye.isChecked) {
                cb_eye.isChecked = false
                et_input.transformationMethod = PasswordTransformationMethod.getInstance() //不顯示
            } else {
                cb_eye.isChecked = true
                et_input.transformationMethod = HideReturnsTransformationMethod.getInstance() //顯示
            }
            et_input.setSelection(et_input.length())
        }
    }

    private fun setupClear() {
        btn_clear.setOnClickListener {
            et_input.setText("")
        }
    }

    private fun setupVerificationCode() {
        iv_verification_code.setOnClickListener {
            iv_verification_code.visibility = View.GONE
            mVerificationCodeBtnOnClickListener?.onClick(it)
        }
    }

    fun setVerificationCodeBtnOnClickListener(l: OnClickListener?) {
        mVerificationCodeBtnOnClickListener = l
    }

    fun setVerificationCode(bitmap: Bitmap?) {
        iv_verification_code.visibility = View.VISIBLE
        Glide.with(this).load(bitmap).into(iv_verification_code)
    }

    fun setHint(value: String?) {
        et_input.hint = value
    }

    fun setError(value: String?) {
        tv_error.text = value
        if (tv_error.text.isNullOrEmpty()) {
            tv_error.visibility = View.INVISIBLE
            block_editText.isActivated = false
        } else {
            tv_error.visibility = View.VISIBLE
            block_editText.isActivated = true
        }
    }

    fun setText(value: String?) {
        et_input.setText(value)
    }

    fun getText(): String {
        return et_input.text.toString()
    }

    fun afterTextChanged(afterTextChanged: (String) -> Unit) {
        et_input.afterTextChanged { afterTextChanged.invoke(it) }
    }

    fun getAllButton(clickGetAll: (EditText) -> Unit) {
        btn_withdraw_all.setOnClickListener {
            clickGetAll(et_input)
        }
    }

    fun setEditTextOnFocusChangeListener(listener: ((View, Boolean) -> Unit)) {
        et_input.setOnFocusChangeListener { v, hasFocus ->
            mOnFocusChangeListener.onFocusChange(v, hasFocus)
            listener.invoke(v, hasFocus)
        }
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