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


@Suppress("DEPRECATION")
class KeyBoardUtil(
    private val keyboardView: CustomKeyBoardView,
    private val parent: View?,
    private val presetBetAmount: List<Int>,
    private val isLogin: Boolean,
    private var maxBetMoney: Long?,
    private val keyBoardViewListener: KeyBoardViewListener
) : OnKeyboardActionListener {


    init {
        this.keyboardView.setOnKeyboardActionListener(this)
        this.keyboardView.keyboard = Keyboard(keyboardView.context, R.xml.keyboard)
        this.keyboardView.isEnabled = true
        this.keyboardView.isPreviewEnabled = false

        setPresetBetAmount(this.keyboardView.keyboard.keys)
    }

    private fun setPresetBetAmount(keys: MutableList<Keyboard.Key>) {
       keys.forEach {
           when(it.codes[0]){
               KeyBoardCode.PLUS_25.code -> it.label = "+ ${presetBetAmount[0]}"
               KeyBoardCode.PLUS_50.code -> it.label = "+ ${presetBetAmount[1]}"
               KeyBoardCode.PLUS_75.code -> it.label = "+ ${presetBetAmount[2]}"
               KeyBoardCode.PLUS_100.code -> it.label = "+ ${presetBetAmount[3]}"

           }
       }
    }


    private lateinit var mEditText: EditText


    private var isShow = false


    fun showKeyboard(editText: EditText, position: Int?, maxBetMoney: Long) {
        this.mEditText = editText
        this.maxBetMoney = maxBetMoney
        //InputType.TYPE_NULL 禁止彈出系統鍵盤
        mEditText.apply {
            inputType = InputType.TYPE_NULL
            isFocusable = true
            isFocusableInTouchMode = true
        }
        keyboardView.visibility = View.VISIBLE
        parent?.visibility = View.VISIBLE

        isShow = true

        keyBoardViewListener.showOrHideKeyBoardBackground(true, position)
    }


    fun hideKeyboard() {
        keyboardView.visibility = View.GONE
        parent?.visibility = View.INVISIBLE
        if (isShow) mEditText.isFocusable = false

        isShow = false

        keyBoardViewListener.showOrHideKeyBoardBackground(false, null)
    }


    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {
    }
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

            //KeyBoardCode.PLUS_10.code -> plus(presetBetAmount[0].toLong())
            KeyBoardCode.PLUS_25.code -> plus(presetBetAmount[0].toLong())

            KeyBoardCode.PLUS_50.code -> plus(presetBetAmount[1].toLong())
            KeyBoardCode.PLUS_75.code -> plus(presetBetAmount[2].toLong())

            KeyBoardCode.PLUS_100.code -> plus(presetBetAmount[3].toLong())

            KeyBoardCode.MAX.code -> {
                if(isLogin){
                    plusAll(maxBetMoney ?: 0)
                }else{
                    keyBoardViewListener.showLoginNotice()
                }
            }

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
                            if (!this.toString().contains(".")) "." else return
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
        } else input.toDouble() + count
        mEditText.setText(tran.toString())
        mEditText.setSelection(mEditText.text.length)
    }

    private fun plusAll(all: Long) {
        mEditText.setText(all.toString())
        mEditText.setSelection(mEditText.text.length)
    }

    interface KeyBoardViewListener{
        fun showLoginNotice()

        fun showOrHideKeyBoardBackground(isShow: Boolean, position: Int?)
    }


}



