package org.cxct.sportlottery.util


import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.text.InputType
import android.view.View
import android.widget.EditText
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.CustomKeyBoardView
import org.cxct.sportlottery.ui.common.KeyBoardCode


@Suppress("DEPRECATION")
class KeyBoardUtil(private val keyboardView: CustomKeyBoardView, private val parent: View?) :
    OnKeyboardActionListener {


    init {
        this.keyboardView.setOnKeyboardActionListener(this)
        this.keyboardView.keyboard = Keyboard(keyboardView.context, R.xml.keyboard)
        this.keyboardView.isEnabled = true
        this.keyboardView.isPreviewEnabled = false
    }


    private lateinit var mEditText: EditText


    private var isShow = false


    fun showKeyboard(editText: EditText) {
        this.mEditText = editText

        //InputType.TYPE_NULL 禁止彈出系統鍵盤
        mEditText.apply {
            inputType = InputType.TYPE_NULL
            isFocusable = true
            isFocusableInTouchMode = true
        }
        keyboardView.visibility = View.VISIBLE
        parent?.visibility = View.VISIBLE

        isShow = true
    }


    fun hideKeyboard() {
        keyboardView.visibility = View.GONE
        parent?.visibility = View.INVISIBLE
        if (isShow) mEditText.isFocusable = false

        isShow = false
    }


    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}


    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val editable = mEditText.text
        val start = mEditText.selectionStart
        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> if (editable != null && editable.isNotEmpty()) {
                if (start > 0) {
                    editable.delete(start - 1, start)
                }
            }

            Keyboard.KEYCODE_DONE -> {
                hideKeyboard()
            }

            KeyBoardCode.PLUS_10.code -> plus(KeyBoardCode.PLUS_10.value.toLong())

            KeyBoardCode.PLUS_50.code -> plus(KeyBoardCode.PLUS_50.value.toLong())

            KeyBoardCode.INSERT_0.code -> {
                if (editable.isNotEmpty()) {
                    editable.insert(start, primaryCode.toChar().toString())
                }
            }
            KeyBoardCode.DOT.code -> {
                editable.apply {
                    insert(
                        start,
                        if (isNotEmpty()) {
                            if (!toString().contains(KeyBoardCode.DOT.value)) KeyBoardCode.DOT.value else return
                        } else {
                            "0."
                        }
                    )
                }
            }

            else -> {
                editable.insert(start, primaryCode.toChar().toString())
            }
        }
    }


    private fun plus(count: Long) {
        val input = if (mEditText.text.toString() == "") "0" else mEditText.text.toString()
        val tran = if (input.contains(".")) {
            input.toDouble() + count
        } else input.toLong() + count
        mEditText.setText(tran.toString())
        mEditText.setSelection(mEditText.text.length)
    }


}



