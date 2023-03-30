package org.cxct.sportlottery.ui.login

import android.content.Context
import android.graphics.Bitmap
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.IntDef
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.edittext_login.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.extentions.isEmptyStr
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.VerifyConstUtil
import org.cxct.sportlottery.widget.boundsEditText.TextFormFieldBoxes

class LoginEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var mVerificationCodeBtnOnClickListener: OnClickListener? = null
    private var mOnFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
        v_bottom_line.isSelected = hasFocus
        block_editText.isSelected = hasFocus
        setError(null)
    }

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

    private var clearListener: OnClickListener? = null

    var getAllIsShow
        get() = btn_withdraw_all.visibility == View.VISIBLE
        set(value) {
            btn_withdraw_all.visibility = if (value) View.VISIBLE else View.GONE
        }

    private var inputType: Int = 0
    private val typedArray by lazy {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
    }

    private val editable by lazy { typedArray.getBoolean(R.styleable.CustomView_cvEditable, true) }
    private val isShowLine by lazy { typedArray.getBoolean(R.styleable.CustomView_cvBottomLine, false) }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.edittext_login, this, false)
        addView(view)

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
        try {
            view.tv_title.text = typedArray.getText(R.styleable.CustomView_cvTitle)
            view.tv_title.setTypeface(
                null,
                typedArray.getInt(R.styleable.CustomView_cvTitleTextStyle, 1)
            )
            view.tv_title.setTextColor( typedArray.getInt(R.styleable.CustomView_cvTextColor, 1))
            view.et_input.setText(typedArray.getText(R.styleable.CustomView_cvText))
            view.et_input.hint = typedArray.getText(R.styleable.CustomView_cvHint)
            view.v_bottom_line2.isVisible = isShowLine
            if (!editable) {
                view.et_input.isEnabled = false
                view.et_input.inputType = InputType.TYPE_NULL
                view.et_input.isFocusable = false
                view.btn_clear.visibility = View.GONE
            }
            typedArray.getInt(R.styleable.CustomView_cvEms, -1).let {
                if (it > 0) {
                    view.et_input.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(it))
                }
            }
            typedArray.getInt(R.styleable.CustomView_cvTitleMinEms, -1).let {
                if (it > 0) {
                    view.tv_title.minEms = it
                }
            }
            view.block_verification_code.visibility = if (typedArray.getBoolean(
                    R.styleable.CustomView_cvEnableVerificationCode,
                    false
                )
            ) View.VISIBLE else View.GONE

            inputType = typedArray.getInt(R.styleable.CustomView_cvInputType, 0x00000001)
            when (inputType) {
                0x00000081 -> {
                    view.et_input.transformationMethod = PasswordTransformationMethod()
                }
                0x00000012 -> {
                    view.et_input.inputType = 0x00000002
                    view.et_input.transformationMethod = PasswordTransformationMethod()
                }
                else -> {
                    view.et_input.inputType = inputType
                }
            }


            view.et_input.isEnabled =
                typedArray.getBoolean(R.styleable.CustomView_cvEnable, true).apply {
                    if (this)
                        clearIsShow = false
                }

            view.tv_start.visibility =
                typedArray.getInt(R.styleable.CustomView_necessarySymbol, 0x00000008) //預設隱藏 需要再打開
            view.btn_withdraw_all.visibility = View.GONE //預設關閉 需要再打開
            view.btn_clear.visibility = View.GONE
            view.btn_eye.visibility =
                if (inputType == 0x00000081 || inputType == 0x00000012) View.VISIBLE else View.GONE

            //控制物件與下方的間距, default = 10dp
            val itemMarginBottom: Int =
                typedArray.getDimensionPixelOffset(R.styleable.CustomView_cvMarginBottom, 10.dp)
            setMarginBottom(itemMarginBottom)

            //分割線顏色
            val dividerColor: Int =
                typedArray.getResourceId(R.styleable.CustomView_cvDividerColor, 0)
            if (dividerColor != 0) {
                view.v_divider.setBackgroundResource(dividerColor)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
        afterTextChanged { }
        setupFocus()
        setupEye()
        setupEditTextClearListener()
        setupVerificationCode()
        setError(null)
        setupKeyBoardPressDown()

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

    fun setMaxLength(length: Int) {
        val filters = mutableListOf<InputFilter>()
        filters.addAll(et_input.filters)
        filters.add(LengthFilter(length))
        et_input.filters = filters.toTypedArray()
    }

    fun setupEditTextClearListener(listener: (() -> Unit)? = null) {
        listener?.let {
            clearListener = OnClickListener {
                et_input.setText("")
                listener.invoke()
            }
        }
        clearListener?.let {
            btn_clear.setOnClickListener(it)
        } ?: run {
            btn_clear.setOnClickListener {
                et_input.setText("")
            }
        }
    }

    private fun setupVerificationCode() {
        iv_verification_code.setOnClickListener {
            iv_verification_code.visibility = View.GONE
            mVerificationCodeBtnOnClickListener?.onClick(it)
        }
    }

    private fun setupKeyBoardPressDown() {
        et_input.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearFocus()
            }
            return@setOnEditorActionListener false
        }
    }

    fun setVerificationCodeBtnOnClickListener(l: OnClickListener?) {
        mVerificationCodeBtnOnClickListener = l
    }

    fun setVerificationCode(bitmap: Bitmap?) {
        iv_verification_code.visibility = View.VISIBLE
        Glide.with(this).load(bitmap).into(iv_verification_code)
    }

    fun setTitle(value: String?) {
        tv_title.text = value
    }

    fun setHint(value: String?) {
        et_input.hint = value
    }

    fun setError(value: String?) {
        tv_error.text = value
        if (value.isEmptyStr()) {
            tv_error.visibility = View.GONE
            block_editText.isActivated = false
            v_bottom_line.visibility = View.INVISIBLE
            v_bottom_line.isActivated = false
        } else {
            tv_error.visibility = View.VISIBLE
            v_bottom_line.visibility = View.VISIBLE
            block_editText.isActivated = true
            v_bottom_line.isActivated = true
        }
    }

    fun setMarginBottom(px: Int) {
        (layout.layoutParams as LayoutParams).setMargins(0, 0, 0, px)
    }

    fun setText(value: String?) {
        et_input.setText(value)
    }

    fun getText(): String {
        return et_input.text.toString()
    }

    fun setDigits(input: String) {
        et_input.keyListener = DigitsKeyListener.getInstance(input)
    }

    fun resetText() {
        et_input.setText("")
        setError(null)
    }

    fun afterTextChanged(afterTextChanged: (String) -> Unit) {
        if (editable) {
            et_input.afterTextChanged {
                if (inputType != 0x00000081 && inputType != 0x00000012 && et_input.isEnabled) {
                    clearIsShow = it.isNotEmpty()
                }
                afterTextChanged.invoke(it)
            }
        }
    }

    fun setCursor() {
        et_input.setSelection(et_input.text.length)
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

    @IntDef(View.VISIBLE, View.INVISIBLE, View.GONE)
    annotation class Visibility

    fun setNecessarySymbolVisibility(@Visibility visibility: Int) {
        tv_start.visibility = visibility
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

/**
 *Extension function to simplify setting an onFocusChange action to EditText components.
 */
fun EditText.onFocusChange(onFocusChange: (String) -> Unit) {
    this.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
        onFocusChange.invoke(this.text.toString())
    }
}

fun EditText.checkRegisterListener(onCheck: (String) -> Unit) {
    this.afterTextChanged { onCheck(it) }
    this.onFocusChange { onCheck(it) }
}

fun EditText.checkPhoneNum(textFieldBoxes: TextFormFieldBoxes, onResult: ((String?) -> Unit)?) {
    checkRegisterListener { phoneNum->
        val msg = when {
            phoneNum.isBlank() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPhone(phoneNum) -> {
                LocalUtils.getString(R.string.phone_no_error)
            }
            else -> null
        }

        textFieldBoxes.setError(msg, false)
        onResult?.invoke(if (msg == null) phoneNum else null)
    }
}

fun EditText.checkSMSCode(textFieldBoxes: TextFormFieldBoxes, onResult: ((String?) -> Unit)?) {
    checkRegisterListener { smsCode->
        val msg = when {
            smsCode.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            smsCode.length < 4 -> textFieldBoxes.context.getString(R.string.sms_code_length_error)
            !VerifyConstUtil.verifySMSCode(smsCode, 4) -> LocalUtils.getString(R.string.error_verification_code_by_sms)
            else -> null
        }

        textFieldBoxes.setError(msg, false)
        onResult?.invoke(if (msg == null) smsCode else null)
    }
}

fun EditText.checkEmail(textFieldBoxes: TextFormFieldBoxes, onResult: ((String?) -> Unit)?) {
    checkRegisterListener { email->
        val msg = when {
            email.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyMail(email) -> LocalUtils.getString(R.string.pls_enter_correct_email)
            else -> null
        }

        textFieldBoxes.setError(msg, false)
        onResult?.invoke(if (msg == null) email else null)
    }
}
