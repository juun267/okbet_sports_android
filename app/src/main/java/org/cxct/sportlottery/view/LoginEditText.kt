package org.cxct.sportlottery.view

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
import android.util.TypedValue
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.databinding.EdittextLoginBinding
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.VerifyConstUtil
import org.cxct.sportlottery.view.boundsEditText.LoginFormFieldView
import org.cxct.sportlottery.view.boundsEditText.TextFormFieldBoxes
import splitties.systemservices.layoutInflater

class LoginEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayout(context, attrs, defStyle) {

    private var mVerificationCodeBtnOnClickListener: OnClickListener? = null
    private var mOnFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
        binding.vBottomLine.isSelected = hasFocus
        binding.blockEditText.isSelected = hasFocus
        setError(null)
    }

    var eyeVisibility
        get() = binding.btnEye.visibility
        set(value) {
            binding.btnEye.visibility = value
        }

    var clearIsShow
        get() = binding.btnClear.visibility == View.VISIBLE
        set(value) {
            binding.btnClear.visibility = if (value) View.VISIBLE else View.GONE
        }

    private var clearListener: OnClickListener? = null

    var getAllIsShow
        get() = binding.btnWithdrawAll.visibility == View.VISIBLE
        set(value) {
            binding.btnWithdrawAll.visibility = if (value) View.VISIBLE else View.GONE
        }

    private var inputType: Int = 0
    private var isShowLine: Boolean = true
    private val typedArray by lazy {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
    }

    private val editable by lazy { typedArray.getBoolean(R.styleable.CustomView_cvEditable, true) }
    val binding by lazy { EdittextLoginBinding.inflate(layoutInflater,this,false) }

    init {
        addView(binding.root)

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.CustomView, 0, 0)
        binding.run {
            tvTitle.text = typedArray.getText(R.styleable.CustomView_cvTitle)
            tvTitle.setTypeface(
                null,
                typedArray.getInt(R.styleable.CustomView_cvTitleTextStyle, 1)
            )
            tvTitle.setTextColor( typedArray.getInt(R.styleable.CustomView_cvTextColor, 1))
            etInput.setText(typedArray.getText(R.styleable.CustomView_cvText))
            etInput.hint = typedArray.getText(R.styleable.CustomView_cvHint)
            etInput.setTextSize(TypedValue.COMPLEX_UNIT_PX,typedArray.getDimension(R.styleable.CustomView_cvTextSize,resources.getDimension(R.dimen.textSize14sp)))
            etInput.setTypeface(null, typedArray.getInt(R.styleable.CustomView_cvTextStyle, 0))
            isShowLine = typedArray.getBoolean(R.styleable.CustomView_cvBottomLine, true)
            vBottomLine2.isVisible = isShowLine
            if (!editable) {
                etInput.isEnabled = false
                etInput.inputType = InputType.TYPE_NULL
                etInput.isFocusable = false
                btnClear.visibility = View.GONE
            }
            typedArray.getInt(R.styleable.CustomView_cvEms, -1).let {
                if (it > 0) {
                    etInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(it))
                }
            }
            typedArray.getInt(R.styleable.CustomView_cvTitleMinEms, -1).let {
                if (it > 0) {
                    tvTitle.minEms = it
                }
            }
            blockVerificationCode.visibility = if (typedArray.getBoolean(
                    R.styleable.CustomView_cvEnableVerificationCode,
                    false
                )
            ) View.VISIBLE else View.GONE

            inputType = typedArray.getInt(R.styleable.CustomView_cvInputType, 0x00000001)
            when (inputType) {
                0x00000081 -> {
                    etInput.transformationMethod = PasswordTransformationMethod()
                }
                0x00000012 -> {
                    etInput.inputType = 0x00000002
                    etInput.transformationMethod = PasswordTransformationMethod()
                }
                else -> {
                    etInput.inputType = inputType
                }
            }


            etInput.isEnabled =
                typedArray.getBoolean(R.styleable.CustomView_cvEnable, true).apply {
                    if (this)
                        clearIsShow = false
                }

            tvStart.visibility =
                typedArray.getInt(R.styleable.CustomView_necessarySymbol, 0x00000008) //預設隱藏 需要再打開
            btnWithdrawAll.visibility = View.GONE //預設關閉 需要再打開
            btnClear.visibility = View.GONE
            btnEye.visibility =
                if (inputType == 0x00000081 || inputType == 0x00000012) View.VISIBLE else View.GONE

            //控制物件與下方的間距, default = 10dp
            val itemMarginBottom: Int =
                typedArray.getDimensionPixelOffset(R.styleable.CustomView_cvMarginBottom, 10.dp)
            setMarginBottom(itemMarginBottom)

            //分割線顏色
            val dividerColor: Int =
                typedArray.getResourceId(R.styleable.CustomView_cvDividerColor, 0)
            if (dividerColor != 0) {
                vDivider.setBackgroundResource(dividerColor)
            }
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
        binding.etInput.onFocusChangeListener = mOnFocusChangeListener
    }

    private fun setupEye()=binding.run{
        btnEye.setOnClickListener {
            if (cbEye.isChecked) {
                cbEye.isChecked = false
                etInput.transformationMethod = PasswordTransformationMethod.getInstance() //不顯示
            } else {
                cbEye.isChecked = true
                etInput.transformationMethod = HideReturnsTransformationMethod.getInstance() //顯示
            }
            etInput.setSelection(etInput.length())
        }
    }

    fun setMaxLength(length: Int)=binding.run{
        val filters = mutableListOf<InputFilter>()
        filters.addAll(etInput.filters)
        filters.add(LengthFilter(length))
        etInput.filters = filters.toTypedArray()
    }

    fun setupEditTextClearListener(listener: (() -> Unit)? = null)=binding.run {
        listener?.let {
            clearListener = OnClickListener {
                etInput.setText("")
                listener.invoke()
            }
        }
        clearListener?.let {
            btnClear.setOnClickListener(it)
        } ?: run {
            btnClear.setOnClickListener {
                etInput.setText("")
            }
        }
    }
    fun setSelection()=binding.run{
        etInput.setSelection(etInput.length())
    }

    private fun setupVerificationCode() {
        binding.ivVerificationCode.setOnClickListener {
            binding.ivVerificationCode.visibility = View.GONE
            mVerificationCodeBtnOnClickListener?.onClick(it)
        }
    }

    private fun setupKeyBoardPressDown()=binding.run {
        etInput.setOnEditorActionListener { v, actionId, event ->
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
        binding.ivVerificationCode.visibility = View.VISIBLE
        Glide.with(this).load(bitmap).into(binding.ivVerificationCode)
    }

    fun setTitle(value: String?) {
        binding.tvTitle.text = value
    }

    fun setHint(value: String?) {
        binding.etInput.hint = value
    }

    fun setError(value: String?)=binding.run {
        tvError.text = value
        if (value.isEmptyStr()) {
            tvError.visibility = View.GONE
            blockEditText.isActivated = false
            vBottomLine.visibility = View.INVISIBLE
            vBottomLine.isActivated = false
        } else {
            tvError.visibility = View.VISIBLE
            vBottomLine.visibility = View.VISIBLE
            blockEditText.isActivated = true
            vBottomLine.isActivated = true
        }
    }

    fun setMarginBottom(px: Int) {
        (binding.layout.layoutParams as LayoutParams).setMargins(0, 0, 0, px)
    }

    fun showLine(show: Boolean) {
        isShowLine = show
        binding.vBottomLine2.isVisible = isShowLine
    }

    fun setText(value: String?) {
        binding.etInput.setText(value)
    }

    fun getText(): String {
        return binding.etInput.text.toString()
    }

    fun setDigits(input: String) {
        binding.etInput.keyListener = DigitsKeyListener.getInstance(input)
    }

    fun resetText() {
        binding.etInput.setText("")
        setError(null)
    }

    fun afterTextChanged(afterTextChanged: (String) -> Unit) {
        if (editable) {
            binding.etInput.afterTextChanged {
                if (inputType != 0x00000081 && inputType != 0x00000012 && binding.etInput.isEnabled) {
                    clearIsShow = it.isNotEmpty()
                }
                afterTextChanged.invoke(it)
            }
        }
    }

    fun setCursor() {
        binding.etInput.setSelection(binding.etInput.text.length)
    }


    override fun clearFocus() {
        super.clearFocus()
        binding.etInput.clearFocus()
    }

    fun getAllButton(clickGetAll: (EditText) -> Unit) {
        binding.btnWithdrawAll.setOnClickListener {
            clickGetAll(binding.etInput)
        }
    }

    fun setEditTextOnFocusChangeListener(listener: ((View, Boolean) -> Unit)) {
        binding.etInput.setOnFocusChangeListener { v, hasFocus ->
            mOnFocusChangeListener.onFocusChange(v, hasFocus)
            listener.invoke(v, hasFocus)
        }
    }

    @IntDef(View.VISIBLE, View.INVISIBLE, View.GONE)
    annotation class Visibility

    fun setNecessarySymbolVisibility(@Visibility visibility: Int) {
        binding.tvStart.visibility = visibility
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun TextView.afterTextChanged(afterTextChanged: (String) -> Unit) {
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
fun TextView.onFocusChange(onFocusChange: (String) -> Unit) {
    this.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
        onFocusChange.invoke(this.text.toString())
    }
}

fun TextView.checkRegisterListener(onCheck: (String) -> Unit) {
    this.afterTextChanged { onCheck(it) }
    this.onFocusChange { onCheck(it) }
}

fun EditText.checkPhoneNum(textFieldBoxes: FrameLayout?, onResult: ((String?) -> Unit)?) {
    checkRegisterListener { phoneNum->
        val msg = when {
            phoneNum.isBlank() -> context.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPhone(phoneNum) -> {
                context.getString(R.string.phone_no_error)
            }
            else -> null
        }
        when(textFieldBoxes){
            is TextFormFieldBoxes-> textFieldBoxes.setError(msg, false)
            is LoginFormFieldView -> textFieldBoxes.setError(msg, false)
        }
        onResult?.invoke(if (msg == null) phoneNum else null)
    }
}

fun EditText.checkSMSCode(textFieldBoxes: FrameLayout?, onResult: ((String?) -> Unit)?) {
    checkRegisterListener { smsCode->
        val msg = when {
            smsCode.isNullOrEmpty() -> context.getString(R.string.error_input_empty)
            smsCode.length < 4 -> context.getString(R.string.sms_code_length_error)
            !VerifyConstUtil.verifySMSCode(smsCode, 4) -> context.getString(R.string.error_verification_code_by_sms)
            else -> null
        }
        when(textFieldBoxes){
            is TextFormFieldBoxes-> textFieldBoxes.setError(msg, false)
            is LoginFormFieldView -> textFieldBoxes.setError(msg, false)
        }
        onResult?.invoke(if (msg == null) smsCode else null)
    }
}

fun EditText.checkEmail(textFieldBoxes: FrameLayout, onResult: ((String?) -> Unit)?) {
    checkRegisterListener { email->
        val msg = when {
            email.isNullOrEmpty() -> context.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyMail(email) -> context.getString(R.string.pls_enter_correct_email)
            else -> null
        }

        when(textFieldBoxes){
            is TextFormFieldBoxes-> textFieldBoxes.setError(msg, false)
            is LoginFormFieldView -> textFieldBoxes.setError(msg, false)
        }
        onResult?.invoke(if (msg == null) email else null)
    }
}
fun EditText.checkUserName(textFieldBoxes: FrameLayout?, onResult: ((String?) -> Unit)?) {
    checkRegisterListener { username->
        val msg = when {
            username.isBlank() -> context.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyLengthRange(
                username,
                4,
                20
            ) -> {
                context.getString(R.string.pls_enter_correct_mobile_email_username)
            }
            else -> null
        }
        when(textFieldBoxes){
            is TextFormFieldBoxes-> textFieldBoxes.setError(msg, false)
            is LoginFormFieldView -> textFieldBoxes.setError(msg, false)
        }
        onResult?.invoke(if (msg == null) username else null)
    }
}

fun EditText.checkWithdrawPassword(textFieldBoxes: FrameLayout, other: EditText? = null, onResult: ((String?) -> Unit)?) {
    checkRegisterListener { text->
        val msg = when {
            text.isNullOrEmpty() -> context.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPayPwd(text) -> context.getString(R.string.error_withdraw_password_for_new)
            (other != null && other.text.toString() != text) -> context.getString(R.string.J169)
            else -> null
        }

        when(textFieldBoxes){
            is TextFormFieldBoxes-> textFieldBoxes.setError(msg, false)
            is LoginFormFieldView -> textFieldBoxes.setError(msg, false)
        }
        onResult?.invoke(if (msg == null) text else null)
    }
}
