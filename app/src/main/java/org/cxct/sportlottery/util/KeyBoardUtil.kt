package org.cxct.sportlottery.util

import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.common.CustomKeyBoardView
import org.cxct.sportlottery.ui.common.KeyBoardCode

class KeyBoardUtil(private val keyboardView: CustomKeyBoardView, private val parent: View) :
    OnKeyboardActionListener {


    init {
        this.keyboardView.setOnKeyboardActionListener(this)
        this.keyboardView.keyboard = Keyboard(keyboardView.context, R.xml.keyboard)
        this.keyboardView.isEnabled = true
        this.keyboardView.isPreviewEnabled = false
    }


    private lateinit var mEditText: EditText


    fun showKeyboard(editText: EditText) {
        this.mEditText = editText

        //InputType.TYPE_NULL 禁止彈出系統鍵盤
        mEditText.inputType = InputType.TYPE_NULL
        parent.visibility = View.VISIBLE
    }


    fun hideKeyboard() {
        parent.visibility = View.INVISIBLE
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
            KeyBoardCode.DELETE.code -> if (editable != null && editable.isNotEmpty()) {
                if (start > 0) {
                    editable.delete(start - 1, start)
                }
            }

            KeyBoardCode.PLUS_100.code -> plus(KeyBoardCode.PLUS_100.value.toLong())

            KeyBoardCode.PLUS_1000.code -> plus(KeyBoardCode.PLUS_1000.value.toLong())

            KeyBoardCode.PLUS_10000.code -> plus(KeyBoardCode.PLUS_10000.value.toLong())

            KeyBoardCode.INSERT_0.code -> {
                if(editable.isNotEmpty()){
                    editable.insert(start, primaryCode.toChar().toString())
                }
            }
            KeyBoardCode.INSERT_00.code -> {
               if(editable.isNotEmpty()){
                   editable.insert(start, KeyBoardCode.INSERT_00.value)
               }
            }

            else -> {
                editable.insert(start, primaryCode.toChar().toString())
            }
        }
    }


    private fun plus(count: Long) {
        val input = if (mEditText.text.toString() == "") "0" else mEditText.text.toString()
        mEditText.setText((input.toLong() + count).toString())
        mEditText.setSelection(mEditText.text.length)
    }


}