package org.cxct.sportlottery.ui.withdraw

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.dialog_withdraw_password.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.login.afterTextChanged

class WithdrawPassWordDialog(context: Context, private val listener: WithdrawPasswordDialogListener) : AlertDialog(context) {
    private val passwordViewList by lazy { mutableListOf<ImageView>(iv_first, iv_second, iv_third, iv_fourth) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_withdraw_password)
        window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        }
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
            showBackground = ContextCompat.getDrawable(context, R.color.colorSilver)
            showColor = ContextCompat.getColor(context, R.color.colorSilver)
        } else {
            showBackground = ContextCompat.getDrawable(context, R.color.colorRed)
            showColor = ContextCompat.getColor(context, R.color.colorRed)
        }
        cv_frame.setCardBackgroundColor(showColor)
        line1.background = showBackground
        line2.background = showBackground
        line3.background = showBackground
    }

    private fun modifyFinish() {
        hideKeyBoard()
        this@WithdrawPassWordDialog.currentFocus?.clearFocus()
    }

    private fun showKeyBoard() {
        try {
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideKeyBoard() {
        try {
            //*隱藏軟鍵盤
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val focusedView = this@WithdrawPassWordDialog.currentFocus
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
