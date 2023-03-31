package org.cxct.sportlottery.ui.money.withdraw

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_withdraw_password.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.login.afterTextChanged

class WithdrawPassWordDialog(private val listener: WithdrawPasswordDialogListener) : DialogFragment() {

    private val passwordViewList by lazy { mutableListOf<ImageView>(iv_first, iv_second, iv_third, iv_fourth) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.dialog_withdraw_password, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        initEvent()
        initView()
    }

    private fun initEvent() {
        setupFocusEvent()
        setupOnClick()
        setupInputEvent()
    }

    private fun initView() {
        hidePassword("")
    }

    private fun setupFocusEvent() {
        cv_frame.setOnClickListener {
            showKeyBoard()
            et_withdrawal_password.requestFocus()
        }

        et_withdrawal_password.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                checkPasswordFormat()
            }
        }

        et_withdrawal_password.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    modifyFinish()
                    return true
                }
                return false
            }
        })
    }

    private fun setupInputEvent() {
        et_withdrawal_password.afterTextChanged {
            hidePassword(it)
            checkPasswordFormat()
        }
    }

    private fun hidePassword(password: String) {
        passwordViewList.forEach {
            if (passwordViewList.indexOf(it) < password.length) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.INVISIBLE
            }
        }
    }

    private fun setupOnClick() {
        btn_confirm.setOnClickListener {
            if (checkPasswordFormat()) {
                dismiss()
                et_withdrawal_password?.let {
                    listener.onConfirm(it.text.toString())
                }
            }
        }
    }

    private fun checkPasswordFormat(): Boolean {
        return if (et_withdrawal_password.text.length != 4) {
            showErrorFrame(true)
            false
        } else {
            showErrorFrame(false)
            true
        }
    }

    private fun showErrorFrame(isError: Boolean) {
        val showBackground: Drawable?
        val showColor: Int
        if (!isError) {
            showBackground = ContextCompat.getDrawable(context ?: requireContext(), R.color.color_666666_bcbcbc)
            showColor = ContextCompat.getColor(context ?: requireContext(), R.color.color_666666_bcbcbc)
        } else {
            showBackground = ContextCompat.getDrawable(context ?: requireContext(), R.color.color_E44438_e44438)
            showColor = ContextCompat.getColor(context ?: requireContext(), R.color.color_E44438_e44438)
        }
        cv_frame.setCardBackgroundColor(showColor)
        line1.background = showBackground
        line2.background = showBackground
        line3.background = showBackground
    }

    private fun modifyFinish() {
        hideKeyBoard()
        this@WithdrawPassWordDialog.dialog?.currentFocus?.clearFocus()
    }

    private fun showKeyBoard() {
        try {
            val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideKeyBoard() {
        try {
            //*隱藏軟鍵盤
            val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val focusedView = this@WithdrawPassWordDialog.dialog?.currentFocus
            if (inputMethodManager.isActive && focusedView != null) {
                inputMethodManager.hideSoftInputFromWindow(focusedView.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class WithdrawPasswordDialogListener(private val confirmEvent: (password: String) -> Unit) {
    fun onConfirm(password: String) = confirmEvent(password)

}
